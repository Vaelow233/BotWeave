package org.vaelow233.botweave.connector.qq.ob11.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.event.MessageReceivedEvent;
import org.vaelow233.botweave.connector.qq.ob11.bot.OneBotBot;
import org.vaelow233.botweave.connector.qq.ob11.capability.OneBotMessaging;
import org.vaelow233.botweave.connector.qq.ob11.codec.OneBotCodec;
import org.vaelow233.botweave.connector.qq.ob11.config.OneBotConfig;
import org.vaelow233.botweave.connector.qq.ob11.session.OneBotSession;
import org.vaelow233.botweave.core.connector.Connector;
import org.vaelow233.botweave.core.connector.ConnectorContext;
import org.vaelow233.botweave.core.lifecycle.LifecycleState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.*;

public class OneBotConnector implements Connector {
    private final OneBotConfig configuration;
    private final ConnectorContext context;
    private final ScheduledThreadPoolExecutor loop;
    private final OneBotSession session;

    private volatile LifecycleState state = LifecycleState.CREATED;

    private final CompletableFuture<Void> started = new CompletableFuture<>();
    private final CompletableFuture<Void> stopped = new CompletableFuture<>();

    private OneBotBot bot;
    private String selfId;
    private boolean registered;
    private boolean terminating;

    private final Deque<JsonNode> startupEvents = new ArrayDeque<>();

    public OneBotConnector(OneBotConfig configuration, ConnectorContext context) {
        this.configuration = configuration;
        this.context = context;

        this.loop = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "qq-onebot11-control-" + context.id());
            thread.setDaemon(true);
            return thread;
        });
        loop.setRemoveOnCancelPolicy(true);

        this.session = new OneBotSession(
                configuration,
                loop,
                this::onEvent,
                this::terminate
        );

        context.manage(() -> {
            try {
                session.abortTransport();
            } finally {
                loop.shutdown();
            }
        });
    }

    @Override
    public String id() {
        return context.id();
    }

    @Override
    public LifecycleState state() {
        return state;
    }

    @Override
    public CompletionStage<Void> start() {
        if (!started.isDone()) {
            dispatch(this::beginStart, started);
        }
        return started.thenApply(value -> value);
    }

    /**
     * Begin to start the session
     */
    private void beginStart() {
        // If the session has created, return
        if (state != LifecycleState.CREATED) {
            return;
        }
        state = LifecycleState.STARTING;
        try {
            // Open the session, establish the connection
            session.open()
                    // Get the login information of the account
                    .thenCompose(unused -> session.request(
                            "get_login_info",
                            JsonNodeFactory.instance.objectNode()
                    ))
                    // Update the login information (selfId)
                    .thenCompose(reply -> {
                        selfId = OneBotCodec.id(reply.data(), "user_id");
                        if (Long.parseLong(selfId) <= 0) {
                            throw new IllegalStateException("Invalid login account ID");
                        }
                        return session.request("get_status", JsonNodeFactory.instance.objectNode());
                    })
                    .whenComplete((reply, error) -> {
                        if (terminating) {
                            return;
                        }
                        if (error != null) {
                            terminate(error);
                            return;
                        }
                        try {
                            finishStart(reply.data());
                        } catch (RuntimeException failure) {
                            terminate(failure);
                        }
                    });

        } catch (RuntimeException error) {
            terminate(error);
        }
    }

    /**
     * Finished starting the session
     *
     * @param status the status of the server
     */
    private void finishStart(JsonNode status) {
        // Set botId, create capabilities and bot instance, update status, register the bots, trigger the startup events
        BotId botId = new BotId("qq-onebot-11:" + selfId);
        OneBotMessaging messaging = new OneBotMessaging(botId, this::call);
        bot = new OneBotBot(botId, messaging);
        updateOperational(status);
        context.bots().register(bot);
        registered = true;
        state = LifecycleState.RUNNING;
        started.complete(null);
        while (!startupEvents.isEmpty() && !terminating) {
            onEvent(startupEvents.removeFirst());
        }
    }

    /**
     * Send a request to the server
     *
     * @param action The name of the action in Onebot 11
     * @param params The data of the action, which will be sent together
     * @return The completion stage
     */
    private CompletionStage<OneBotSession.Reply> call(String action, ObjectNode params) {
        CompletableFuture<OneBotSession.Reply> result = new CompletableFuture<>();

        dispatch(() -> {
            try {
                // If the session is neither running nor operational, return the stage with exception
                if (state != LifecycleState.RUNNING || !bot.state().operational()) {
                    throw new IllegalStateException("Bot is not operational");
                }
                // Send the request
                session.request(action, params).whenComplete((reply, error) -> {
                    if (error == null) {
                        result.complete(reply);
                    } else {
                        result.completeExceptionally(error);
                    }
                });
            } catch (RuntimeException error) {
                result.completeExceptionally(error);
            }
        }, result);

        // Handle the result and return
        return result.handleAsync((reply, error) -> {
            if (error != null) {
                throw new CompletionException(error);
            }
            return reply;
        }, context.completionExecutor());
    }

    /**
     * Handle an event
     *
     * @param event The event need to be handled
     */
    private void onEvent(JsonNode event) {
        if (terminating) {
            return;
        }
        if (state == LifecycleState.STARTING) {
            if ("message".equals(event.path("post_type").asText())) {
                startupEvents.addLast(event);
            }
            return;
        }
        if (state != LifecycleState.RUNNING) {
            return;
        }
        try {
            if (!selfId.equals(OneBotCodec.id(event, "self_id"))) {
                // Ignored event from a different bot account;
                return;
            }

            if ("meta_event".equals(event.path("post_type").asText())
                    && "heartbeat".equals(event.path("meta_event_type").asText())) {
                // The event is related to the status
                updateOperational(event.path("status"));
                return;
            }

            if ("message".equals(event.path("post_type").asText()) && selfId.equals(OneBotCodec.id(event, "user_id"))) {
                // Ignored the message that sent by self
                return;
            }
            Optional<MessageReceivedEvent> decoded = OneBotCodec.decodeEvent(bot, event);
            if (!decoded.isPresent()) {
                return;
            }
            MessageReceivedEvent messageEvent = decoded.get();
            context.executor().execute(() -> {
                if (state == LifecycleState.RUNNING) {
                    context.publisher().publish(messageEvent);
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void updateOperational(JsonNode status) {
        boolean operational = status.path("online").asBoolean(false)
                && status.path("good").asBoolean(false);
        bot.setOperational(operational);
    }

    @Override
    public CompletionStage<Void> stop() {
        if (!stopped.isDone()) {
            dispatch(() -> terminate(null), stopped);
        }
        return stopped.thenApply(value -> value);
    }

    private void terminate(Throwable error) {
        // Start to terminal the connection
        if (terminating) {
            return;
        }
        terminating = true;
        // If the error is not null, set the lifecycle state to failed
        state = error == null ? LifecycleState.STOPPING : LifecycleState.FAILED;
        // Trigger the started
        started.completeExceptionally(error == null ? new CancellationException("Connector stopped") : error);
        // Clear the startup events (they will not be triggered)
        startupEvents.clear();
        Throwable cleanupError = null;
        try {
            if (bot != null) {
                bot.setOperational(false);
            }
            // Unregister the bot
            if (registered) {
                context.bots().unregister(bot.id());
                registered = false;
            }
        } catch (RuntimeException failure) {
            cleanupError = failure;
        }
        Throwable registrationCleanupError = cleanupError;
        boolean failed = error != null;
        session.shutdown().whenComplete((unused, transportError) -> dispatch(() -> {
            // Change the lifecycle state if there are errors
            Throwable failure = registrationCleanupError;
            if (failure == null) {
                failure = transportError;
            } else if (transportError != null && transportError != failure) {
                failure.addSuppressed(transportError);
            }

            state = failed || failure != null ? LifecycleState.FAILED : LifecycleState.STOPPED;

            // Shutdown the executor
            loop.shutdown();

            // Finish the termination
            if (failure == null) {
                stopped.complete(null);
            } else {
                stopped.completeExceptionally(failure);
            }}, stopped)
        );
    }

    private void dispatch(Runnable action, CompletableFuture<?> failureTarget) {
        try {
            loop.execute(action);
        } catch (RejectedExecutionException error) {
            failureTarget.completeExceptionally(error);
        }
    }
}

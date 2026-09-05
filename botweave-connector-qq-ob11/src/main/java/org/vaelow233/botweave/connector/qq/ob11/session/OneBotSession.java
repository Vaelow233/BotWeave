package org.vaelow233.botweave.connector.qq.ob11.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.vaelow233.botweave.connector.qq.ob11.config.OneBotConfig;

import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class OneBotSession extends WebSocketClient {
    private final ScheduledThreadPoolExecutor loop;
    private final Consumer<JsonNode> eventConsumer;
    private final Consumer<Throwable> failureConsumer;

    private final CompletableFuture<Void> opened = new CompletableFuture<>();

    /**
     * When trying to open a connection, and the process didn't finished until reached the timeout, it'll be triggered
     */
    private ScheduledFuture<?> openTimeout;

    private long sequence = 0;

    /**
     * <code>true</code> when the session has launched, which means the connection has been established successfully
     */
    private boolean launched;

    /**
     * <code>true</code> when the session is going to be closed due to some reasons, at the same time new requests and responses will not be handled
     */
    private boolean terminal;

    /**
     * <code>true</code> when the connection has been closed
     */
    private final AtomicBoolean abort = new AtomicBoolean();

    /**
     * Something to be triggered when the session is closed
     */
    private final CompletableFuture<Void> ended = new CompletableFuture<>();

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * The pending requests, the key is the echo and the value is the pending request
     */
    private final Map<String, Pending> pending = new HashMap<>();

    public OneBotSession(OneBotConfig config, ScheduledThreadPoolExecutor loop, Consumer<JsonNode> eventConsumer, Consumer<Throwable> failureConsumer) {
        super(config.toURI(), new Draft_6455(), config.getHeaders(), 10_000);

        this.loop = loop;
        this.eventConsumer = eventConsumer;
        this.failureConsumer = failureConsumer;

        setDaemon(true);
        setConnectionLostTimeout(30);
    }

    /**
     * Run something in the loop thread
     *
     * @param action The action to be run
     */
    private void post(Runnable action) {
        try {
            loop.execute(action);
        } catch (RejectedExecutionException ignored) {}
    }

    /**
     * The method was called when an exception was occurred (failed to do some operations)
     * It'll close the connection and release the resources
     *
     * @param error The exception that was occurred
     */
    private void fail(Throwable error) {
        // Start to terminal the connection
        if (terminal) {
            return;
        }
        terminal = true;
        // Cancel the openTimeout because it has crashed
        if (openTimeout != null) {
            openTimeout.cancel(false);
        }
        // Close the connection
        abortTransport();

        List<Pending> waiting = new ArrayList<>(pending.values());
        // Clear all the pending requests to prevent to be handled
        pending.clear();
        // Trigger opened with exception
        opened.completeExceptionally(error);
        // Cancel all the pending request and trigger them with exception
        for (Pending entry : waiting) {
            entry.cancelTimeout();
            entry.result.completeExceptionally(error);
        }
        // Trigger ended with nothing
        if (!launched) {
            ended.complete(null);
        }
        // Trigger the failure consumer
        failureConsumer.accept(error);
    }

    /**
     * This method will abort the session, which means the connection will be closed
     */
    public void abortTransport() {
        abort.set(true);
        try {
            closeConnection(1000, "Connector stopped");
        } finally {
            Socket socket = getSocket();
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Open the connection
     *
     * @return The completion stage
     */
    public CompletionStage<Void> open() {
        if (launched || terminal) {
            return opened;
        }
        launched = true;
        openTimeout = loop.schedule(() -> fail(new TimeoutException("WebSocket open timed out")), 10, TimeUnit.SECONDS);
        try {
            connect();
        } catch (Exception e) {
            fail(e);
            ended.complete(null);
        }
        return opened;
    }

    /**
     * Send a request to the server
     *
     * @param action The name of the action in Onebot 11
     * @param params The data of the action, which will be sent together
     * @return The completion stage
     */
    public CompletionStage<Reply> request(String action, ObjectNode params) {
        CompletableFuture<Reply> result = new CompletableFuture<>();
        // If the session was terminated or not opened yet, return the stage with exception
        if (terminal || !isOpen()) {
            result.completeExceptionally(new IllegalStateException("WebSocket is not open"));
            return result;
        }
        String echo = Long.toString(++sequence);
        // Put the request into pending map
        Pending entry = new Pending(action, result);
        pending.put(echo, entry);
        try {
            // Return the stage with exception if the request was timeout
            entry.timeout = loop.schedule(() -> {
                if (pending.remove(echo, entry)) {
                    result.completeExceptionally(new TimeoutException("OneBot API timed out: " + action));
                }
            }, 30, TimeUnit.SECONDS);
            // Construct the request body and send
            ObjectNode request = mapper.createObjectNode();
            request.put("action", action);
            request.set("params", params);
            request.put("echo", echo);
            send(mapper.writeValueAsString(request));
        } catch (Exception error) {
            // Remove the request from pending map, cancel the timeout, return the stage with exception
            pending.remove(echo, entry);
            entry.cancelTimeout();
            result.completeExceptionally(error);
        }
        return result;
    }

    /**
     * Shutdown the session, close the connection, release the resources
     *
     * @return The completion stage
     */
    public CompletionStage<Void> shutdown() {
        fail(new CancellationException("OneBot session stopped"));
        return ended;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        post(() -> {
            if (terminal) {
                abortTransport();
                return;
            }
            if (openTimeout != null) {
                openTimeout.cancel(false);
            }
            opened.complete(null);
        });
    }

    /**
     * Handle a response when received and it's in valid JSON format
     *
     * @param root The body of the response, represented in JSON format
     * @param receivedAt The time that received the response
     */
    private void acceptResponse(JsonNode root, Instant receivedAt) {
        Pending entry = pending.remove(root.path("echo").asText());
        if (entry == null) {
            // Maybe timeout, or the response is duplicate
            return;
        }
        // Cancel the timeout (because we have received it)
        entry.cancelTimeout();
        JsonNode retcode = root.get("retcode");
        // Check whether the response is successful
        boolean success = "ok".equals(root.path("status").asText())
                && retcode != null
                && retcode.isIntegralNumber()
                && retcode.longValue() == 0;
        if (!success) {
            // If the response isn't successful, return the stage with exception
            entry.result.completeExceptionally(new IllegalStateException(
                    "OneBot API failed: action=" + entry.action
                            + ", status=" + root.path("status").asText()
                            + ", retcode=" + root.path("retcode").asText()
            ));
            return;
        }
        // The response is successful, return the data part of it
        entry.result.complete(new Reply(root.path("data"), receivedAt));
    }

    @Override
    public void onMessage(String message) {
        Instant receivedAt = Instant.now();
        post(() -> {
            // If the session was terminated, we will not handle
            if (terminal) {
                return;
            }
            try {
                // Try to parse the response in JSON format
                JsonNode root = mapper.readTree(message);
                if (root == null || !root.isObject()) {
                    throw new IllegalArgumentException("Expected OneBot JSON object");
                }
                if (root.has("post_type")) {
                    // Receive an event
                    eventConsumer.accept(root);
                } else if (root.has("echo")) {
                    // Receive a response
                    acceptResponse(root, receivedAt);
                }
            } catch (Exception error) {
                fail(error);
            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        post(() -> fail(new IOException("WebSocket closed: code=" + code)));
    }

    @Override
    public void onError(Exception ex) {
        post(() -> fail(ex));
    }

    private static final class Pending {
        private final String action;
        private final CompletableFuture<Reply> result;
        private ScheduledFuture<?> timeout;
        private Pending(String action, CompletableFuture<Reply> result) {
            this.action = action;
            this.result = result;
        }

        private void cancelTimeout() {
            if (timeout != null) {
                timeout.cancel(false);
            }
        }
    }

    public static final class Reply {
        private final JsonNode data;
        private final Instant acknowledgedAt;
        public Reply(JsonNode data, Instant acknowledgedAt) {
            this.data = data;
            this.acknowledgedAt = acknowledgedAt;
        }

        public JsonNode data() {
            return data;
        }

        public Instant acknowledgedAt() {
            return acknowledgedAt;
        }
    }
}

package org.vaelow233.botweave.core.connector;

import org.vaelow233.botweave.core.bot.BotRegistry;
import org.vaelow233.botweave.core.eventbus.SimpleEventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectorRegistry {
    private final BotRegistry bots;
    private final SimpleEventBus events;
    private final Executor businessExecutor;
    private final Executor cleanupExecutor;
    private final Executor controlExecutor;

    private final Map<String, ConnectorFactory<?>> factories = new ConcurrentHashMap<>();

    private final Map<String, Entry> connectors = new HashMap<>();

    private boolean closing;

    private final AtomicReference<CompletableFuture<Void>> stopping = new AtomicReference<>();

    public ConnectorRegistry(BotRegistry bots, SimpleEventBus events, Executor businessExecutor, Executor cleanupExecutor, Executor controlExecutor) {
        this.bots = bots;
        this.events = events;
        this.businessExecutor = businessExecutor;
        this.cleanupExecutor = cleanupExecutor;
        this.controlExecutor = controlExecutor;
    }

    public void registerFactory(ConnectorFactory<?> factory) {
        factories.put(factory.type(), factory);
    }

    public <C> CompletionStage<Void> start(String id, ConnectorFactory<C> factory, C configuration) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (stopping.get() != null) {
            result.completeExceptionally(new IllegalStateException("ConnectorRegistry is stopping"));
            return view(result);
        }
        execute(() -> {
            if (closing) {
                throw new IllegalStateException("ConnectorRegistry is stopping");
            }
            if (connectors.containsKey(id)) {
                throw new IllegalStateException("Connector already registered: " + id);
            }
            DefaultConnectorContext context = new DefaultConnectorContext(id, bots, events, events, businessExecutor);
            Entry entry = new Entry(id, context);
            connectors.put(id, entry);
            entry.started.whenComplete((unused, error) -> complete(result, error));

            try {
                Connector connector = factory.create(configuration, context);
                entry.managed = new ManagedConnector(connector, context, cleanupExecutor);
                if (!id.equals(connector.id())) {
                    throw new IllegalStateException("Connector ID does not match context ID: " + id);
                }
                entry.managed.start().whenComplete((unused, error) -> {
                    if (entry.started.isDone()) {
                        return;
                    }
                    execute(() -> onStarted(entry, error), entry.started);
                });
            } catch (RuntimeException error) {
                onStarted(entry, error);
            }
        }, result);

        return view(result);
    }

    public CompletionStage<Void> stop(String id) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        execute(() -> {
            Entry entry = connectors.get(id);
            if (entry == null) {
                result.complete(null);
                return;
            }
            entry.stopped.whenComplete((unused, error) -> complete(result, error));
            requestStop(entry);
        }, result);
        return view(result);
    }

    public CompletionStage<Void> stopAll() {
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (!stopping.compareAndSet(null, result)) {
            return view(stopping.get());
        }
        execute(() -> {
            closing = true;
            List<Entry> snapshot = new ArrayList<>(connectors.values());
            CompletableFuture<?>[] waits = new CompletableFuture<?>[snapshot.size()];
            for (int i = 0; i < snapshot.size(); i++) {
                Entry entry = snapshot.get(i);
                waits[i] = entry.stopped;
                requestStop(entry);
            }
            CompletableFuture.allOf(waits).whenComplete((unused, error) -> complete(result, error));
        }, result);

        return view(result);
    }

    private void onStarted(Entry entry, Throwable error) {
        if (entry.stopRequested) {
            return;
        }
        if (error == null) {
            entry.started.complete(null);
        } else {
            entry.started.completeExceptionally(error);
            requestStop(entry);
        }
    }

    private void requestStop(Entry entry) {
        if (entry.stopRequested) {
            return;
        }
        entry.stopRequested = true;
        entry.started.completeExceptionally(new CancellationException("Connector stopped during startup: " + entry.id));
        CompletionStage<Void> stopped;
        try {
            if (entry.managed != null) {
                stopped = entry.managed.stop();
            } else {
                stopped = CompletableFuture.runAsync(entry.context::closeScope, cleanupExecutor);
            }
        } catch (RuntimeException error) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(error);
            stopped = failed;
        }

        stopped.whenComplete((unused, error) ->
                execute(() -> {
                    connectors.remove(entry.id, entry);
                    complete(entry.stopped, error);
                }, entry.stopped)
        );
    }

    private void execute(Runnable action, CompletableFuture<?> failureTarget) {
        try {
            controlExecutor.execute(() -> {
                try {
                    action.run();
                } catch (RuntimeException error) {
                    failureTarget.completeExceptionally(error);
                }
            });
        } catch (RejectedExecutionException error) {
            failureTarget.completeExceptionally(error);
        }
    }

    private static void complete(CompletableFuture<Void> result, Throwable error) {
        if (error == null) {
            result.complete(null);
        } else {
            result.completeExceptionally(error);
        }
    }

    private static CompletionStage<Void> view(CompletableFuture<Void> result) {
        return result.thenApply(value -> value);
    }

    private static final class Entry {
        private final String id;
        private final DefaultConnectorContext context;
        private final CompletableFuture<Void> started = new CompletableFuture<>();
        private final CompletableFuture<Void> stopped = new CompletableFuture<>();

        private ManagedConnector managed;
        private boolean stopRequested;

        private Entry(String id, DefaultConnectorContext context) {
            this.id = id;
            this.context = context;
        }
    }
}
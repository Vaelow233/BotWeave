package org.vaelow233.botweave.core;

import org.vaelow233.botweave.api.eventbus.EventBus;
import org.vaelow233.botweave.core.bot.BotRegistry;
import org.vaelow233.botweave.core.bot.SimpleBotRegistry;
import org.vaelow233.botweave.core.connector.ConnectorRegistry;
import org.vaelow233.botweave.core.eventbus.SimpleEventBus;
import org.vaelow233.botweave.core.lifecycle.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class BotWeave implements AutoCloseable {
    private final BotRegistry bots;
    private final ConnectorRegistry connectors;
    private final SimpleEventBus eventBus;
    private final ResourceManager resources;

    private final ExecutorService controlExecutor;
    private final ExecutorService businessExecutor;
    private final ExecutorService cleanupExecutor;

    private final AtomicReference<CompletableFuture<Void>> stopping = new AtomicReference<>();

    public BotWeave() {
        this.eventBus = new SimpleEventBus();
        this.bots = new SimpleBotRegistry();
        this.resources = new ResourceManager();

        this.controlExecutor = Executors.newSingleThreadExecutor();
        this.businessExecutor = Executors.newFixedThreadPool(2);
        this.cleanupExecutor = Executors.newSingleThreadExecutor();

        this.connectors = new ConnectorRegistry(bots, eventBus, businessExecutor, cleanupExecutor, controlExecutor);
    }

    public BotRegistry bots() {
        return bots;
    }

    public EventBus events() {
        return eventBus;
    }

    public ConnectorRegistry connectors() {
        return connectors;
    }

    public CompletionStage<Void> stop() {
        CompletableFuture<Void> result = new CompletableFuture<>();
        if (!stopping.compareAndSet(null, result)) {
            return stopping.get().thenApply(value -> value);
        }
        connectors.stopAll().whenComplete((unused, error) -> {
            businessExecutor.shutdown();
            cleanupExecutor.shutdown();
            controlExecutor.shutdown();

            if (error == null) {
                result.complete(null);
            } else {
                result.completeExceptionally(error);
            }
        });
        return result.thenApply(value -> value);
    }

    @Override
    public void close() {
        stop().toCompletableFuture().join();
    }
}

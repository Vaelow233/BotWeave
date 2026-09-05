package org.vaelow233.botweave.core.connector;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

class ManagedConnector {
    private final Connector connector;
    private final DefaultConnectorContext context;
    private final Executor cleanupExecutor;

    private final AtomicReference<CompletableFuture<Void>> stopping = new AtomicReference<CompletableFuture<Void>>();

    ManagedConnector(Connector connector, DefaultConnectorContext context, Executor cleanupExecutor) {
        this.connector = connector;
        this.context = context;
        this.cleanupExecutor = cleanupExecutor;
    }

    CompletionStage<Void> start() {
        return connector.start();
    }

    CompletionStage<Void> stop() {
        CompletableFuture<Void> result = new CompletableFuture<>();

        if (!stopping.compareAndSet(null, result)) {
            return stopping.get().thenApply(value -> value);
        }

        CompletionStage<Void> connectorStopped;

        try {
            connectorStopped = connector.stop();
        } catch (RuntimeException error) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(error);
            connectorStopped = failed;
        }

        connectorStopped.handleAsync((unused, stopError) -> {
            Throwable failure = stopError;

            try {
                context.closeScope();
            } catch (RuntimeException closeError) {
                if (failure == null) {
                    failure = closeError;
                } else if (failure != closeError) {
                    failure.addSuppressed(closeError);
                }
            }

            if (failure != null) {
                throw new CompletionException(failure);
            }

            return (Void) null;
        }, cleanupExecutor).whenComplete((unused, error) -> {
            if (error == null) {
                result.complete(null);
            } else {
                result.completeExceptionally(error);
            }
        });

        return result.thenApply(value -> value);
    }
}

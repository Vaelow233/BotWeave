package org.vaelow233.botweave.core.connector;

import org.vaelow233.botweave.core.lifecycle.LifecycleState;

import java.util.concurrent.CompletionStage;

public interface Connector extends AutoCloseable {
    String id();
    LifecycleState state();
    CompletionStage<Void> start();
    CompletionStage<Void> stop();

    @Override
    default void close() {
        stop().toCompletableFuture().join();
    }
}

package org.vaelow233.botweave.api.eventbus;

public interface Subscription extends AutoCloseable {
    @Override
    void close();
}

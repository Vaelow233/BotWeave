package org.vaelow233.botweave.core.lifecycle;

import java.util.ArrayDeque;
import java.util.Deque;

public class ResourceManager implements AutoCloseable {
    private final Object lock = new Object();
    private final Deque<AutoCloseable> resources = new ArrayDeque<AutoCloseable>();
    private boolean closed;

    public <T extends AutoCloseable> T manage(T resource) {
        synchronized (lock) {
            if (closed) {
                throw new IllegalStateException("ResourceManager has closed");
            }
            resources.push(resource);
            return resource;
        }
    }

    @Override
    public void close() {
        Deque<AutoCloseable> closing;
        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;
            closing = new ArrayDeque<>(resources);
            resources.clear();
        }
        RuntimeException failure = null;
        AutoCloseable resource;
        while ((resource = closing.pollFirst()) != null) {
            try {
                resource.close();
            } catch (Exception error) {
                if (failure == null) {
                    failure = new IllegalStateException("Failed to close managed resources");
                }
                failure.addSuppressed(error);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}

package org.vaelow233.botweave.core.connector;

import org.vaelow233.botweave.api.eventbus.EventBus;
import org.vaelow233.botweave.api.eventbus.EventPublisher;
import org.vaelow233.botweave.core.bot.BotRegistry;
import org.vaelow233.botweave.core.lifecycle.ResourceManager;

import java.util.concurrent.Executor;

public class DefaultConnectorContext implements ConnectorContext {
    private final String id;
    private final BotRegistry bots;
    private final EventBus events;
    private final EventPublisher publisher;
    private final Executor executor;

    private final ResourceManager resources = new ResourceManager();

    public DefaultConnectorContext(String id, BotRegistry bots, EventBus events, EventPublisher publisher, Executor executor) {
        this.id = id;
        this.bots = bots;
        this.events = events;
        this.publisher = publisher;
        this.executor = executor;
    }

    @Override
    public <T extends AutoCloseable> T manage(T resource) {
        return resources.manage(resource);
    }

    void closeScope() {
        resources.close();
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public BotRegistry bots() {
        return this.bots;
    }

    @Override
    public EventBus events() {
        return this.events;
    }

    @Override
    public EventPublisher publisher() {
        return this.publisher;
    }

    @Override
    public Executor executor() {
        return this.executor;
    }
}

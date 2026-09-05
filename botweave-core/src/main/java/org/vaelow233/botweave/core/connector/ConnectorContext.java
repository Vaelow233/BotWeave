package org.vaelow233.botweave.core.connector;

import org.vaelow233.botweave.api.eventbus.EventBus;
import org.vaelow233.botweave.api.eventbus.EventPublisher;
import org.vaelow233.botweave.core.bot.BotRegistry;

import java.util.concurrent.Executor;

public interface ConnectorContext {
    String id();
    BotRegistry bots();
    EventBus events();
    EventPublisher publisher();
    Executor executor();
    <T extends AutoCloseable> T manage(T resource);
}

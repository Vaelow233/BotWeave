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
    /**
     * @return The executor used to handle events
     */
    Executor executor();
    /**
     * @return The executor used to return the async operation result
     */
    Executor completionExecutor();
    <T extends AutoCloseable> T manage(T resource);
}

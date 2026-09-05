package org.vaelow233.botweave.api.eventbus;

import org.vaelow233.botweave.api.event.BotEvent;

public interface EventPublisher {
    void publish(BotEvent event);
}

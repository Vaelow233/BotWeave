package org.vaelow233.botweave.api.eventbus;

import org.vaelow233.botweave.api.event.BotEvent;

@FunctionalInterface
public interface EventListener<E extends BotEvent> {
    void onEvent(E event);
}
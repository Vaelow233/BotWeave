package org.vaelow233.botweave.api.eventbus;

import org.vaelow233.botweave.api.event.BotEvent;

public interface EventBus {
    <E extends BotEvent> Subscription subscribe(Class<E> eventType, EventListener<? super E> listener);
}

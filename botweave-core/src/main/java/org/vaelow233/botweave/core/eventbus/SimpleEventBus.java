package org.vaelow233.botweave.core.eventbus;

import org.vaelow233.botweave.api.event.BotEvent;
import org.vaelow233.botweave.api.eventbus.EventBus;
import org.vaelow233.botweave.api.eventbus.EventListener;
import org.vaelow233.botweave.api.eventbus.EventPublisher;
import org.vaelow233.botweave.api.eventbus.Subscription;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleEventBus implements EventBus, EventPublisher {
    private final CopyOnWriteArrayList<Registration<?>> registrations = new CopyOnWriteArrayList<>();

    @Override
    public <E extends BotEvent> Subscription subscribe(Class<E> eventType, EventListener<? super E> listener) {
        Registration<E> registration = new Registration<>(eventType, listener, registrations);
        registrations.add(registration);
        return registration;
    }

    @Override
    public void publish(BotEvent event) {
        for (Registration<?> registration : registrations) {
            try {
                registration.dispatch(event);
            } catch (RuntimeException error) {
                error.printStackTrace();
            }
        }
    }

    private static final class Registration<E extends BotEvent> implements Subscription {

        private final Class<E> eventType;
        private final EventListener<? super E> listener;
        private final CopyOnWriteArrayList<Registration<?>> owner;

        private final AtomicBoolean active = new AtomicBoolean(true);

        private Registration(Class<E> eventType, EventListener<? super E> listener, CopyOnWriteArrayList<Registration<?>> owner) {
            this.eventType = eventType;
            this.listener = listener;
            this.owner = owner;
        }

        private void dispatch(BotEvent event) {
            if (!active.get() || !eventType.isInstance(event)) {
                return;
            }
            listener.onEvent(eventType.cast(event));
        }

        @Override
        public void close() {
            if (active.compareAndSet(true, false)) {
                owner.remove(this);
            }
        }
    }
}
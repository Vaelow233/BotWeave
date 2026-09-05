package org.vaelow233.botweave.core.bot;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.bot.BotId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleBotRegistry implements BotRegistry {
    private final ConcurrentMap<BotId, Bot> bots = new ConcurrentHashMap<>();

    @Override
    public Collection<Bot> all() {
        return new ArrayList<>(this.bots.values());
    }

    @Override
    public Optional<Bot> find(BotId id) {
        return Optional.ofNullable(this.bots.get(id));
    }

    @Override
    public void register(Bot bot) {
        Bot previous = this.bots.putIfAbsent(bot.id(), bot);
        if (previous != null) {
            throw new IllegalStateException("Bot already registered: " + bot.id());
        }
    }

    @Override
    public void unregister(BotId id) {
        this.bots.remove(id);
    }
}
package org.vaelow233.botweave.core.bot;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.bot.BotId;

import java.util.Collection;
import java.util.Optional;

public interface BotRegistry {
    Collection<Bot> all();
    Optional<Bot> find(BotId id);
    void register(Bot bot);
    void unregister(BotId id);
}

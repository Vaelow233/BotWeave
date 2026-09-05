package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;

import java.time.Instant;

public interface BotEvent {
    String type();
    Bot bot();
    Instant time();
}

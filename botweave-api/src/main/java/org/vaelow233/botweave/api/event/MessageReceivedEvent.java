package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.message.MessageRef;

import java.time.Instant;

public class MessageReceivedEvent implements BotEvent {
    private final Bot bot;
    private final MessageRef message;
    private final Instant time;
    public MessageReceivedEvent(Bot bot, MessageRef message, Instant time) {
        this.bot = bot;
        this.message = message;
        this.time = time;
    }

    @Override
    public String type() {
        return "message-received";
    }

    @Override
    public Bot bot() {
        return this.bot;
    }

    @Override
    public Instant time() {
        return this.time;
    }

    public MessageRef message() {
        return this.message;
    }
}

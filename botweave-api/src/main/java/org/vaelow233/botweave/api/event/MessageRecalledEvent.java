package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Instant;

public class MessageRecalledEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final MessageId message;
    private final UserRef operator;
    private final UserRef user;
    private final Instant time;
    public MessageRecalledEvent(Bot bot, ConversationRef conversation, MessageId message, UserRef operator, UserRef user, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.message = message;
        this.operator = operator;
        this.user = user;
        this.time = time;
    }

    @Override
    public String type() {
        return "message-recalled";
    }

    @Override
    public Bot bot() {
        return this.bot;
    }

    @Override
    public Instant time() {
        return this.time;
    }

    public MessageId messageId() {
        return this.message;
    }

    public ConversationRef conversation() {
        return this.conversation;
    }

    public UserRef operator() {
        return operator;
    }

    public UserRef user() {
        return user;
    }
}

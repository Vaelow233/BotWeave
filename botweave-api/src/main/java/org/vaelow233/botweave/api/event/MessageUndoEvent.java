package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.MessageRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Instant;

public class MessageUndoEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final MessageId message;
    private final UserRef operator;
    private final UserRef user;
    private final Instant time;
    public MessageUndoEvent(Bot bot, ConversationRef conversation, MessageId message, UserRef operator, UserRef user, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.message = message;
        this.operator = operator;
        this.user = user;
        this.time = time;
    }

    @Override
    public String type() {
        return "message-undo";
    }

    @Override
    public Bot bot() {
        return this.bot;
    }

    @Override
    public Instant time() {
        return this.time;
    }

    public MessageId message() {
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

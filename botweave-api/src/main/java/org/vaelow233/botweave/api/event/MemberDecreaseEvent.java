package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Instant;
import java.util.Optional;

public class MemberDecreaseEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final UserRef operator;
    private final UserRef user;
    private final Optional<SubType> type;
    private final Instant time;
    public MemberDecreaseEvent(Bot bot, ConversationRef conversation, UserRef operator, UserRef user, Optional<SubType> type, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.operator = operator;
        this.user = user;
        this.type = type;
        this.time = time;
    }

    @Override
    public String type() {
        return "member-decrease";
    }

    @Override
    public Bot bot() {
        return bot;
    }

    @Override
    public Instant time() {
        return time;
    }

    public ConversationRef conversation() {
        return conversation;
    }

    public UserRef user() {
        return user;
    }

    public UserRef operator() {
        return operator;
    }

    public Optional<SubType> subType() {
        return type;
    }

    public enum SubType {
        LEAVE,
        KICK,
        KICK_ME
    }
}

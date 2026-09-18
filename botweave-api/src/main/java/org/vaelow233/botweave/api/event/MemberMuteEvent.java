package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class MemberMuteEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final UserRef operator;
    private final UserRef user;
    private final Duration duration;
    private final Instant time;
    public MemberMuteEvent(Bot bot, ConversationRef conversation, UserRef operator, UserRef user, Duration duration, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.operator = operator;
        this.user = user;
        this.duration = duration;
        this.time = time;
    }

    @Override
    public String type() {
        return "member-mute";
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

    public Duration duration() {
        return duration;
    }
}

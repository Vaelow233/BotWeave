package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Duration;
import java.time.Instant;

public class MemberUnmuteEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final UserRef operator;
    private final UserRef user;
    private final Instant time;
    public MemberUnmuteEvent(Bot bot, ConversationRef conversation, UserRef operator, UserRef user, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.operator = operator;
        this.user = user;
        this.time = time;
    }

    @Override
    public String type() {
        return "member-unmute";
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
}

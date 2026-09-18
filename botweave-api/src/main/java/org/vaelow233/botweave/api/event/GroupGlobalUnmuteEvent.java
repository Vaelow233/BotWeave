package org.vaelow233.botweave.api.event;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Duration;
import java.time.Instant;

public class GroupGlobalUnmuteEvent implements BotEvent {
    private final Bot bot;
    private final ConversationRef conversation;
    private final UserRef operator;
    private final Duration duration;
    private final Instant time;
    public GroupGlobalUnmuteEvent(Bot bot, ConversationRef conversation, UserRef operator, Duration duration, Instant time) {
        this.bot = bot;
        this.conversation = conversation;
        this.operator = operator;
        this.duration = duration;
        this.time = time;
    }

    @Override
    public String type() {
        return "group-global-unmute";
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

    public UserRef operator() {
        return operator;
    }

    public Duration duration() {
        return duration;
    }
}

package org.vaelow233.botweave.api.message;

import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.conversation.ConversationRef;

import java.time.Instant;

public class SentMessage {
    private final BotId botId;
    private final ConversationRef conversation;
    private final MessageId id;
    private final Instant acknowledgedAt;
    public SentMessage(BotId botId, ConversationRef conversation, MessageId id, Instant acknowledgedAt) {
        this.botId = botId;
        this.conversation = conversation;
        this.id = id;
        this.acknowledgedAt = acknowledgedAt;
    }

    public BotId botId() {
        return botId;
    }

    public ConversationRef conversation() {
        return conversation;
    }

    public MessageId id() {
        return id;
    }

    public Instant acknowledgedAt() {
        return acknowledgedAt;
    }
}
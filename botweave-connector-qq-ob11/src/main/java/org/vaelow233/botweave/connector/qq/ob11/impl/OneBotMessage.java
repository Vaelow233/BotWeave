package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.message.MessageContent;
import org.vaelow233.botweave.api.message.MessageId;
import org.vaelow233.botweave.api.message.MessageRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Instant;

public class OneBotMessage implements MessageRef {
    private final MessageId id;
    private final UserRef sender;
    private final ConversationRef conversation;
    private final MessageContent content;
    private final Instant timestamp;
    public OneBotMessage(MessageId id, UserRef sender, ConversationRef conversation, MessageContent content, Instant timestamp) {
        this.id = id;
        this.sender = sender;
        this.conversation = conversation;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Override
    public MessageId id() {
        return id;
    }

    @Override
    public UserRef sender() {
        return sender;
    }

    @Override
    public ConversationRef conversation() {
        return conversation;
    }

    @Override
    public MessageContent content() {
        return content;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public BotNetwork network() {
        return BotNetwork.QQ_ONEBOT_11;
    }
}

package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationId;
import org.vaelow233.botweave.api.conversation.ConversationKind;
import org.vaelow233.botweave.api.conversation.ConversationRef;

import java.util.Optional;

public class OneBotConversation implements ConversationRef {
    private final ConversationId id;
    private final ConversationKind kind;
    public OneBotConversation(ConversationId id, ConversationKind kind) {
        this.id = id;
        this.kind = kind;
    }

    @Override
    public ConversationId id() {
        return id;
    }

    @Override
    public ConversationKind kind() {
        return kind;
    }

    @Override
    public BotNetwork network() {
        return BotNetwork.QQ_ONEBOT_11;
    }

    @Override
    public Optional<ConversationRef> parent() {
        return Optional.empty();
    }
}

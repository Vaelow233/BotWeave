package org.vaelow233.botweave.api.conversation;

import org.vaelow233.botweave.api.bot.BotNetwork;

import java.util.Optional;

public interface ConversationRef {
    ConversationId id();
    ConversationKind kind();
    BotNetwork network();
    Optional<ConversationRef> parent();
}

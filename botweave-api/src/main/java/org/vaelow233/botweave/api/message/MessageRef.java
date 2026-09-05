package org.vaelow233.botweave.api.message;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

import java.time.Instant;

public interface MessageRef {
    MessageId id();
    UserRef sender();
    ConversationRef conversation();
    MessageContent content();
    Instant timestamp();
    BotNetwork network();
}

package org.vaelow233.botweave.api.conversation.group;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;

public interface GroupProfile {
    ConversationRef conversation();
    String name();
    long memberCount();
    BotNetwork network();
}

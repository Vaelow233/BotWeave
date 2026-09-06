package org.vaelow233.botweave.api.conversation.group;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.user.UserRef;

public interface MemberProfile {
    ConversationRef conversation();
    UserRef user();
    String name();
    String displayName();
    BotNetwork network();
}

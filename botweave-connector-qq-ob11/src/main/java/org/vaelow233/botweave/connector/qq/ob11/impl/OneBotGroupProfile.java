package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.GroupProfile;

public class OneBotGroupProfile implements GroupProfile {
    private final ConversationRef conversation;
    private final String groupName;
    private final long memberCount;

    public OneBotGroupProfile(ConversationRef conversation, String groupName, long memberCount) {
        this.conversation = conversation;
        this.groupName = groupName;
        this.memberCount = memberCount;
    }

    @Override
    public ConversationRef conversation() {
        return conversation;
    }

    @Override
    public String name() {
        return groupName;
    }

    @Override
    public long memberCount() {
        return memberCount;
    }

    @Override
    public BotNetwork network() {
        return BotNetwork.QQ_ONEBOT_11;
    }
}

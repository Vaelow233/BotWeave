package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.MemberProfile;
import org.vaelow233.botweave.api.user.UserRef;

public class OneBotMemberProfile implements MemberProfile {
    private final ConversationRef conversation;
    private final UserRef user;
    private final String name;
    private final String displayName;
    public OneBotMemberProfile(ConversationRef conversation, UserRef user, String name, String displayName) {
        this.conversation = conversation;
        this.user = user;
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public ConversationRef conversation() {
        return conversation;
    }

    @Override
    public UserRef user() {
        return user;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public BotNetwork network() {
        return BotNetwork.QQ_ONEBOT_11;
    }
}

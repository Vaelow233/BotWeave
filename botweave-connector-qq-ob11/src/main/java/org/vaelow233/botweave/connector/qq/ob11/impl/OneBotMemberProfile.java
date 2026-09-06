package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.conversation.ConversationRef;
import org.vaelow233.botweave.api.conversation.group.MemberProfile;
import org.vaelow233.botweave.api.conversation.group.MemberRole;
import org.vaelow233.botweave.api.user.UserRef;

import java.util.Optional;

public class OneBotMemberProfile implements MemberProfile {
    private final ConversationRef conversation;
    private final UserRef user;
    private final String name;
    private final String displayName;
    private final Optional<MemberRole> role;
    public OneBotMemberProfile(ConversationRef conversation, UserRef user, String name, String displayName, Optional<MemberRole> role) {
        this.conversation = conversation;
        this.user = user;
        this.name = name;
        this.displayName = displayName;
        this.role = role;
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

    @Override
    public Optional<MemberRole> role() {
        return role;
    }
}

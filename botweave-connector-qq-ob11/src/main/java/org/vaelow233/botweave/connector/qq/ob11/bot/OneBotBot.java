package org.vaelow233.botweave.connector.qq.ob11.bot;

import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.bot.BotState;
import org.vaelow233.botweave.api.capability.*;
import org.vaelow233.botweave.api.util.CollectionUtil;
import org.vaelow233.botweave.core.bot.AbstractBot;

import java.util.Collections;
import java.util.Set;

public class OneBotBot extends AbstractBot {
    private volatile BotState state = new BotState(false);

    public OneBotBot(BotId id, Messaging messaging, GroupModeration groupModeration, GroupQuery groupQuery, MemberQuery memberQuery) {
        super(id, BotNetwork.QQ_ONEBOT_11);
        registerCapability(Messaging.class, messaging);
        registerCapability(GroupModeration.class, groupModeration);
        registerCapability(GroupQuery.class, groupQuery);
        registerCapability(MemberQuery.class, memberQuery);
    }

    @Override
    public BotState state() {
        return state;
    }

    @Override
    public Set<String> capabilities() {
        return CollectionUtil.ofSet("messaging", "group-moderation", "group-query", "member-query");
    }

    public void setOperational(boolean operational) {
        state = new BotState(operational);
    }
}

package org.vaelow233.botweave.connector.qq.ob11.bot;

import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.bot.BotState;
import org.vaelow233.botweave.api.capability.Messaging;
import org.vaelow233.botweave.core.bot.AbstractBot;

import java.util.Collections;
import java.util.Set;

public class OneBotBot extends AbstractBot {
    private volatile BotState state = new BotState(false);

    public OneBotBot(BotId id, Messaging messaging) {
        super(id, BotNetwork.QQ_ONEBOT_11);
        registerCapability(Messaging.class, messaging);
    }

    @Override
    public BotState state() {
        return state;
    }

    @Override
    public Set<String> capabilities() {
        return Collections.singleton("messaging");
    }

    public void setOperational(boolean operational) {
        state = new BotState(operational);
    }
}

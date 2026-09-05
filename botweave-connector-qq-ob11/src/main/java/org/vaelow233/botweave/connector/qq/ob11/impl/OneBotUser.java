package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.user.UserId;
import org.vaelow233.botweave.api.user.UserRef;

public class OneBotUser implements UserRef {
    private final UserId id;
    public OneBotUser(UserId id) {
        this.id = id;
    }

    @Override
    public UserId id() {
        return id;
    }

    @Override
    public BotNetwork network() {
        return BotNetwork.QQ_ONEBOT_11;
    }
}

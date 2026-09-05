package org.vaelow233.botweave.api.user;

import org.vaelow233.botweave.api.bot.BotNetwork;

public interface UserRef {
    UserId id();
    BotNetwork network();
}

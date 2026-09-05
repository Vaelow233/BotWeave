package org.vaelow233.botweave.api.user;

import org.vaelow233.botweave.api.bot.BotNetwork;

public interface UserProfile {
    UserId id();
    String nickname();
    String avatarUrl();
    BotNetwork network();
}

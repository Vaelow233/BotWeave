package org.vaelow233.botweave.api.user;

import org.vaelow233.botweave.api.bot.BotNetwork;

import java.util.Optional;

public interface UserProfile {
    UserId id();
    String nickname();
    UserSex sex();
    Optional<String> avatarUrl();
    BotNetwork network();
}

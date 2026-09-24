package org.vaelow233.botweave.connector.qq.ob11.impl;

import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.user.UserId;
import org.vaelow233.botweave.api.user.UserProfile;
import org.vaelow233.botweave.api.user.UserRef;
import org.vaelow233.botweave.api.user.UserSex;

import java.util.Optional;

public class OneBotUserProfile implements UserProfile {
    private final UserRef user;
    private final String name;
    private final UserSex sex;
    private final Optional<String> avatarUrl;

    public OneBotUserProfile(UserRef user, String name, UserSex sex, Optional<String> avatarUrl) {
        this.user = user;
        this.name = name;
        this.sex = sex;
        this.avatarUrl = avatarUrl;
    }

    @Override
    public UserId id() {
        return user.id();
    }

    @Override
    public String nickname() {
        return name;
    }

    @Override
    public UserSex sex() {
        return sex;
    }

    @Override
    public Optional<String> avatarUrl() {
        return avatarUrl;
    }

    @Override
    public BotNetwork network() {
        return user.network();
    }
}

package org.vaelow233.botweave.api.capability;

import org.vaelow233.botweave.api.user.UserProfile;
import org.vaelow233.botweave.api.user.UserRef;

import java.util.concurrent.CompletionStage;

public interface UserQuery extends Capability {
    CompletionStage<UserProfile> getUser(UserRef user);

    default String type() {
        return "user-query";
    }
}

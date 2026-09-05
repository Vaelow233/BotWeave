package org.vaelow233.botweave.api.message.element;

import org.vaelow233.botweave.api.user.UserRef;

public class MentionElement implements MessageElement {
    private final UserRef user;
    public MentionElement(UserRef user) {
        this.user = user;
    }

    @Override
    public String type() {
        return "mention";
    }

    @Override
    public String plainString() {
        return "@" + user.id().value();
    }

    public UserRef user() {
        return this.user;
    }
}

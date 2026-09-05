package org.vaelow233.botweave.api.message.element;

public class MentionAllElement implements MessageElement {
    public MentionAllElement() {

    }

    @Override
    public String type() {
        return "mention-all";
    }

    @Override
    public String plainString() {
        return "@all";
    }
}

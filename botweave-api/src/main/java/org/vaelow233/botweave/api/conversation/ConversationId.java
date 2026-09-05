package org.vaelow233.botweave.api.conversation;

public class ConversationId {
    private final String value;
    public ConversationId(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConversationId)) {
            return false;
        }
        ConversationId that = (ConversationId) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}

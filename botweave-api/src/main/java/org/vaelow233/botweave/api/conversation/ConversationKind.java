package org.vaelow233.botweave.api.conversation;

public class ConversationKind {
    public static final ConversationKind PRIVATE = new ConversationKind("private");
    public static final ConversationKind GROUP = new ConversationKind("group");
    public static final ConversationKind CHANNEL = new ConversationKind("channel");

    private final String value;
    public ConversationKind(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConversationKind)) {
            return false;
        }
        ConversationKind that = (ConversationKind) other;
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

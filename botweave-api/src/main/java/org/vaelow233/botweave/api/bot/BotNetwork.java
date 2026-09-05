package org.vaelow233.botweave.api.bot;

public class BotNetwork {
    public static final BotNetwork QQ_ONEBOT_11 = new BotNetwork("qq-onebot-11");

    private final String value;
    public BotNetwork(String value) {
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
        if (!(other instanceof BotNetwork)) {
            return false;
        }
        BotNetwork that = (BotNetwork) other;
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

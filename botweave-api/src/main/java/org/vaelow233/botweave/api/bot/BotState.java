package org.vaelow233.botweave.api.bot;

public class BotState {
    private final boolean operational;
    public BotState(boolean operational) {
        this.operational = operational;
    }

    public boolean operational() {
        return operational;
    }
}

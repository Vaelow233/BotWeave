package org.vaelow233.botweave.core.bot;

import org.vaelow233.botweave.api.bot.Bot;
import org.vaelow233.botweave.api.bot.BotId;
import org.vaelow233.botweave.api.bot.BotNetwork;
import org.vaelow233.botweave.api.capability.Capability;
import org.vaelow233.botweave.core.capability.CapabilityRegistry;
import org.vaelow233.botweave.core.capability.SimpleCapabilityRegistry;

import java.util.Optional;

public abstract class AbstractBot implements Bot {
    private final BotId id;
    private final BotNetwork network;
    private final CapabilityRegistry capabilities = new SimpleCapabilityRegistry();

    protected AbstractBot(BotId id, BotNetwork network) {
        this.id = id;
        this.network = network;
    }

    @Override
    public BotId id() {
        return id;
    }

    @Override
    public BotNetwork network() {
        return network;
    }

    @Override
    public <T extends Capability> Optional<T> capability(Class<T> type) {
        return capabilities.find(type);
    }

    protected <T extends Capability> void registerCapability(Class<T> type, T capability) {
        capabilities.register(type, capability);
    }
}
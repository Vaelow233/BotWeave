package org.vaelow233.botweave.api.bot;

import org.vaelow233.botweave.api.capability.Capability;

import java.util.Optional;
import java.util.Set;

public interface Bot {
    BotId id();
    BotNetwork network();
    BotState state();
    Set<String> capabilities();
    <T extends Capability> Optional<T> capability(Class<T> type);
}

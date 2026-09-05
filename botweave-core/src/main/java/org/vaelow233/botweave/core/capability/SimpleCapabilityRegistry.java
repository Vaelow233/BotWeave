package org.vaelow233.botweave.core.capability;

import org.vaelow233.botweave.api.capability.Capability;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleCapabilityRegistry implements CapabilityRegistry {
    private final ConcurrentMap<Class<? extends Capability>, Capability> capabilities = new ConcurrentHashMap<>();

    @Override
    public <T extends Capability> void register(Class<T> type, T capability) {
        capabilities.put(type, capability);
    }

    @Override
    public <T extends Capability> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(type.cast(capabilities.get(type)));
    }
}

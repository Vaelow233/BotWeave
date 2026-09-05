package org.vaelow233.botweave.core.capability;

import org.vaelow233.botweave.api.capability.Capability;

import java.util.Optional;

public interface CapabilityRegistry {
    <T extends Capability> void register(Class<T> type, T capability);
    <T extends Capability> Optional<T> find(Class<T> type);
}

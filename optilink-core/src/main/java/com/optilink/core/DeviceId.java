package com.optilink.core;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable device identity visible to the application layer.
 *
 * @param value UUID backing the device identifier
 */
public record DeviceId(UUID value) {

    /**
     * Creates a device identifier.
     *
     * @param value UUID backing the identifier
     */
    public DeviceId {
        Objects.requireNonNull(value, "value");
    }

    /**
     * Generates a new device identifier using the provided entropy source.
     *
     * @param entropySource entropy provider
     * @return generated device identifier
     */
    public static DeviceId generate(EntropySource entropySource) {
        return new DeviceId(entropySource.randomUuid());
    }
}

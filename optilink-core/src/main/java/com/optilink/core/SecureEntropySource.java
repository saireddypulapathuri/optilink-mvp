package com.optilink.core;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

/**
 * SecureRandom-backed entropy source.
 */
public final class SecureEntropySource implements EntropySource {
    private final SecureRandom secureRandom;

    /**
     * Creates an entropy source with the platform default secure random provider.
     */
    public SecureEntropySource() {
        this(new SecureRandom());
    }

    /**
     * Creates an entropy source backed by the provided secure random.
     *
     * @param secureRandom secure random provider
     */
    public SecureEntropySource(SecureRandom secureRandom) {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
    }

    @Override
    public byte[] randomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be non-negative");
        }
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    @Override
    public UUID randomUuid() {
        return new UUID(secureRandom.nextLong(), secureRandom.nextLong());
    }
}

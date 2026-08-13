package com.optilink.core;

import java.util.UUID;

/**
 * Supplies cryptographically strong randomness to application services.
 */
public interface EntropySource {

    /**
     * Returns random bytes.
     *
     * @param length number of bytes to return
     * @return random bytes
     */
    byte[] randomBytes(int length);

    /**
     * Returns a random UUID.
     *
     * @return random UUID
     */
    UUID randomUuid();
}

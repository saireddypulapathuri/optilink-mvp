package com.optilink.optical;

import java.util.Arrays;
import java.util.Objects;

/**
 * Encoded optical symbol ready to be rendered by a display adapter.
 *
 * @param profile profile used to produce the symbol
 * @param frameBytes encoded protocol frame bytes
 */
public record OpticalSymbol(OpticalProfile profile, byte[] frameBytes) {

    /**
     * Creates an optical symbol.
     */
    public OpticalSymbol {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(frameBytes, "frameBytes");
        frameBytes = frameBytes.clone();
    }

    @Override
    public byte[] frameBytes() {
        return frameBytes.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof OpticalSymbol other
                && profile.equals(other.profile)
                && Arrays.equals(frameBytes, other.frameBytes);
    }

    @Override
    public int hashCode() {
        return 31 * profile.hashCode() + Arrays.hashCode(frameBytes);
    }
}

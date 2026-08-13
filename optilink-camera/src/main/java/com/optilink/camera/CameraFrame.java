package com.optilink.camera;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * Raw camera frame supplied by a platform adapter.
 *
 * @param pixels encoded pixel bytes in adapter-defined format
 * @param width frame width in pixels
 * @param height frame height in pixels
 * @param capturedAt capture timestamp
 */
public record CameraFrame(byte[] pixels, int width, int height, Instant capturedAt) {

    /**
     * Creates a camera frame.
     */
    public CameraFrame {
        Objects.requireNonNull(pixels, "pixels");
        Objects.requireNonNull(capturedAt, "capturedAt");
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("frame dimensions must be positive");
        }
        pixels = pixels.clone();
    }

    @Override
    public byte[] pixels() {
        return pixels.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof CameraFrame other
                && width == other.width
                && height == other.height
                && capturedAt.equals(other.capturedAt)
                && Arrays.equals(pixels, other.pixels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, capturedAt, Arrays.hashCode(pixels));
    }
}

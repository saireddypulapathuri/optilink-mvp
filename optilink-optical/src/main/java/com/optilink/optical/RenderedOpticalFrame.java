package com.optilink.optical;

import java.util.Arrays;
import java.util.Objects;

/**
 * Raster frame generated for display by an optical renderer.
 *
 * @param profile optical profile used for rendering
 * @param mimeType media type of the encoded image
 * @param width frame width in pixels
 * @param height frame height in pixels
 * @param imageBytes encoded image bytes
 */
public record RenderedOpticalFrame(
        OpticalProfile profile,
        String mimeType,
        int width,
        int height,
        byte[] imageBytes) {

    /**
     * Creates a rendered optical frame.
     */
    public RenderedOpticalFrame {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(mimeType, "mimeType");
        Objects.requireNonNull(imageBytes, "imageBytes");
        if (width < 1 || height < 1 || imageBytes.length == 0) {
            throw new IllegalArgumentException("rendered frame dimensions and bytes must be non-empty");
        }
        imageBytes = imageBytes.clone();
    }

    @Override
    public byte[] imageBytes() {
        return imageBytes.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof RenderedOpticalFrame other
                && profile.equals(other.profile)
                && mimeType.equals(other.mimeType)
                && width == other.width
                && height == other.height
                && Arrays.equals(imageBytes, other.imageBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profile, mimeType, width, height, Arrays.hashCode(imageBytes));
    }
}

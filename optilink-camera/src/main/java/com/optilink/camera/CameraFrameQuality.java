package com.optilink.camera;

/**
 * Quality metrics derived from a camera luminance frame.
 *
 * @param brightness normalized mean brightness
 * @param contrast normalized luminance standard deviation
 * @param blurScore normalized blur estimate where 0 is sharp and 1 is blurred
 */
public record CameraFrameQuality(double brightness, double contrast, double blurScore) {

    /**
     * Creates frame quality metrics.
     */
    public CameraFrameQuality {
        validateRatio("brightness", brightness);
        validateRatio("contrast", contrast);
        validateRatio("blurScore", blurScore);
    }

    private static void validateRatio(String name, double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be within [0, 1]");
        }
    }
}

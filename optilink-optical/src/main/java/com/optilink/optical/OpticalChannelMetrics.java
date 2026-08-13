package com.optilink.optical;

/**
 * Receiver-observed optical channel metrics.
 *
 * @param cameraFps measured camera frames per second
 * @param decodeSuccessRatio decoded symbols divided by observed symbols
 * @param blurScore normalized blur score where 0 is sharp and 1 is unusable
 * @param brightness normalized brightness where 0 is black and 1 is saturated white
 * @param roundTripMillis measured round-trip latency in milliseconds
 */
public record OpticalChannelMetrics(
        double cameraFps,
        double decodeSuccessRatio,
        double blurScore,
        double brightness,
        long roundTripMillis) {

    /**
     * Creates channel metrics.
     */
    public OpticalChannelMetrics {
        if (cameraFps <= 0.0 || roundTripMillis < 0) {
            throw new IllegalArgumentException("cameraFps must be positive and RTT non-negative");
        }
        validateRatio("decodeSuccessRatio", decodeSuccessRatio);
        validateRatio("blurScore", blurScore);
        validateRatio("brightness", brightness);
    }

    private static void validateRatio(String name, double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be within [0, 1]");
        }
    }
}

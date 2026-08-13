package com.optilink.optical;

/**
 * Selects conservative, balanced, or throughput profiles from receiver feedback.
 */
public final class AdaptiveOpticalProfileSelector {
    private static final double STABLE_DECODE_RATIO = 0.97;
    private static final double POOR_DECODE_RATIO = 0.85;
    private static final double HIGH_BLUR_SCORE = 0.35;
    private static final double LOW_CAMERA_FPS = 20.0;

    private final OpticalProfile robust = new OpticalProfile("robust", 6, 8, ErrorCorrectionLevel.H, 80, 256);
    private final OpticalProfile balanced = new OpticalProfile("balanced", 10, 12, ErrorCorrectionLevel.Q, 85, 512);
    private final OpticalProfile throughput = new OpticalProfile("throughput", 15, 18, ErrorCorrectionLevel.M, 90, 1024);

    /**
     * Selects an optical profile appropriate for the observed channel.
     *
     * @param metrics channel metrics
     * @return selected profile
     */
    public OpticalProfile select(OpticalChannelMetrics metrics) {
        if (needsRobustProfile(metrics)) {
            return robust;
        }
        if (isStable(metrics)) {
            return throughput;
        }
        return balanced;
    }

    private boolean needsRobustProfile(OpticalChannelMetrics metrics) {
        return metrics.decodeSuccessRatio() < POOR_DECODE_RATIO
                || metrics.blurScore() > HIGH_BLUR_SCORE
                || metrics.cameraFps() < LOW_CAMERA_FPS;
    }

    private boolean isStable(OpticalChannelMetrics metrics) {
        return metrics.decodeSuccessRatio() >= STABLE_DECODE_RATIO
                && metrics.blurScore() < HIGH_BLUR_SCORE
                && metrics.cameraFps() >= LOW_CAMERA_FPS;
    }
}

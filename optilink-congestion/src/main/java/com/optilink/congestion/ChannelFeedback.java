package com.optilink.congestion;

/**
 * Loss and latency feedback from the optical receive path.
 *
 * @param decodeSuccessRatio ratio of optical symbols decoded successfully
 * @param roundTripMillis measured round-trip latency in milliseconds
 */
public record ChannelFeedback(double decodeSuccessRatio, long roundTripMillis) {

    /**
     * Creates channel feedback.
     */
    public ChannelFeedback {
        if (decodeSuccessRatio < 0.0 || decodeSuccessRatio > 1.0) {
            throw new IllegalArgumentException("decodeSuccessRatio must be within [0, 1]");
        }
        if (roundTripMillis < 0) {
            throw new IllegalArgumentException("roundTripMillis must be non-negative");
        }
    }
}

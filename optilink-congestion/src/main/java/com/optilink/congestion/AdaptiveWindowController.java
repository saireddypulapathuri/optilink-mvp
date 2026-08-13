package com.optilink.congestion;

import java.util.Objects;

/**
 * AIMD-style congestion controller tuned for slow, lossy optical channels.
 */
public final class AdaptiveWindowController implements CongestionController {
    private static final double POOR_DECODE_RATIO = 0.85;
    private static final long HIGH_RTT_MILLIS = 750;
    private final int minimumWindow;
    private final int maximumWindow;
    private int currentWindow;
    private int acknowledgementsSinceGrowth;

    /**
     * Creates an adaptive congestion controller.
     *
     * @param minimumWindow minimum in-flight packet count
     * @param initialWindow initial in-flight packet count
     * @param maximumWindow maximum in-flight packet count
     */
    public AdaptiveWindowController(int minimumWindow, int initialWindow, int maximumWindow) {
        if (minimumWindow < 1 || initialWindow < minimumWindow || maximumWindow < initialWindow) {
            throw new IllegalArgumentException("invalid congestion window bounds");
        }
        this.minimumWindow = minimumWindow;
        this.maximumWindow = maximumWindow;
        this.currentWindow = initialWindow;
    }

    @Override
    public synchronized int windowSize() {
        return currentWindow;
    }

    @Override
    public synchronized void onAcknowledgement() {
        acknowledgementsSinceGrowth++;
        if (acknowledgementsSinceGrowth >= currentWindow && currentWindow < maximumWindow) {
            currentWindow++;
            acknowledgementsSinceGrowth = 0;
        }
    }

    @Override
    public synchronized void onLoss() {
        currentWindow = Math.max(minimumWindow, Math.max(currentWindow / 2, 1));
        acknowledgementsSinceGrowth = 0;
    }

    @Override
    public synchronized void onFeedback(ChannelFeedback feedback) {
        Objects.requireNonNull(feedback, "feedback");
        if (feedback.decodeSuccessRatio() < POOR_DECODE_RATIO || feedback.roundTripMillis() > HIGH_RTT_MILLIS) {
            onLoss();
        }
    }
}

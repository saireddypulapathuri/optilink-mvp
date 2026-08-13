package com.optilink.congestion;

/**
 * Controls the number of in-flight packets allowed by the transport.
 */
public interface CongestionController {

    /**
     * Returns the current congestion window in packets.
     *
     * @return window size
     */
    int windowSize();

    /**
     * Records a newly acknowledged packet.
     */
    void onAcknowledgement();

    /**
     * Records a packet loss signal.
     */
    void onLoss();

    /**
     * Applies optical channel feedback.
     *
     * @param feedback channel feedback
     */
    void onFeedback(ChannelFeedback feedback);
}

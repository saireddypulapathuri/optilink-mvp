package com.optilink.protocol;

import java.util.List;
import java.util.Objects;

/**
 * Selective acknowledgement frame used by the reliable transport layer.
 *
 * @param cumulativeAck highest contiguous sequence number received
 * @param ranges additional non-contiguous acknowledged ranges
 */
public record SelectiveAckFrame(long cumulativeAck, List<AckRange> ranges) {

    /**
     * Creates a selective acknowledgement frame.
     */
    public SelectiveAckFrame {
        Objects.requireNonNull(ranges, "ranges");
        if (cumulativeAck < 0) {
            throw new IllegalArgumentException("cumulativeAck must be non-negative");
        }
        ranges = List.copyOf(ranges);
    }

    /**
     * Returns whether this frame acknowledges the sequence number.
     *
     * @param sequenceNumber sequence number to test
     * @return true when acknowledged
     */
    public boolean acknowledges(long sequenceNumber) {
        if (sequenceNumber <= cumulativeAck) {
            return true;
        }
        return ranges.stream().anyMatch(range -> range.contains(sequenceNumber));
    }
}

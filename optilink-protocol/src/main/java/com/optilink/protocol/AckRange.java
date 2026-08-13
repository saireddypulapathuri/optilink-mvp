package com.optilink.protocol;

/**
 * Inclusive range of received packet sequence numbers above the cumulative ACK.
 *
 * @param startInclusive first acknowledged sequence number
 * @param endInclusive last acknowledged sequence number
 */
public record AckRange(long startInclusive, long endInclusive) {

    /**
     * Creates a selective acknowledgement range.
     */
    public AckRange {
        if (startInclusive < 0 || endInclusive < startInclusive) {
            throw new IllegalArgumentException("invalid acknowledgement range");
        }
    }

    /**
     * Returns whether the sequence number is inside this range.
     *
     * @param sequenceNumber sequence number to test
     * @return true when acknowledged by this range
     */
    public boolean contains(long sequenceNumber) {
        return sequenceNumber >= startInclusive && sequenceNumber <= endInclusive;
    }
}

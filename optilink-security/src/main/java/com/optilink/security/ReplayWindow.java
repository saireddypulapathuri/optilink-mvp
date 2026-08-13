package com.optilink.security;

import java.util.BitSet;

/**
 * Sliding replay protection window for packet sequence numbers.
 */
public final class ReplayWindow {
    private final int windowSize;
    private final BitSet received;
    private long largestSeen = -1;

    /**
     * Creates a replay window.
     *
     * @param windowSize number of sequence numbers retained
     */
    public ReplayWindow(int windowSize) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("windowSize must be positive");
        }
        this.windowSize = windowSize;
        this.received = new BitSet(windowSize);
    }

    /**
     * Records a sequence number if it has not been seen before.
     *
     * @param sequenceNumber sequence number to accept
     * @return true when the sequence number is fresh
     */
    public synchronized boolean accept(long sequenceNumber) {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("sequenceNumber must be non-negative");
        }
        if (largestSeen < 0 || sequenceNumber > largestSeen) {
            acceptNewLargest(sequenceNumber);
            return true;
        }
        return acceptWithinWindow(sequenceNumber);
    }

    private void acceptNewLargest(long sequenceNumber) {
        long distance = largestSeen < 0 ? windowSize : sequenceNumber - largestSeen;
        shift(distance);
        largestSeen = sequenceNumber;
        received.set(0);
    }

    private boolean acceptWithinWindow(long sequenceNumber) {
        long offset = largestSeen - sequenceNumber;
        if (offset >= windowSize || received.get((int) offset)) {
            return false;
        }
        received.set((int) offset);
        return true;
    }

    private void shift(long distance) {
        if (distance >= windowSize) {
            received.clear();
            return;
        }
        for (int index = windowSize - 1; index >= distance; index--) {
            received.set(index, received.get((int) (index - distance)));
        }
        received.clear(0, (int) distance);
    }
}

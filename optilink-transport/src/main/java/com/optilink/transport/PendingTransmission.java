package com.optilink.transport;

import com.optilink.protocol.OpticalPacket;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Packet currently occupying the sender's in-flight window.
 *
 * @param packet packet to transmit
 * @param lastSentAt last transmission timestamp
 * @param attempts number of transmission attempts
 */
public record PendingTransmission(OpticalPacket packet, Instant lastSentAt, int attempts) {

    /**
     * Creates a pending transmission.
     */
    public PendingTransmission {
        Objects.requireNonNull(packet, "packet");
        Objects.requireNonNull(lastSentAt, "lastSentAt");
        if (attempts < 1) {
            throw new IllegalArgumentException("attempts must be positive");
        }
    }

    /**
     * Returns a copy marked as transmitted at the provided time.
     *
     * @param now transmission time
     * @return updated transmission state
     */
    public PendingTransmission retransmittedAt(Instant now) {
        return new PendingTransmission(packet, now, attempts + 1);
    }

    /**
     * Returns whether the packet has exceeded its retransmission timeout.
     *
     * @param now current time
     * @param timeout retransmission timeout
     * @return true when retransmission is due
     */
    public boolean isExpired(Instant now, Duration timeout) {
        return !lastSentAt.plus(timeout).isAfter(now);
    }
}

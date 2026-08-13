package com.optilink.transport;

import java.time.Duration;
import java.util.Objects;

/**
 * Tunable transport parameters for an OptiLink session.
 *
 * @param maxPayloadBytes maximum payload bytes in a DATA packet
 * @param retransmissionTimeout timeout before retransmission
 */
public record TransportParameters(int maxPayloadBytes, Duration retransmissionTimeout) {

    /**
     * Creates validated transport parameters.
     */
    public TransportParameters {
        Objects.requireNonNull(retransmissionTimeout, "retransmissionTimeout");
        if (maxPayloadBytes < 1) {
            throw new IllegalArgumentException("maxPayloadBytes must be positive");
        }
        if (retransmissionTimeout.isNegative() || retransmissionTimeout.isZero()) {
            throw new IllegalArgumentException("retransmissionTimeout must be positive");
        }
    }
}

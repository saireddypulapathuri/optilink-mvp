package com.optilink.transport;

import com.optilink.core.BinaryPayload;
import com.optilink.protocol.AckRange;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketType;
import com.optilink.protocol.SelectiveAckFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Reliable receiver that de-duplicates, buffers out-of-order packets, and emits SACK state.
 */
public final class ReliableTransportReceiver {
    private final TreeMap<Long, BinaryPayload> pending = new TreeMap<>();
    private final NavigableSet<Long> receivedAboveAck = new TreeSet<>();
    private long nextExpectedSequence = 1;

    /**
     * Accepts a DATA packet and returns newly ordered payloads.
     *
     * @param packet packet from the peer
     * @return payloads released in sequence order
     */
    public synchronized List<BinaryPayload> receive(OpticalPacket packet) {
        Objects.requireNonNull(packet, "packet");
        if (packet.header().type() != PacketType.DATA) {
            throw new IllegalArgumentException("receiver accepts DATA packets only");
        }
        long sequenceNumber = packet.header().sequenceNumber();
        if (sequenceNumber < nextExpectedSequence) {
            return List.of();
        }
        pending.putIfAbsent(sequenceNumber, packet.payload());
        receivedAboveAck.add(sequenceNumber);
        return drainOrdered();
    }

    /**
     * Builds the current selective acknowledgement state.
     *
     * @return acknowledgement frame
     */
    public synchronized SelectiveAckFrame acknowledgement() {
        return new SelectiveAckFrame(nextExpectedSequence - 1, buildRanges());
    }

    private List<BinaryPayload> drainOrdered() {
        List<BinaryPayload> ordered = new ArrayList<>();
        while (pending.containsKey(nextExpectedSequence)) {
            ordered.add(pending.remove(nextExpectedSequence));
            receivedAboveAck.remove(nextExpectedSequence);
            nextExpectedSequence++;
        }
        return List.copyOf(ordered);
    }

    private List<AckRange> buildRanges() {
        List<AckRange> ranges = new ArrayList<>();
        Long rangeStart = null;
        Long previous = null;
        for (Long sequenceNumber : receivedAboveAck) {
            if (sequenceNumber < nextExpectedSequence) {
                continue;
            }
            if (rangeStart == null) {
                rangeStart = sequenceNumber;
            } else if (sequenceNumber != previous + 1) {
                ranges.add(new AckRange(rangeStart, previous));
                rangeStart = sequenceNumber;
            }
            previous = sequenceNumber;
        }
        if (rangeStart != null) {
            ranges.add(new AckRange(rangeStart, previous));
        }
        return ranges;
    }
}

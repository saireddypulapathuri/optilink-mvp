package com.optilink.transport;

import com.optilink.congestion.CongestionController;
import com.optilink.core.BinaryPayload;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import com.optilink.protocol.SelectiveAckFrame;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Reliable sender using sliding-window scheduling, SACK processing, and timeout retransmission.
 */
public final class ReliableTransportSender {
    private final UUID sessionId;
    private final CongestionController congestionController;
    private final TransportParameters parameters;
    private final Queue<BinaryPayload> backlog = new ArrayDeque<>();
    private final NavigableMap<Long, PendingTransmission> inFlight = new TreeMap<>();
    private long nextSequenceNumber = 1;
    private long lastCumulativeAck;
    private int duplicateAckCount;

    /**
     * Creates a reliable sender.
     *
     * @param sessionId session identifier
     * @param congestionController congestion controller
     * @param parameters transport parameters
     */
    public ReliableTransportSender(
            UUID sessionId,
            CongestionController congestionController,
            TransportParameters parameters) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.congestionController = Objects.requireNonNull(congestionController, "congestionController");
        this.parameters = Objects.requireNonNull(parameters, "parameters");
    }

    /**
     * Enqueues payload bytes, chunking them into transport-sized packets.
     *
     * @param payload payload to send
     */
    public synchronized void enqueue(BinaryPayload payload) {
        byte[] bytes = Objects.requireNonNull(payload, "payload").bytes();
        if (bytes.length == 0) {
            backlog.add(payload);
            return;
        }
        for (int offset = 0; offset < bytes.length; offset += parameters.maxPayloadBytes()) {
            int length = Math.min(parameters.maxPayloadBytes(), bytes.length - offset);
            backlog.add(new BinaryPayload(copyRange(bytes, offset, length)));
        }
    }

    /**
     * Returns packets ready for transmission at the provided instant.
     *
     * @param now current time
     * @return packets to transmit now
     */
    public synchronized List<OpticalPacket> pollReady(Instant now) {
        Objects.requireNonNull(now, "now");
        List<OpticalPacket> packets = new ArrayList<>();
        collectExpiredRetransmissions(now, packets);
        fillOpenWindow(now, packets);
        return List.copyOf(packets);
    }

    /**
     * Processes selective acknowledgement feedback from the peer.
     *
     * @param acknowledgement acknowledgement frame
     */
    public synchronized void onAcknowledgement(SelectiveAckFrame acknowledgement) {
        Objects.requireNonNull(acknowledgement, "acknowledgement");
        updateDuplicateAckState(acknowledgement);
        List<Long> acknowledged = acknowledgedSequences(acknowledgement);
        acknowledged.forEach(this::removeAcknowledged);
    }

    /**
     * Returns the number of in-flight packets.
     *
     * @return in-flight packet count
     */
    public synchronized int inFlightCount() {
        return inFlight.size();
    }

    /**
     * Returns whether there is no queued or in-flight data.
     *
     * @return true when idle
     */
    public synchronized boolean isIdle() {
        return backlog.isEmpty() && inFlight.isEmpty();
    }

    private void collectExpiredRetransmissions(Instant now, List<OpticalPacket> packets) {
        for (Long sequenceNumber : new ArrayList<>(inFlight.keySet())) {
            PendingTransmission pending = inFlight.get(sequenceNumber);
            if (pending.isExpired(now, parameters.retransmissionTimeout())) {
                congestionController.onLoss();
                PendingTransmission updated = pending.retransmittedAt(now);
                inFlight.put(sequenceNumber, updated);
                packets.add(updated.packet());
            }
        }
    }

    private void fillOpenWindow(Instant now, List<OpticalPacket> packets) {
        while (inFlight.size() < congestionController.windowSize() && !backlog.isEmpty()) {
            OpticalPacket packet = createPacket(backlog.remove());
            inFlight.put(packet.header().sequenceNumber(), new PendingTransmission(packet, now, 1));
            packets.add(packet);
        }
    }

    private OpticalPacket createPacket(BinaryPayload payload) {
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                sessionId,
                nextSequenceNumber++,
                0,
                payload.length(),
                0);
        return new OpticalPacket(header, payload);
    }

    private void updateDuplicateAckState(SelectiveAckFrame acknowledgement) {
        if (acknowledgement.cumulativeAck() == lastCumulativeAck) {
            duplicateAckCount++;
            triggerFastRetransmitWhenNeeded();
            return;
        }
        lastCumulativeAck = acknowledgement.cumulativeAck();
        duplicateAckCount = 0;
    }

    private void triggerFastRetransmitWhenNeeded() {
        if (duplicateAckCount >= 3 && !inFlight.isEmpty()) {
            Long firstUnacked = inFlight.firstKey();
            PendingTransmission pending = inFlight.get(firstUnacked);
            inFlight.put(firstUnacked, pending.retransmittedAt(Instant.EPOCH));
            congestionController.onLoss();
            duplicateAckCount = 0;
        }
    }

    private List<Long> acknowledgedSequences(SelectiveAckFrame acknowledgement) {
        List<Long> acknowledged = new ArrayList<>();
        for (Long sequenceNumber : inFlight.keySet()) {
            if (acknowledgement.acknowledges(sequenceNumber)) {
                acknowledged.add(sequenceNumber);
            }
        }
        return acknowledged;
    }

    private void removeAcknowledged(Long sequenceNumber) {
        if (inFlight.remove(sequenceNumber) != null) {
            congestionController.onAcknowledgement();
        }
    }

    private byte[] copyRange(byte[] bytes, int offset, int length) {
        byte[] chunk = new byte[length];
        System.arraycopy(bytes, offset, chunk, 0, length);
        return chunk;
    }
}

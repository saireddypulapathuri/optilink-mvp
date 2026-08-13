package com.optilink.protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Binary codec for selective acknowledgement payloads.
 */
public final class SelectiveAckCodec {
    private static final int MAX_RANGES = 1_024;
    private static final int FIXED_BYTES = Long.BYTES + Integer.BYTES;
    private static final int RANGE_BYTES = Long.BYTES * 2;

    /**
     * Encodes a selective acknowledgement frame.
     *
     * @param frame acknowledgement frame
     * @return encoded bytes
     */
    public byte[] encode(SelectiveAckFrame frame) {
        if (frame.ranges().size() > MAX_RANGES) {
            throw new IllegalArgumentException("too many acknowledgement ranges");
        }
        ByteBuffer buffer = ByteBuffer.allocate(FIXED_BYTES + frame.ranges().size() * RANGE_BYTES);
        buffer.putLong(frame.cumulativeAck());
        buffer.putInt(frame.ranges().size());
        frame.ranges().forEach(range -> writeRange(buffer, range));
        return buffer.array();
    }

    /**
     * Decodes a selective acknowledgement frame.
     *
     * @param encoded encoded frame bytes
     * @return decoded acknowledgement frame
     */
    public SelectiveAckFrame decode(byte[] encoded) {
        if (encoded.length < FIXED_BYTES) {
            throw new PacketIntegrityException("ack frame is too short");
        }
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        long cumulativeAck = buffer.getLong();
        int rangeCount = readRangeCount(buffer);
        List<AckRange> ranges = readRanges(buffer, rangeCount);
        if (buffer.hasRemaining()) {
            throw new PacketIntegrityException("ack frame contains trailing bytes");
        }
        return new SelectiveAckFrame(cumulativeAck, ranges);
    }

    private void writeRange(ByteBuffer buffer, AckRange range) {
        buffer.putLong(range.startInclusive());
        buffer.putLong(range.endInclusive());
    }

    private int readRangeCount(ByteBuffer buffer) {
        int rangeCount = buffer.getInt();
        if (rangeCount < 0 || rangeCount > MAX_RANGES) {
            throw new PacketIntegrityException("invalid ack range count");
        }
        if (buffer.remaining() != rangeCount * RANGE_BYTES) {
            throw new PacketIntegrityException("ack range length mismatch");
        }
        return rangeCount;
    }

    private List<AckRange> readRanges(ByteBuffer buffer, int rangeCount) {
        List<AckRange> ranges = new ArrayList<>(rangeCount);
        for (int index = 0; index < rangeCount; index++) {
            ranges.add(new AckRange(buffer.getLong(), buffer.getLong()));
        }
        return ranges;
    }
}

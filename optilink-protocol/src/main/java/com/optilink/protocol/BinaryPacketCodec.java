package com.optilink.protocol;

import com.optilink.core.BinaryPayload;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.zip.CRC32C;

/**
 * Canonical binary encoder and decoder for OptiLink protocol packets.
 */
public final class BinaryPacketCodec {
    /**
     * Current protocol version.
     */
    public static final int CURRENT_VERSION = 1;

    /**
     * Maximum unsigned 16-bit flag value.
     */
    public static final int MAX_FLAGS = 65_535;

    /**
     * Conservative maximum payload size for a single visual symbol.
     */
    public static final int MAX_PAYLOAD_BYTES = 16_384;

    private static final int MAGIC = 0x4F50544C;
    private static final int HEADER_BYTES = 48;
    private static final int CHECKSUM_BYTES = 4;

    /**
     * Encodes a packet into its canonical binary representation.
     *
     * @param packet packet to encode
     * @return encoded packet bytes
     */
    public byte[] encode(OpticalPacket packet) {
        byte[] payload = packet.payload().bytes();
        PacketHeader header = packet.header().withChecksum(checksum(packet.header(), payload));
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + payload.length);
        writeHeader(buffer, header);
        buffer.put(payload);
        return buffer.array();
    }

    /**
     * Decodes and verifies a canonical binary packet.
     *
     * @param encoded encoded packet bytes
     * @return decoded packet
     */
    public OpticalPacket decode(byte[] encoded) {
        if (encoded.length < HEADER_BYTES) {
            throw new PacketIntegrityException("frame is shorter than header");
        }
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        PacketHeader header = readHeader(buffer);
        byte[] payload = readPayload(buffer, header.payloadLength());
        verifyLength(buffer);
        verifyChecksum(header, payload);
        return new OpticalPacket(header, new BinaryPayload(payload));
    }

    private void writeHeader(ByteBuffer buffer, PacketHeader header) {
        buffer.putInt(MAGIC);
        buffer.put((byte) header.version());
        buffer.put((byte) header.type().wireValue());
        buffer.putShort((short) header.flags());
        buffer.putLong(header.sessionId().getMostSignificantBits());
        buffer.putLong(header.sessionId().getLeastSignificantBits());
        buffer.putLong(header.sequenceNumber());
        buffer.putLong(header.acknowledgementNumber());
        buffer.putInt(header.payloadLength());
        buffer.putInt(header.checksum());
    }

    private PacketHeader readHeader(ByteBuffer buffer) {
        int magic = buffer.getInt();
        if (magic != MAGIC) {
            throw new PacketIntegrityException("invalid frame magic");
        }
        int version = Byte.toUnsignedInt(buffer.get());
        PacketType type = PacketType.fromWireValue(Byte.toUnsignedInt(buffer.get()));
        int flags = Short.toUnsignedInt(buffer.getShort());
        UUID sessionId = new UUID(buffer.getLong(), buffer.getLong());
        long sequence = buffer.getLong();
        long acknowledgement = buffer.getLong();
        int payloadLength = buffer.getInt();
        int checksum = buffer.getInt();
        return new PacketHeader(version, type, flags, sessionId, sequence, acknowledgement, payloadLength, checksum);
    }

    private byte[] readPayload(ByteBuffer buffer, int payloadLength) {
        if (buffer.remaining() < payloadLength) {
            throw new PacketIntegrityException("payload is truncated");
        }
        byte[] payload = new byte[payloadLength];
        buffer.get(payload);
        return payload;
    }

    private void verifyLength(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            throw new PacketIntegrityException("frame contains trailing bytes");
        }
    }

    private void verifyChecksum(PacketHeader header, byte[] payload) {
        int actual = checksum(header, payload);
        if (actual != header.checksum()) {
            throw new PacketIntegrityException("packet checksum mismatch");
        }
    }

    private int checksum(PacketHeader header, byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES - CHECKSUM_BYTES + payload.length);
        buffer.putInt(MAGIC);
        buffer.put((byte) header.version());
        buffer.put((byte) header.type().wireValue());
        buffer.putShort((short) header.flags());
        buffer.putLong(header.sessionId().getMostSignificantBits());
        buffer.putLong(header.sessionId().getLeastSignificantBits());
        buffer.putLong(header.sequenceNumber());
        buffer.putLong(header.acknowledgementNumber());
        buffer.putInt(header.payloadLength());
        buffer.put(payload);
        CRC32C crc32C = new CRC32C();
        crc32C.update(buffer.array(), 0, buffer.position());
        return (int) crc32C.getValue();
    }
}

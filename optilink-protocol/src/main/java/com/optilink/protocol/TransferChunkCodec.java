package com.optilink.protocol;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Binary codec for sequenced MVP transfer chunks.
 */
public final class TransferChunkCodec {
    private static final int MAGIC = 0x4F544348;
    private static final int VERSION = 1;
    private static final int HEADER_BYTES = Integer.BYTES * 5 + Long.BYTES * 2;

    /**
     * Returns the fixed encoded overhead before chunk bytes.
     *
     * @return overhead bytes
     */
    public int encodedOverheadBytes() {
        return HEADER_BYTES;
    }

    /**
     * Encodes a transfer chunk.
     *
     * @param chunk transfer chunk
     * @return encoded bytes
     */
    public byte[] encode(TransferChunk chunk) {
        byte[] bytes = chunk.bytes();
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_BYTES + bytes.length);
        buffer.putInt(MAGIC);
        buffer.putInt(VERSION);
        buffer.putLong(chunk.transferId().getMostSignificantBits());
        buffer.putLong(chunk.transferId().getLeastSignificantBits());
        buffer.putInt(chunk.index());
        buffer.putInt(chunk.totalChunks());
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        return buffer.array();
    }

    /**
     * Decodes a transfer chunk.
     *
     * @param encoded encoded bytes
     * @return decoded transfer chunk
     */
    public TransferChunk decode(byte[] encoded) {
        ByteBuffer buffer = ByteBuffer.wrap(encoded.clone());
        verifyHeader(buffer);
        UUID transferId = new UUID(buffer.getLong(), buffer.getLong());
        int index = buffer.getInt();
        int totalChunks = buffer.getInt();
        byte[] bytes = readChunkBytes(buffer);
        return new TransferChunk(transferId, index, totalChunks, bytes);
    }

    private void verifyHeader(ByteBuffer buffer) {
        if (buffer.remaining() < HEADER_BYTES || buffer.getInt() != MAGIC || buffer.getInt() != VERSION) {
            throw new PacketIntegrityException("invalid transfer chunk header");
        }
    }

    private byte[] readChunkBytes(ByteBuffer buffer) {
        int length = buffer.getInt();
        if (length < 1 || length != buffer.remaining()) {
            throw new PacketIntegrityException("invalid transfer chunk length");
        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }
}

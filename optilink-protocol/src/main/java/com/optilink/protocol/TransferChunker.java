package com.optilink.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Splits transfer bytes into sequenced chunks.
 */
public final class TransferChunker {

    /**
     * Splits bytes into transfer chunks.
     *
     * @param transferId transfer identifier
     * @param bytes bytes to chunk
     * @param maxChunkBytes maximum chunk payload bytes
     * @return transfer chunks
     */
    public List<TransferChunk> chunk(UUID transferId, byte[] bytes, int maxChunkBytes) {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length == 0 || maxChunkBytes < 1) {
            throw new IllegalArgumentException("transfer bytes and chunk size must be positive");
        }
        int totalChunks = Math.ceilDiv(bytes.length, maxChunkBytes);
        List<TransferChunk> chunks = new ArrayList<>(totalChunks);
        for (int index = 0; index < totalChunks; index++) {
            chunks.add(new TransferChunk(transferId, index, totalChunks, copyChunk(bytes, index, maxChunkBytes)));
        }
        return List.copyOf(chunks);
    }

    private byte[] copyChunk(byte[] bytes, int index, int maxChunkBytes) {
        int offset = index * maxChunkBytes;
        int length = Math.min(maxChunkBytes, bytes.length - offset);
        byte[] chunk = new byte[length];
        System.arraycopy(bytes, offset, chunk, 0, length);
        return chunk;
    }
}

package com.optilink.protocol;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Sequenced chunk of an MVP optical transfer.
 *
 * @param transferId transfer identifier shared by all chunks
 * @param index zero-based chunk index
 * @param totalChunks total number of chunks in the transfer
 * @param bytes chunk bytes
 */
public record TransferChunk(UUID transferId, int index, int totalChunks, byte[] bytes) {

    /**
     * Creates a transfer chunk.
     */
    public TransferChunk {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(bytes, "bytes");
        if (index < 0 || totalChunks < 1 || index >= totalChunks || bytes.length == 0) {
            throw new IllegalArgumentException("invalid transfer chunk");
        }
        bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
        return bytes.clone();
    }

    @Override
    public boolean equals(Object candidate) {
        return candidate instanceof TransferChunk other
                && transferId.equals(other.transferId)
                && index == other.index
                && totalChunks == other.totalChunks
                && Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transferId, index, totalChunks, Arrays.hashCode(bytes));
    }
}

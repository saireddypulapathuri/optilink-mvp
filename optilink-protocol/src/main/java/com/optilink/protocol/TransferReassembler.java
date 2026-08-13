package com.optilink.protocol;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Reassembles sequenced MVP transfer chunks.
 */
public final class TransferReassembler {
    private final Map<Integer, TransferChunk> chunks = new TreeMap<>();
    private UUID transferId;
    private int totalChunks = -1;

    /**
     * Accepts a transfer chunk.
     *
     * @param chunk chunk to store
     */
    public synchronized void accept(TransferChunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        initializeOrVerify(chunk);
        chunks.putIfAbsent(chunk.index(), chunk);
    }

    /**
     * Returns whether all chunks are available.
     *
     * @return true when complete
     */
    public synchronized boolean isComplete() {
        return totalChunks > 0 && chunks.size() == totalChunks;
    }

    /**
     * Returns the transfer identifier when at least one chunk has been accepted.
     *
     * @return transfer identifier
     */
    public synchronized Optional<UUID> transferId() {
        return Optional.ofNullable(transferId);
    }

    /**
     * Returns the expected chunk count, or zero before any chunk is accepted.
     *
     * @return expected chunk count
     */
    public synchronized int expectedChunks() {
        return Math.max(totalChunks, 0);
    }

    /**
     * Returns the number of unique chunks accepted so far.
     *
     * @return accepted unique chunk count
     */
    public synchronized int acceptedChunks() {
        return chunks.size();
    }

    /**
     * Returns accepted chunks sorted by chunk index.
     *
     * @return accepted chunks
     */
    public synchronized List<TransferChunk> chunks() {
        return List.copyOf(chunks.values());
    }

    /**
     * Returns zero-based missing chunk indexes.
     *
     * @return missing chunk indexes
     */
    public synchronized List<Integer> missingIndexes() {
        if (totalChunks < 1) {
            return List.of();
        }
        List<Integer> missing = new ArrayList<>();
        for (int index = 0; index < totalChunks; index++) {
            if (!chunks.containsKey(index)) {
                missing.add(index);
            }
        }
        return List.copyOf(missing);
    }

    /**
     * Reassembles all chunks into transfer bytes.
     *
     * @return reassembled transfer bytes
     */
    public synchronized byte[] assemble() {
        if (!isComplete()) {
            throw new PacketIntegrityException("transfer is incomplete");
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        chunks.values().forEach(chunk -> output.writeBytes(chunk.bytes()));
        return output.toByteArray();
    }

    private void initializeOrVerify(TransferChunk chunk) {
        if (transferId == null) {
            transferId = chunk.transferId();
            totalChunks = chunk.totalChunks();
            return;
        }
        if (!transferId.equals(chunk.transferId()) || totalChunks != chunk.totalChunks()) {
            throw new PacketIntegrityException("transfer chunk belongs to a different transfer");
        }
    }
}

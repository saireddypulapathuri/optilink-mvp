package com.optilink.transfer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Passphrase-free diagnostics for an MVP QR transfer directory.
 *
 * @param transferId transfer identifier when at least one valid frame is present
 * @param framesRead PNG frames found in the directory
 * @param decodedChunks frames that decoded into transfer chunks accepted for this transfer
 * @param uniqueChunks unique chunks accepted for this transfer
 * @param expectedChunks expected chunk count, or zero when unknown
 * @param duplicateFrames decoded frames already present in the transfer
 * @param invalidFrames PNG frames that could not be decoded or did not belong to the transfer
 * @param missingFrameNumbers one-based missing frame numbers
 */
public record MvpTransferInspection(
        Optional<UUID> transferId,
        int framesRead,
        int decodedChunks,
        int uniqueChunks,
        int expectedChunks,
        int duplicateFrames,
        int invalidFrames,
        List<Integer> missingFrameNumbers) {

    /**
     * Creates transfer inspection diagnostics.
     */
    public MvpTransferInspection {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(missingFrameNumbers, "missingFrameNumbers");
        if (framesRead < 0
                || decodedChunks < 0
                || uniqueChunks < 0
                || expectedChunks < 0
                || duplicateFrames < 0
                || invalidFrames < 0) {
            throw new IllegalArgumentException("inspection counts must be non-negative");
        }
        missingFrameNumbers = List.copyOf(missingFrameNumbers);
    }

    /**
     * Returns whether all chunks needed to assemble the transfer are present.
     *
     * @return true when complete
     */
    public boolean complete() {
        return expectedChunks > 0 && uniqueChunks == expectedChunks && missingFrameNumbers.isEmpty();
    }
}

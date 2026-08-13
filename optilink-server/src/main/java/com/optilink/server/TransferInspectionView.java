package com.optilink.server;

import java.util.List;
import java.util.Objects;

/**
 * API view of transfer frame diagnostics.
 *
 * @param transferId transfer identifier, or unknown when no valid frame is present
 * @param framesRead PNG frames found in the transfer directory
 * @param decodedChunks decoded chunks accepted for this transfer
 * @param uniqueChunks unique chunks accepted for this transfer
 * @param expectedChunks expected chunk count
 * @param duplicateFrames duplicate decoded frames
 * @param invalidFrames undecodable or mismatched frames
 * @param complete whether all frames are present
 * @param missingFrameNumbers one-based missing frame numbers
 */
public record TransferInspectionView(
        String transferId,
        int framesRead,
        int decodedChunks,
        int uniqueChunks,
        int expectedChunks,
        int duplicateFrames,
        int invalidFrames,
        boolean complete,
        List<Integer> missingFrameNumbers) {

    /**
     * Creates a diagnostics view.
     */
    public TransferInspectionView {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(missingFrameNumbers, "missingFrameNumbers");
        missingFrameNumbers = List.copyOf(missingFrameNumbers);
    }
}

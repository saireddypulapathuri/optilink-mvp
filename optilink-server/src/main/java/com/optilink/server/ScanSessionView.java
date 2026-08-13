package com.optilink.server;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Browser-facing state for a live scan session.
 *
 * @param sessionId scan session id
 * @param transferId decoded transfer id when known
 * @param acceptedFrames unique frames accepted
 * @param expectedFrames expected frame count, or zero until known
 * @param complete whether all chunks are present
 * @param invalidFrames frames that did not decode
 * @param duplicateFrames frames already seen
 * @param missingFrameNumbers one-based missing frame numbers
 * @param createdAt session creation timestamp
 * @param receiveUrl receive endpoint for the completed scan
 */
public record ScanSessionView(
        String sessionId,
        String transferId,
        int acceptedFrames,
        int expectedFrames,
        boolean complete,
        int invalidFrames,
        int duplicateFrames,
        List<Integer> missingFrameNumbers,
        Instant createdAt,
        String receiveUrl) {

    /**
     * Creates a scan session view.
     */
    public ScanSessionView {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(missingFrameNumbers, "missingFrameNumbers");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(receiveUrl, "receiveUrl");
        missingFrameNumbers = List.copyOf(missingFrameNumbers);
    }
}

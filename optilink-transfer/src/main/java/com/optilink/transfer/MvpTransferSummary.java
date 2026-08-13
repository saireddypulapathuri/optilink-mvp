package com.optilink.transfer;

import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

/**
 * Summary of files generated for an MVP QR transfer.
 *
 * @param transferId transfer identifier
 * @param frameCount number of QR frames written
 * @param outputDirectory directory containing frame images
 * @param displayPage browser display page
 * @param manifest transfer metadata manifest
 */
public record MvpTransferSummary(UUID transferId, int frameCount, Path outputDirectory, Path displayPage, Path manifest) {

    /**
     * Creates a transfer summary.
     */
    public MvpTransferSummary {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(displayPage, "displayPage");
        Objects.requireNonNull(manifest, "manifest");
        if (frameCount < 1) {
            throw new IllegalArgumentException("frameCount must be positive");
        }
    }
}

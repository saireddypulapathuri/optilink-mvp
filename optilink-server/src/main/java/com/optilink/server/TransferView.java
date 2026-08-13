package com.optilink.server;

import java.time.Instant;
import java.util.Objects;

/**
 * API view of a generated transfer.
 *
 * @param transferId transfer identifier
 * @param kind text, image, video, or file
 * @param name operator-facing payload name
 * @param contentType payload MIME type
 * @param plaintextByteCount original plaintext byte count
 * @param frameCount number of QR frames generated
 * @param createdAt creation timestamp
 * @param displayUrl generated QR display page URL
 * @param manifestUrl transfer manifest URL
 * @param inspectUrl diagnostics URL
 * @param receiveUrl receive URL
 */
public record TransferView(
        String transferId,
        String kind,
        String name,
        String contentType,
        int plaintextByteCount,
        int frameCount,
        Instant createdAt,
        String displayUrl,
        String manifestUrl,
        String inspectUrl,
        String receiveUrl) {

    /**
     * Creates a transfer view.
     */
    public TransferView {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(contentType, "contentType");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(displayUrl, "displayUrl");
        Objects.requireNonNull(manifestUrl, "manifestUrl");
        Objects.requireNonNull(inspectUrl, "inspectUrl");
        Objects.requireNonNull(receiveUrl, "receiveUrl");
    }
}

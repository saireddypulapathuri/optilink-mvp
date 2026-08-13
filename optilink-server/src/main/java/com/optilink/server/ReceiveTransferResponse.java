package com.optilink.server;

/**
 * Response returned after decrypting a transfer.
 *
 * @param transferId transfer identifier
 * @param kind text, image, video, or file
 * @param name operator-facing payload name
 * @param contentType payload MIME type
 * @param byteCount recovered plaintext byte count
 * @param text UTF-8 text when the payload is valid UTF-8
 * @param base64 plaintext bytes encoded as Base64
 * @param dataUrl browser-ready data URL for preview or download
 * @param utf8Text whether text contains the recovered payload
 * @param media whether the response is an image or video payload
 */
public record ReceiveTransferResponse(
        String transferId,
        String kind,
        String name,
        String contentType,
        int byteCount,
        String text,
        String base64,
        String dataUrl,
        boolean utf8Text,
        boolean media) {
}

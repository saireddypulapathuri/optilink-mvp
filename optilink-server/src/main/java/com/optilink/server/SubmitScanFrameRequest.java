package com.optilink.server;

/**
 * Camera frame submitted by the browser receiver.
 *
 * @param imageBase64 camera frame image bytes encoded as Base64 when browser-side QR detection is unavailable
 * @param qrPayload decoded OptiLink QR text payload when the browser camera detector succeeds
 */
public record SubmitScanFrameRequest(String imageBase64, String qrPayload) {

    /**
     * Creates an image-backed request for older camera clients.
     *
     * @param imageBase64 camera frame image bytes encoded as Base64
     */
    public SubmitScanFrameRequest(String imageBase64) {
        this(imageBase64, null);
    }
}

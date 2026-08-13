package com.optilink.server;

/**
 * Camera frame submitted by the browser receiver.
 *
 * @param imageBase64 camera frame image bytes encoded as Base64
 */
public record SubmitScanFrameRequest(String imageBase64) {
}

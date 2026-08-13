package com.optilink.server;

/**
 * Request to create a passphrase-sealed file transfer.
 *
 * @param fileName original file name
 * @param contentType browser MIME type for the payload
 * @param contentBase64 file bytes encoded as Base64
 * @param passphrase transfer passphrase
 */
public record CreateFileTransferRequest(String fileName, String contentType, String contentBase64, String passphrase) {
}

package com.optilink.server;

/**
 * Request to create a live camera scan session.
 *
 * @param passphrase transfer passphrase
 */
public record CreateScanSessionRequest(String passphrase) {
}

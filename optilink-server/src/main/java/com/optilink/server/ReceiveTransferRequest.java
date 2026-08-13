package com.optilink.server;

/**
 * Request to recover a transfer.
 *
 * @param passphrase transfer passphrase
 */
public record ReceiveTransferRequest(String passphrase) {
}

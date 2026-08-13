package com.optilink.server;

/**
 * Request to create a passphrase-sealed text transfer.
 *
 * @param text text payload
 * @param passphrase transfer passphrase
 */
public record CreateTextTransferRequest(String text, String passphrase) {
}

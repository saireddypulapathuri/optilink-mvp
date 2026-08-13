package com.optilink.core;

/**
 * Base unchecked exception for invariant violations and unrecoverable protocol errors.
 */
public class OptiLinkException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message failure description
     */
    public OptiLinkException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message failure description
     * @param cause root cause
     */
    public OptiLinkException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.optilink.protocol;

import com.optilink.core.OptiLinkException;

/**
 * Signals a malformed frame or checksum mismatch at the protocol layer.
 */
public final class PacketIntegrityException extends OptiLinkException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an integrity failure.
     *
     * @param message failure description
     */
    public PacketIntegrityException(String message) {
        super(message);
    }

    /**
     * Creates an integrity failure with a root cause.
     *
     * @param message failure description
     * @param cause root cause
     */
    public PacketIntegrityException(String message, Throwable cause) {
        super(message, cause);
    }
}

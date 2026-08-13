package com.optilink.session;

/**
 * Lifecycle states for an OptiLink secure session.
 */
public enum SessionState {
    /**
     * Session object has been created but no handshake material has been exchanged.
     */
    NEW,

    /**
     * Handshake material is in progress.
     */
    HANDSHAKING,

    /**
     * Session keys are available and application data may flow.
     */
    ESTABLISHED,

    /**
     * Session is rotating traffic keys.
     */
    REKEYING,

    /**
     * Session closed cleanly.
     */
    CLOSED,

    /**
     * Session failed due to security, integrity, or transport policy.
     */
    FAILED
}

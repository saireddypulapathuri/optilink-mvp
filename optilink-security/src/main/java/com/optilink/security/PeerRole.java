package com.optilink.security;

/**
 * Session role used to assign directional traffic keys.
 */
public enum PeerRole {
    /**
     * Handshake initiator.
     */
    INITIATOR,

    /**
     * Handshake responder.
     */
    RESPONDER
}

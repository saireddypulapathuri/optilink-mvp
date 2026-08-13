package com.optilink.api;

import com.optilink.core.BinaryPayload;

/**
 * Application port for sending and receiving authenticated optical messages.
 */
public interface OptiLinkEndpoint {

    /**
     * Starts a session with a nearby optical peer.
     *
     * @return session handle
     */
    SessionHandle openSession();

    /**
     * Sends application payload bytes through an established session.
     *
     * @param handle session handle
     * @param payload payload to send
     */
    void send(SessionHandle handle, BinaryPayload payload);

    /**
     * Closes an established session.
     *
     * @param handle session handle
     */
    void close(SessionHandle handle);
}

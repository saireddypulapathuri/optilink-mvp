package com.optilink.api;

import com.optilink.core.BinaryPayload;

/**
 * Hexagonal port implemented by display and camera adapters.
 */
public interface OpticalChannelPort {

    /**
     * Transmits encoded optical bytes to the display layer.
     *
     * @param payload encoded symbol payload
     */
    void transmit(BinaryPayload payload);

    /**
     * Receives encoded optical bytes from the camera layer.
     *
     * @return received symbol payload
     */
    BinaryPayload receive();
}

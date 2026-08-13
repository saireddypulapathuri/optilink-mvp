package com.optilink.optical;

/**
 * QR-compatible Reed-Solomon correction levels used by the optical symbol layer.
 */
public enum ErrorCorrectionLevel {
    /**
     * Low redundancy and highest capacity.
     */
    L,

    /**
     * Medium redundancy.
     */
    M,

    /**
     * Quality-focused redundancy.
     */
    Q,

    /**
     * Highest redundancy and lowest capacity.
     */
    H
}

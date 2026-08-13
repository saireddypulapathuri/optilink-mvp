package com.optilink.optical;

import java.util.Objects;

/**
 * Display-side optical transmission profile.
 *
 * @param name profile name
 * @param targetFps target displayed symbols per second
 * @param qrVersion QR version used by symbol renderers
 * @param errorCorrectionLevel Reed-Solomon correction level
 * @param brightnessPercent display brightness percentage
 * @param maxPayloadBytes maximum payload bytes per symbol
 */
public record OpticalProfile(
        String name,
        int targetFps,
        int qrVersion,
        ErrorCorrectionLevel errorCorrectionLevel,
        int brightnessPercent,
        int maxPayloadBytes) {

    /**
     * Creates an optical profile.
     */
    public OpticalProfile {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(errorCorrectionLevel, "errorCorrectionLevel");
        if (targetFps < 1 || qrVersion < 1 || qrVersion > 40 || maxPayloadBytes < 1) {
            throw new IllegalArgumentException("invalid optical profile dimensions");
        }
        if (brightnessPercent < 10 || brightnessPercent > 100) {
            throw new IllegalArgumentException("brightnessPercent must be within [10, 100]");
        }
    }
}

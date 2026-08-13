package com.optilink.camera;

import java.util.Optional;

/**
 * Platform camera adapter SPI.
 */
public interface CameraAdapter {

    /**
     * Captures a single camera frame.
     *
     * @return captured frame, or empty when no frame is available
     */
    Optional<CameraFrame> capture();

    /**
     * Returns calibration data for this camera.
     *
     * @return calibration values
     */
    CameraCalibration calibration();
}

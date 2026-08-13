package com.optilink.camera;

/**
 * Camera calibration values used before perspective correction and symbol decoding.
 *
 * @param focalLengthPixels focal length in pixels
 * @param principalPointX optical center x-coordinate
 * @param principalPointY optical center y-coordinate
 * @param radialDistortion normalized radial distortion coefficient
 */
public record CameraCalibration(
        double focalLengthPixels,
        double principalPointX,
        double principalPointY,
        double radialDistortion) {

    /**
     * Creates camera calibration values.
     */
    public CameraCalibration {
        if (focalLengthPixels <= 0.0) {
            throw new IllegalArgumentException("focalLengthPixels must be positive");
        }
    }
}

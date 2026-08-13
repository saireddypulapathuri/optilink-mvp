package com.optilink.camera;

import java.util.Objects;

/**
 * Computes brightness, contrast, and blur feedback from 8-bit grayscale frames.
 */
public final class LuminanceFrameAnalyzer {
    private static final double MAX_BYTE = 255.0;
    private static final double MAX_STDDEV = 127.5;
    private static final double SHARP_LAPLACIAN_VARIANCE = 1_000.0;

    /**
     * Analyzes an 8-bit grayscale frame.
     *
     * @param frame frame whose pixel array contains one luminance byte per pixel
     * @return computed quality metrics
     */
    public CameraFrameQuality analyze(CameraFrame frame) {
        Objects.requireNonNull(frame, "frame");
        byte[] pixels = frame.pixels();
        validatePixels(frame, pixels);
        double mean = mean(pixels);
        double contrast = normalizedStandardDeviation(pixels, mean);
        double blurScore = blurScore(frame, pixels);
        return new CameraFrameQuality(mean / MAX_BYTE, contrast, blurScore);
    }

    private void validatePixels(CameraFrame frame, byte[] pixels) {
        if (pixels.length != frame.width() * frame.height()) {
            throw new IllegalArgumentException("luminance frame must contain one byte per pixel");
        }
        if (frame.width() < 3 || frame.height() < 3) {
            throw new IllegalArgumentException("frame must be at least 3x3 for blur analysis");
        }
    }

    private double mean(byte[] pixels) {
        long sum = 0;
        for (byte pixel : pixels) {
            sum += Byte.toUnsignedInt(pixel);
        }
        return sum / (double) pixels.length;
    }

    private double normalizedStandardDeviation(byte[] pixels, double mean) {
        double variance = 0.0;
        for (byte pixel : pixels) {
            double delta = Byte.toUnsignedInt(pixel) - mean;
            variance += delta * delta;
        }
        double standardDeviation = Math.sqrt(variance / pixels.length);
        return Math.min(1.0, standardDeviation / MAX_STDDEV);
    }

    private double blurScore(CameraFrame frame, byte[] pixels) {
        double variance = laplacianVariance(frame, pixels);
        return 1.0 - Math.min(1.0, variance / SHARP_LAPLACIAN_VARIANCE);
    }

    private double laplacianVariance(CameraFrame frame, byte[] pixels) {
        double sum = 0.0;
        double sumSquares = 0.0;
        int samples = 0;
        for (int y = 1; y < frame.height() - 1; y++) {
            for (int x = 1; x < frame.width() - 1; x++) {
                double value = laplacianAt(frame, pixels, x, y);
                sum += value;
                sumSquares += value * value;
                samples++;
            }
        }
        double mean = sum / samples;
        return sumSquares / samples - mean * mean;
    }

    private int laplacianAt(CameraFrame frame, byte[] pixels, int x, int y) {
        int center = luminance(frame, pixels, x, y) * 4;
        return center
                - luminance(frame, pixels, x - 1, y)
                - luminance(frame, pixels, x + 1, y)
                - luminance(frame, pixels, x, y - 1)
                - luminance(frame, pixels, x, y + 1);
    }

    private int luminance(CameraFrame frame, byte[] pixels, int x, int y) {
        return Byte.toUnsignedInt(pixels[y * frame.width() + x]);
    }
}

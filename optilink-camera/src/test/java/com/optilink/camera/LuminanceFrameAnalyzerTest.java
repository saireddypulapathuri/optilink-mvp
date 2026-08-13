package com.optilink.camera;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class LuminanceFrameAnalyzerTest {

    @Test
    void reportsHigherBlurForFlatFrameThanCheckerboard() {
        LuminanceFrameAnalyzer analyzer = new LuminanceFrameAnalyzer();

        CameraFrameQuality flat = analyzer.analyze(frame(flatPixels()));
        CameraFrameQuality sharp = analyzer.analyze(frame(checkerboardPixels()));

        assertTrue(flat.blurScore() > sharp.blurScore());
        assertTrue(flat.contrast() < sharp.contrast());
    }

    private CameraFrame frame(byte[] pixels) {
        return new CameraFrame(pixels, 8, 8, Instant.EPOCH);
    }

    private byte[] flatPixels() {
        byte[] pixels = new byte[64];
        java.util.Arrays.fill(pixels, (byte) 120);
        return pixels;
    }

    private byte[] checkerboardPixels() {
        byte[] pixels = new byte[64];
        for (int index = 0; index < pixels.length; index++) {
            pixels[index] = (byte) (index % 2 == 0 ? 0 : 255);
        }
        return pixels;
    }
}

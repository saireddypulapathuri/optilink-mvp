package com.optilink.compression;

import com.optilink.core.BinaryPayload;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Deflate-based compressor available in the Java runtime.
 */
public final class DeflatePayloadCompressor implements PayloadCompressor {
    private static final int BUFFER_BYTES = 4_096;
    private final int compressionLevel;

    /**
     * Creates a balanced compressor.
     */
    public DeflatePayloadCompressor() {
        this(Deflater.BEST_SPEED);
    }

    /**
     * Creates a compressor with a configured deflate level.
     *
     * @param compressionLevel deflate compression level
     */
    public DeflatePayloadCompressor(int compressionLevel) {
        if (compressionLevel < Deflater.NO_COMPRESSION || compressionLevel > Deflater.BEST_COMPRESSION) {
            throw new IllegalArgumentException("invalid deflate compression level");
        }
        this.compressionLevel = compressionLevel;
    }

    @Override
    public BinaryPayload compress(BinaryPayload payload) throws IOException {
        Objects.requireNonNull(payload, "payload");
        Deflater deflater = new Deflater(compressionLevel);
        try {
            return new BinaryPayload(deflate(deflater, payload.bytes()));
        } finally {
            deflater.end();
        }
    }

    @Override
    public BinaryPayload decompress(BinaryPayload payload) throws IOException {
        Objects.requireNonNull(payload, "payload");
        Inflater inflater = new Inflater();
        try (InflaterInputStream input = new InflaterInputStream(new java.io.ByteArrayInputStream(payload.bytes()), inflater);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            input.transferTo(output);
            return new BinaryPayload(output.toByteArray());
        } finally {
            inflater.end();
        }
    }

    private byte[] deflate(Deflater deflater, byte[] input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_BYTES];
        deflater.setInput(input);
        deflater.finish();
        while (!deflater.finished()) {
            int written = deflater.deflate(buffer);
            if (written == 0 && deflater.needsInput()) {
                throw new IOException("deflater stalled");
            }
            output.write(buffer, 0, written);
        }
        return output.toByteArray();
    }
}

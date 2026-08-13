package com.optilink.compression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.optilink.core.BinaryPayload;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DeflatePayloadCompressorTest {

    @Test
    void roundTripsCompressiblePayload() throws Exception {
        byte[] source = "optical ".repeat(100).getBytes(StandardCharsets.UTF_8);
        DeflatePayloadCompressor compressor = new DeflatePayloadCompressor();

        BinaryPayload compressed = compressor.compress(new BinaryPayload(source));
        BinaryPayload decompressed = compressor.decompress(compressed);

        assertTrue(compressed.length() < source.length);
        assertArrayEquals(source, decompressed.bytes());
    }
}

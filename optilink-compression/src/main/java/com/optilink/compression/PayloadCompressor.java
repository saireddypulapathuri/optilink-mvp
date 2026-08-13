package com.optilink.compression;

import com.optilink.core.BinaryPayload;
import java.io.IOException;

/**
 * Compresses and decompresses binary payloads before encryption.
 */
public interface PayloadCompressor {

    /**
     * Compresses a payload.
     *
     * @param payload input payload
     * @return compressed payload
     * @throws IOException when compression fails
     */
    BinaryPayload compress(BinaryPayload payload) throws IOException;

    /**
     * Decompresses a payload.
     *
     * @param payload compressed payload
     * @return decompressed payload
     * @throws IOException when decompression fails
     */
    BinaryPayload decompress(BinaryPayload payload) throws IOException;
}

package com.optilink.core;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BinaryPayloadTest {

    @Test
    void payloadDefensivelyCopiesBytes() {
        byte[] source = {1, 2, 3};
        BinaryPayload payload = new BinaryPayload(source);

        source[0] = 9;
        byte[] returned = payload.bytes();
        returned[1] = 8;

        assertArrayEquals(new byte[] {1, 2, 3}, payload.bytes());
        assertEquals(3, payload.length());
    }
}

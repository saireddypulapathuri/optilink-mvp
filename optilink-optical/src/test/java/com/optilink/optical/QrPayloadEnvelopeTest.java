package com.optilink.optical;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QrPayloadEnvelopeTest {

    @Test
    void encodesBinaryBytesIntoVersionedAsciiEnvelope() {
        byte[] bytes = {0, 1, 2, 3, -1};

        String envelope = QrPayloadEnvelope.encode(bytes);

        assertTrue(envelope.startsWith(QrPayloadEnvelope.PREFIX));
        assertArrayEquals(bytes, QrPayloadEnvelope.decode(envelope));
    }

    @Test
    void rejectsUnknownEnvelopePrefix() {
        assertThrows(IllegalArgumentException.class, () -> QrPayloadEnvelope.decode("NOPE"));
    }
}

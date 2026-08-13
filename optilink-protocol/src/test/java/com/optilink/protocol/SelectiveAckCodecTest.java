package com.optilink.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class SelectiveAckCodecTest {

    @Test
    void ackFrameRoundTrips() {
        SelectiveAckFrame frame = new SelectiveAckFrame(4, List.of(new AckRange(7, 9)));
        SelectiveAckCodec codec = new SelectiveAckCodec();

        SelectiveAckFrame decoded = codec.decode(codec.encode(frame));

        assertEquals(frame, decoded);
        assertTrue(decoded.acknowledges(3));
        assertTrue(decoded.acknowledges(8));
    }
}

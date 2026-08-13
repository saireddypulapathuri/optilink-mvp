package com.optilink.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReplayWindowTest {

    @Test
    void rejectsDuplicatesAndExpiredSequenceNumbers() {
        ReplayWindow replayWindow = new ReplayWindow(4);

        assertTrue(replayWindow.accept(10));
        assertFalse(replayWindow.accept(10));
        assertTrue(replayWindow.accept(9));
        assertTrue(replayWindow.accept(14));
        assertFalse(replayWindow.accept(9));
    }
}

package com.optilink.congestion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdaptiveWindowControllerTest {

    @Test
    void growsAdditivelyAndCutsOnLoss() {
        AdaptiveWindowController controller = new AdaptiveWindowController(1, 2, 5);

        controller.onAcknowledgement();
        controller.onAcknowledgement();
        controller.onAcknowledgement();
        controller.onLoss();

        assertEquals(1, controller.windowSize());
    }

    @Test
    void opticalFeedbackCanReduceWindow() {
        AdaptiveWindowController controller = new AdaptiveWindowController(1, 4, 10);

        controller.onFeedback(new ChannelFeedback(0.5, 100));

        assertEquals(2, controller.windowSize());
    }
}

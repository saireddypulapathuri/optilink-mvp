package com.optilink.optical;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdaptiveOpticalProfileSelectorTest {

    @Test
    void selectsRobustProfileForBlurredChannel() {
        AdaptiveOpticalProfileSelector selector = new AdaptiveOpticalProfileSelector();

        OpticalProfile profile = selector.select(new OpticalChannelMetrics(30, 0.99, 0.6, 0.7, 100));

        assertEquals("robust", profile.name());
    }

    @Test
    void selectsThroughputProfileForStableChannel() {
        AdaptiveOpticalProfileSelector selector = new AdaptiveOpticalProfileSelector();

        OpticalProfile profile = selector.select(new OpticalChannelMetrics(60, 0.99, 0.1, 0.7, 50));

        assertEquals("throughput", profile.name());
    }
}

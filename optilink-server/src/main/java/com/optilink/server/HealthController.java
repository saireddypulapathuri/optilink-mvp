package com.optilink.server;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reactive server health endpoint.
 */
@RestController
public final class HealthController {

    /**
     * Returns service health.
     *
     * @return health snapshot
     */
    @GetMapping("/health")
    public HealthSnapshot health() {
        return new HealthSnapshot("UP", Instant.now());
    }
}

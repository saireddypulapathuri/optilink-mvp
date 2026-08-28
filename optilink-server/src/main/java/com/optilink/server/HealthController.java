package com.optilink.server;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reactive server health endpoint.
 */
@RestController
public final class HealthController {
    private final TransferRoomService rooms;
    private final ScanSessionService scans;

    /**
     * Creates the health endpoint.
     */
    public HealthController(TransferRoomService rooms, ScanSessionService scans) {
        this.rooms = rooms;
        this.scans = scans;
    }

    /**
     * Returns service health.
     *
     * @return health snapshot
     */
    @GetMapping("/health")
    public HealthSnapshot health() {
        return new HealthSnapshot("UP", Instant.now(), rooms.activeRoomCount(), scans.activeSessionCount());
    }
}

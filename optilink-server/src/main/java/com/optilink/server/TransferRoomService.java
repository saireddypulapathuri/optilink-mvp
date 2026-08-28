package com.optilink.server;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Expiring coordination rooms for concurrent sender and receiver pairs.
 */
@Service
public final class TransferRoomService {
    private static final char[] CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int CODE_LENGTH = 8;
    private static final int MAX_CODE_ATTEMPTS = 20;

    private final Map<String, TransferRoom> rooms = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;
    private final Duration roomTtl;
    private final int maxRooms;

    /**
     * Creates the production room registry.
     */
    @Autowired
    public TransferRoomService(
            @Value("${optilink.rooms.ttl:PT2H}") Duration roomTtl,
            @Value("${optilink.rooms.max-active:2000}") int maxRooms) {
        this(Clock.systemUTC(), roomTtl, maxRooms);
    }

    TransferRoomService(Clock clock, Duration roomTtl, int maxRooms) {
        this.clock = Objects.requireNonNull(clock, "clock");
        this.roomTtl = Objects.requireNonNull(roomTtl, "roomTtl");
        if (roomTtl.isZero() || roomTtl.isNegative()) {
            throw new IllegalArgumentException("roomTtl must be positive");
        }
        if (maxRooms < 1) {
            throw new IllegalArgumentException("maxRooms must be positive");
        }
        this.maxRooms = maxRooms;
    }

    /**
     * Creates an isolated transfer room.
     *
     * @return room state
     */
    public RoomView create() {
        removeExpired();
        if (rooms.size() >= maxRooms) {
            throw new IllegalStateException("active room capacity reached");
        }
        Instant expiresAt = clock.instant().plus(roomTtl);
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String code = randomCode();
            TransferRoom room = new TransferRoom(code, expiresAt);
            if (rooms.putIfAbsent(code, room) == null) {
                return view(room);
            }
        }
        throw new IllegalStateException("unable to allocate room code");
    }

    /**
     * Returns an active room.
     *
     * @param code room code
     * @return room state
     */
    public RoomView get(String code) {
        return view(existing(code));
    }

    /**
     * Attaches a generated transfer to a room.
     *
     * @param code room code
     * @param transferId transfer identifier
     */
    public void attach(String code, UUID transferId) {
        existing(code).attach(Objects.requireNonNull(transferId, "transferId"));
    }

    /**
     * Lists transfer ids in newest-first order.
     *
     * @param code room code
     * @return immutable transfer id set
     */
    public Set<UUID> transferIds(String code) {
        return existing(code).transferIds();
    }

    /**
     * Returns the current active room count.
     *
     * @return active rooms
     */
    public int activeRoomCount() {
        removeExpired();
        return rooms.size();
    }

    private TransferRoom existing(String code) {
        String normalized = normalize(code);
        TransferRoom room = rooms.get(normalized);
        if (room == null || !room.expiresAt().isAfter(clock.instant())) {
            rooms.remove(normalized);
            throw new IllegalArgumentException("room not found or expired");
        }
        return room;
    }

    private void removeExpired() {
        Instant now = clock.instant();
        rooms.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)]);
        }
        return code.toString();
    }

    private String normalize(String code) {
        if (code == null) {
            throw new IllegalArgumentException("room code is required");
        }
        String normalized = code.strip().toUpperCase(Locale.ROOT);
        if (normalized.length() != CODE_LENGTH) {
            throw new IllegalArgumentException("room code must be eight characters");
        }
        for (int index = 0; index < normalized.length(); index++) {
            if (new String(CODE_ALPHABET).indexOf(normalized.charAt(index)) < 0) {
                throw new IllegalArgumentException("room code contains invalid characters");
            }
        }
        return normalized;
    }

    private RoomView view(TransferRoom room) {
        return new RoomView(
                room.code(),
                room.expiresAt(),
                room.transferIds().size(),
                "/receive?room=" + room.code());
    }

    private static final class TransferRoom {
        private final String code;
        private final Instant expiresAt;
        private final LinkedHashSet<UUID> transferIds = new LinkedHashSet<>();

        private TransferRoom(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }

        private String code() {
            return code;
        }

        private Instant expiresAt() {
            return expiresAt;
        }

        private synchronized void attach(UUID transferId) {
            transferIds.remove(transferId);
            transferIds.add(transferId);
        }

        private synchronized Set<UUID> transferIds() {
            var newestFirst = new java.util.ArrayList<>(transferIds);
            java.util.Collections.reverse(newestFirst);
            return java.util.Collections.unmodifiableSet(new LinkedHashSet<>(newestFirst));
        }
    }
}

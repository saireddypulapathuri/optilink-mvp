package com.optilink.server;

import com.optilink.protocol.PacketIntegrityException;
import com.optilink.protocol.TransferChunk;
import com.optilink.protocol.TransferReassembler;
import com.optilink.transfer.QrTransferMvpService;
import com.optilink.transfer.RecoveredTransferPayload;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * In-memory live camera receiver sessions for browser-to-browser QR scanning.
 */
@Service
public final class ScanSessionService {
    private final QrTransferMvpService transferService;
    private final Map<UUID, ScanSession> sessions = new ConcurrentHashMap<>();
    private final Duration sessionTtl;
    private final int maxSessions;
    private final int maxEncodedImageCharacters;

    /**
     * Creates the scan session service.
     */
    @Autowired
    public ScanSessionService(
            @Value("${optilink.scans.ttl:PT20M}") Duration sessionTtl,
            @Value("${optilink.scans.max-active:2000}") int maxSessions,
            @Value("${optilink.scans.max-encoded-image-characters:2500000}") int maxEncodedImageCharacters) {
        this(new QrTransferMvpService(), sessionTtl, maxSessions, maxEncodedImageCharacters);
    }

    ScanSessionService(QrTransferMvpService transferService) {
        this(transferService, Duration.ofMinutes(20), 2_000, 2_500_000);
    }

    ScanSessionService(
            QrTransferMvpService transferService,
            Duration sessionTtl,
            int maxSessions,
            int maxEncodedImageCharacters) {
        this.transferService = Objects.requireNonNull(transferService, "transferService");
        this.sessionTtl = Objects.requireNonNull(sessionTtl, "sessionTtl");
        if (sessionTtl.isZero() || sessionTtl.isNegative()) {
            throw new IllegalArgumentException("sessionTtl must be positive");
        }
        if (maxSessions < 1 || maxEncodedImageCharacters < 1) {
            throw new IllegalArgumentException("scan limits must be positive");
        }
        this.maxSessions = maxSessions;
        this.maxEncodedImageCharacters = maxEncodedImageCharacters;
    }

    /**
     * Creates a new receiver scan session.
     *
     * @param request request body
     * @return session view
     */
    public ScanSessionView create(CreateScanSessionRequest request) {
        Objects.requireNonNull(request, "request");
        removeExpired();
        if (sessions.size() >= maxSessions) {
            throw new IllegalStateException("active scan capacity reached");
        }
        UUID sessionId = UUID.randomUUID();
        ScanSession session = new ScanSession(sessionId, requirePassphrase(request.passphrase()), Instant.now());
        sessions.put(sessionId, session);
        return view(session);
    }

    /**
     * Returns current receiver session state.
     *
     * @param sessionId session id
     * @return session view
     */
    public ScanSessionView get(UUID sessionId) {
        return view(existing(sessionId));
    }

    /**
     * Accepts one browser-captured frame image.
     *
     * @param sessionId session id
     * @param request request body
     * @return updated session view
     * @throws IOException when image bytes cannot be read
     */
    public ScanSessionView submit(UUID sessionId, SubmitScanFrameRequest request) throws IOException {
        Objects.requireNonNull(request, "request");
        ScanSession session = existing(sessionId);
        try {
            if (request.qrPayload() != null && request.qrPayload().length() > 4_096) {
                throw new IllegalArgumentException("QR payload exceeds scan size limit");
            }
            TransferChunk chunk = request.qrPayload() == null || request.qrPayload().isBlank()
                    ? transferService.decodeFrame(decodeImage(request.imageBase64()))
                    : transferService.decodeQrPayload(request.qrPayload());
            session.accept(chunk);
        } catch (PacketIntegrityException | IllegalArgumentException exception) {
            session.invalid();
        }
        return view(session);
    }

    /**
     * Decrypts a completed receiver session.
     *
     * @param sessionId session id
     * @return recovered payload response
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public ReceiveTransferResponse receive(UUID sessionId) throws GeneralSecurityException {
        ScanSession session = existing(sessionId);
        RecoveredTransferPayload payload = transferService.readTransferPayloadChunks(
                session.chunks(), session.passphrase());
        byte[] plaintext = payload.bytes();
        String text = utf8Text(plaintext).orElse(null);
        String base64 = Base64.getEncoder().encodeToString(plaintext);
        String contentType = payload.contentType();
        return new ReceiveTransferResponse(
                session.transferIdText(),
                payload.kind(),
                payload.name(),
                contentType,
                plaintext.length,
                text,
                base64,
                "data:" + contentType + ";base64," + base64,
                text != null,
                contentType.startsWith("image/") || contentType.startsWith("video/"));
    }

    /**
     * Returns the number of active receiver sessions.
     *
     * @return active scans
     */
    public int activeSessionCount() {
        removeExpired();
        return sessions.size();
    }

    private ScanSession existing(UUID sessionId) {
        ScanSession session = sessions.get(Objects.requireNonNull(sessionId, "sessionId"));
        if (session == null || session.createdAt().plus(sessionTtl).isBefore(Instant.now())) {
            sessions.remove(sessionId);
            throw new IllegalArgumentException("scan session not found or expired");
        }
        return session;
    }

    private void removeExpired() {
        Instant cutoff = Instant.now().minus(sessionTtl);
        sessions.entrySet().removeIf(entry -> entry.getValue().createdAt().isBefore(cutoff));
    }

    private ScanSessionView view(ScanSession session) {
        return new ScanSessionView(
                session.sessionId().toString(),
                session.transferIdText(),
                session.acceptedFrames(),
                session.expectedFrames(),
                session.complete(),
                session.invalidFrames(),
                session.duplicateFrames(),
                session.missingFrameNumbers(),
                session.createdAt(),
                "/api/scans/" + session.sessionId() + "/receive");
    }

    private byte[] decodeImage(String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new IllegalArgumentException("imageBase64 is required");
        }
        if (imageBase64.length() > maxEncodedImageCharacters) {
            throw new IllegalArgumentException("camera frame exceeds scan size limit");
        }
        int comma = imageBase64.indexOf(',');
        String payload = comma >= 0 ? imageBase64.substring(comma + 1) : imageBase64;
        return Base64.getDecoder().decode(payload);
    }

    private char[] requirePassphrase(String passphrase) {
        if (passphrase == null || passphrase.length() < 8) {
            throw new IllegalArgumentException("passphrase must be at least 8 characters");
        }
        return passphrase.toCharArray();
    }

    private Optional<String> utf8Text(byte[] plaintext) {
        try {
            return Optional.of(StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(plaintext))
                    .toString());
        } catch (CharacterCodingException exception) {
            return Optional.empty();
        }
    }

    private static final class ScanSession {
        private final UUID sessionId;
        private final char[] passphrase;
        private final Instant createdAt;
        private final TransferReassembler reassembler = new TransferReassembler();
        private int invalidFrames;
        private int duplicateFrames;

        private ScanSession(UUID sessionId, char[] passphrase, Instant createdAt) {
            this.sessionId = sessionId;
            this.passphrase = passphrase.clone();
            this.createdAt = createdAt;
        }

        private synchronized void accept(TransferChunk chunk) {
            int acceptedBefore = reassembler.acceptedChunks();
            reassembler.accept(chunk);
            if (reassembler.acceptedChunks() == acceptedBefore) {
                duplicateFrames++;
            }
        }

        private synchronized void invalid() {
            invalidFrames++;
        }

        private UUID sessionId() {
            return sessionId;
        }

        private synchronized char[] passphrase() {
            return passphrase.clone();
        }

        private Instant createdAt() {
            return createdAt;
        }

        private synchronized List<TransferChunk> chunks() {
            return reassembler.chunks();
        }

        private synchronized int acceptedFrames() {
            return reassembler.acceptedChunks();
        }

        private synchronized int expectedFrames() {
            return reassembler.expectedChunks();
        }

        private synchronized boolean complete() {
            return reassembler.isComplete();
        }

        private synchronized int invalidFrames() {
            return invalidFrames;
        }

        private synchronized int duplicateFrames() {
            return duplicateFrames;
        }

        private synchronized List<Integer> missingFrameNumbers() {
            return reassembler.missingIndexes().stream().map(index -> index + 1).toList();
        }

        private synchronized String transferIdText() {
            return reassembler.transferId().map(UUID::toString).orElse(null);
        }
    }
}

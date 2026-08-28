package com.optilink.server;

import com.optilink.transfer.MvpTransferInspection;
import com.optilink.transfer.MvpTransferSummary;
import com.optilink.transfer.QrTransferMvpService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

/**
 * Local product workflow for creating, inspecting, serving, and receiving QR transfers.
 */
@Service
public final class ProductTransferService {
    private static final String PRODUCT_METADATA_FILE = "optilink-product.properties";
    private static final String TRANSFER_MANIFEST_FILE = "optilink-transfer.properties";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String TEXT_PLAIN_UTF_8 = "text/plain;charset=utf-8";
    private static final String IMAGE_PNG = "image/png";
    private static final String VIDEO_MP4 = "video/mp4";
    private static final int DEFAULT_MAX_TRANSFER_BYTES = 128 * 1024;
    private static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(
            "[a-z0-9][a-z0-9!#$&^_.+-]*/[a-z0-9][a-z0-9!#$&^_.+-]*(;[a-z0-9._-]+=[a-z0-9._+-]+)*");

    private final Path rootDirectory;
    private final QrTransferMvpService transferService;
    private final int maxTransferBytes;

    /**
     * Creates the transfer product service.
     *
     * @param rootDirectory transfer storage root
     * @param maxTransferBytes maximum payload bytes per transfer
     */
    @Autowired
    public ProductTransferService(
            @Value("${optilink.transfer.root:target/optilink-product-transfers}") String rootDirectory,
            @Value("${optilink.transfer.max-payload-bytes:131072}") int maxTransferBytes) {
        this(Path.of(rootDirectory), new QrTransferMvpService(), maxTransferBytes);
    }

    ProductTransferService(Path rootDirectory, QrTransferMvpService transferService) {
        this(rootDirectory, transferService, DEFAULT_MAX_TRANSFER_BYTES);
    }

    ProductTransferService(Path rootDirectory, QrTransferMvpService transferService, int maxTransferBytes) {
        this.rootDirectory = rootDirectory.toAbsolutePath().normalize();
        this.transferService = Objects.requireNonNull(transferService, "transferService");
        if (maxTransferBytes < 1) {
            throw new IllegalArgumentException("maxTransferBytes must be positive");
        }
        this.maxTransferBytes = maxTransferBytes;
    }

    /**
     * Creates a text transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    public TransferView createText(CreateTextTransferRequest request) throws IOException, GeneralSecurityException {
        Objects.requireNonNull(request, "request");
        byte[] plaintext = requireText(request.text()).getBytes(StandardCharsets.UTF_8);
        return create("text", "Text message", TEXT_PLAIN_UTF_8, plaintext, request.passphrase());
    }

    /**
     * Creates a file transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    public TransferView createFile(CreateFileTransferRequest request) throws IOException, GeneralSecurityException {
        return createBinary("file", request, APPLICATION_OCTET_STREAM);
    }

    /**
     * Creates an image transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    public TransferView createImage(CreateFileTransferRequest request) throws IOException, GeneralSecurityException {
        return createBinary("image", request, IMAGE_PNG);
    }

    /**
     * Creates a video transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    public TransferView createVideo(CreateFileTransferRequest request) throws IOException, GeneralSecurityException {
        return createBinary("video", request, VIDEO_MP4);
    }

    /**
     * Lists generated transfers.
     *
     * @return transfer views sorted newest first
     * @throws IOException when storage cannot be read
     */
    public List<TransferView> listTransfers() throws IOException {
        if (!Files.exists(rootDirectory)) {
            return List.of();
        }
        try (var paths = Files.list(rootDirectory)) {
            return paths
                    .filter(Files::isDirectory)
                    .flatMap(path -> transferId(path).stream())
                    .map(this::view)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparing(TransferView::createdAt).reversed())
                    .toList();
        }
    }

    /**
     * Returns one generated transfer view.
     *
     * @param transferId transfer id
     * @return transfer view
     * @throws IOException when storage cannot be read
     */
    public TransferView getTransfer(UUID transferId) throws IOException {
        return view(transferId).orElseThrow(() -> new NoSuchFileException(transferId.toString()));
    }

    /**
     * Inspects a generated transfer without decrypting it.
     *
     * @param transferId transfer id
     * @return inspection view
     * @throws IOException when storage cannot be read
     */
    public TransferInspectionView inspect(UUID transferId) throws IOException {
        MvpTransferInspection inspection = transferService.inspectTransfer(existingDirectory(transferId));
        return new TransferInspectionView(
                inspection.transferId().map(UUID::toString).orElse("unknown"),
                inspection.framesRead(),
                inspection.decodedChunks(),
                inspection.uniqueChunks(),
                inspection.expectedChunks(),
                inspection.duplicateFrames(),
                inspection.invalidFrames(),
                inspection.complete(),
                inspection.missingFrameNumbers());
    }

    /**
     * Recovers plaintext from a generated transfer.
     *
     * @param transferId transfer id
     * @param request receive request
     * @return recovered plaintext response
     * @throws IOException when storage cannot be read
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public ReceiveTransferResponse receive(UUID transferId, ReceiveTransferRequest request)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(request, "request");
        Path directory = existingDirectory(transferId);
        TransferMetadata metadata = metadata(directory, transferId);
        byte[] plaintext = transferService.readTransfer(directory, requirePassphrase(request.passphrase()));
        String text = utf8Text(plaintext).orElse(null);
        String base64 = Base64.getEncoder().encodeToString(plaintext);
        return new ReceiveTransferResponse(
                transferId.toString(),
                metadata.kind(),
                metadata.name(),
                metadata.contentType(),
                plaintext.length,
                text,
                base64,
                dataUrl(metadata.contentType(), base64),
                text != null,
                isMediaContentType(metadata.contentType()));
    }

    /**
     * Resolves a generated transfer file for serving.
     *
     * @param transferId transfer id
     * @param fileName requested file name
     * @return file view
     * @throws IOException when the file is invalid or missing
     */
    public TransferFile file(UUID transferId, String fileName) throws IOException {
        String safeName = requireSafeFileName(fileName);
        Path directory = existingDirectory(transferId);
        Path file = directory.resolve(safeName).normalize();
        if (!file.startsWith(directory) || !Files.isRegularFile(file)) {
            throw new NoSuchFileException(safeName);
        }
        return new TransferFile(file, mediaType(safeName));
    }

    private TransferView createBinary(String kind, CreateFileTransferRequest request, String fallbackContentType)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(request, "request");
        byte[] plaintext = decodeBase64(request.contentBase64());
        String fileName = safeDisplayName(request.fileName());
        String contentType = contentType(request.contentType(), fileName, fallbackContentType);
        requireContentTypeForKind(kind, contentType);
        return create(kind, fileName, contentType, plaintext, request.passphrase());
    }

    private TransferView create(String kind, String name, String contentType, byte[] plaintext, String passphrase)
            throws IOException, GeneralSecurityException {
        requirePayload(plaintext);
        char[] secret = requirePassphrase(passphrase);
        UUID transferId = UUID.randomUUID();
        Path directory = createDirectory(transferId);
        MvpTransferSummary summary = transferService.writeProductTransfer(
                directory, transferId, plaintext, secret, kind, name, contentType);
        writeProductMetadata(directory, kind, name, contentType, plaintext.length);
        return view(transferId, summary.frameCount()).orElseThrow(() -> new NoSuchFileException(transferId.toString()));
    }

    private Optional<TransferView> view(UUID transferId) {
        try {
            return view(transferId, frameCount(resolveDirectory(transferId)));
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private Optional<TransferView> view(UUID transferId, int frameCount) throws IOException {
        Path directory = resolveDirectory(transferId);
        if (!Files.isDirectory(directory)) {
            return Optional.empty();
        }
        TransferMetadata metadata = metadata(directory, transferId);
        String id = transferId.toString();
        return Optional.of(new TransferView(
                id,
                metadata.kind(),
                metadata.name(),
                metadata.contentType(),
                metadata.plaintextByteCount(),
                frameCount,
                metadata.createdAt(),
                "/transfers/" + id + "/index.html",
                "/transfers/" + id + "/" + TRANSFER_MANIFEST_FILE,
                "/api/transfers/" + id + "/inspect",
                "/api/transfers/" + id + "/receive"));
    }

    private void writeProductMetadata(Path directory, String kind, String name, String contentType, int plaintextByteCount)
            throws IOException {
        Properties properties = new Properties();
        properties.setProperty("kind", kind);
        properties.setProperty("name", name);
        properties.setProperty("contentType", contentType);
        properties.setProperty("plaintextByteCount", Integer.toString(plaintextByteCount));
        properties.setProperty("createdAt", Instant.now().toString());
        try (OutputStream output = Files.newOutputStream(directory.resolve(PRODUCT_METADATA_FILE))) {
            properties.store(output, "OptiLink product transfer metadata");
        }
    }

    private int frameCount(Path directory) throws IOException {
        Properties properties = readProperties(directory.resolve(TRANSFER_MANIFEST_FILE));
        return parseInt(properties.getProperty("frameCount"), 0);
    }

    private Properties readProperties(Path path) throws IOException {
        Properties properties = new Properties();
        if (!Files.isRegularFile(path)) {
            return properties;
        }
        try (var input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return properties;
    }

    private TransferMetadata metadata(Path directory, UUID transferId) throws IOException {
        Properties properties = readProperties(directory.resolve(PRODUCT_METADATA_FILE));
        String kind = properties.getProperty("kind", "transfer");
        String name = properties.getProperty("name", transferId.toString());
        String contentType = metadataContentType(properties, kind, name);
        int byteCount = parseInt(properties.getProperty("plaintextByteCount"), 0);
        Instant createdAt = properties.containsKey("createdAt")
                ? Instant.parse(properties.getProperty("createdAt"))
                : Files.getLastModifiedTime(directory).toInstant();
        return new TransferMetadata(kind, name, contentType, byteCount, createdAt);
    }

    private Optional<UUID> transferId(Path path) {
        try {
            return Optional.of(UUID.fromString(path.getFileName().toString()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Path createDirectory(UUID transferId) throws IOException {
        Files.createDirectories(rootDirectory);
        Path directory = resolveDirectory(transferId);
        Files.createDirectories(directory);
        return directory;
    }

    private Path existingDirectory(UUID transferId) throws IOException {
        Path directory = resolveDirectory(transferId);
        if (!Files.isDirectory(directory)) {
            throw new NoSuchFileException(transferId.toString());
        }
        return directory;
    }

    private Path resolveDirectory(UUID transferId) throws IOException {
        Path directory = rootDirectory.resolve(Objects.requireNonNull(transferId, "transferId").toString()).normalize();
        if (!directory.startsWith(rootDirectory)) {
            throw new IOException("transfer path escapes storage root");
        }
        return directory;
    }

    private String requireText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required");
        }
        return text;
    }

    private byte[] decodeBase64(String contentBase64) {
        if (contentBase64 == null || contentBase64.isBlank()) {
            throw new IllegalArgumentException("file content is required");
        }
        return Base64.getDecoder().decode(contentBase64);
    }

    private String safeDisplayName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }
        String normalized = fileName.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String candidate = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        if (candidate.isBlank()) {
            return "file";
        }
        return candidate
                .replace('\r', '_')
                .replace('\n', '_')
                .replace('\t', '_');
    }

    private String contentType(String supplied, String fileName, String fallback) {
        if (supplied != null && !supplied.isBlank()) {
            return normalizedContentType(supplied);
        }
        return contentTypeFromExtension(fileName).orElse(fallback);
    }

    private String metadataContentType(Properties properties, String kind, String name) {
        String stored = properties.getProperty("contentType");
        if (stored != null && !stored.isBlank()) {
            try {
                return normalizedContentType(stored);
            } catch (IllegalArgumentException exception) {
                return APPLICATION_OCTET_STREAM;
            }
        }
        return contentTypeFromExtension(name).orElse(defaultContentType(kind));
    }

    private String normalizedContentType(String contentType) {
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        if (!CONTENT_TYPE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("invalid content type");
        }
        return normalized;
    }

    private Optional<String> contentTypeFromExtension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return Optional.of(IMAGE_PNG);
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return Optional.of("image/jpeg");
        }
        if (lower.endsWith(".gif")) {
            return Optional.of("image/gif");
        }
        if (lower.endsWith(".webp")) {
            return Optional.of("image/webp");
        }
        if (lower.endsWith(".svg")) {
            return Optional.of("image/svg+xml");
        }
        if (lower.endsWith(".mp4") || lower.endsWith(".m4v")) {
            return Optional.of(VIDEO_MP4);
        }
        if (lower.endsWith(".webm")) {
            return Optional.of("video/webm");
        }
        if (lower.endsWith(".mov")) {
            return Optional.of("video/quicktime");
        }
        if (lower.endsWith(".ogv")) {
            return Optional.of("video/ogg");
        }
        return Optional.empty();
    }

    private String defaultContentType(String kind) {
        return switch (kind) {
            case "text" -> TEXT_PLAIN_UTF_8;
            case "image" -> IMAGE_PNG;
            case "video" -> VIDEO_MP4;
            default -> APPLICATION_OCTET_STREAM;
        };
    }

    private void requireContentTypeForKind(String kind, String contentType) {
        if ("image".equals(kind) && !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("image transfer requires an image content type");
        }
        if ("video".equals(kind) && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("video transfer requires a video content type");
        }
    }

    private String dataUrl(String contentType, String base64) {
        return "data:" + contentType + ";base64," + base64;
    }

    private boolean isMediaContentType(String contentType) {
        return contentType.startsWith("image/") || contentType.startsWith("video/");
    }

    private void requirePayload(byte[] plaintext) {
        if (plaintext.length == 0) {
            throw new IllegalArgumentException("payload must not be empty");
        }
        if (plaintext.length > maxTransferBytes) {
            throw new IllegalArgumentException("payload exceeds " + maxTransferBytes + " bytes");
        }
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

    private String requireSafeFileName(String fileName) {
        if (fileName == null
                || fileName.isBlank()
                || fileName.contains("/")
                || fileName.contains("\\")
                || fileName.contains("..")) {
            throw new IllegalArgumentException("invalid transfer file name");
        }
        return fileName;
    }

    private MediaType mediaType(String fileName) {
        if (fileName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (fileName.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        }
        if (fileName.endsWith(".properties")) {
            return MediaType.TEXT_PLAIN;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private record TransferMetadata(
            String kind,
            String name,
            String contentType,
            int plaintextByteCount,
            Instant createdAt) {
    }
}

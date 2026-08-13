package com.optilink.transfer;

import com.optilink.core.BinaryPayload;
import com.optilink.decoder.PacketSymbolDecoder;
import com.optilink.decoder.QrSymbolImageDecoder;
import com.optilink.encoder.PacketSymbolEncoder;
import com.optilink.encoder.QrSymbolRenderer;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.optical.RenderedOpticalFrame;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketIntegrityException;
import com.optilink.protocol.PacketType;
import com.optilink.protocol.TransferChunk;
import com.optilink.protocol.TransferChunkCodec;
import com.optilink.protocol.TransferChunker;
import com.optilink.protocol.TransferReassembler;
import com.optilink.security.PassphraseSealedBox;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Practical MVP service for passphrase-sealed one-way QR optical transfers.
 */
public final class QrTransferMvpService {
    private static final String FRAME_PATTERN = "frame-%04d-of-%04d.png";
    private static final String MANIFEST_FILE = "optilink-transfer.properties";
    private static final String PNG_MIME_TYPE = "image/png";
    private static final int DISPLAY_FRAME_PERIOD_MILLIS = 650;
    private static final int QR_SAFE_PACKET_PAYLOAD_BYTES = 300;
    private static final OpticalProfile PROFILE =
            new OpticalProfile("mvp-qr", 4, 25, ErrorCorrectionLevel.M, 90, QR_SAFE_PACKET_PAYLOAD_BYTES);

    private final PassphraseSealedBox sealedBox = new PassphraseSealedBox();
    private final TransferChunker chunker = new TransferChunker();
    private final TransferChunkCodec chunkCodec = new TransferChunkCodec();
    private final BinaryPacketCodec packetCodec = new BinaryPacketCodec();
    private final PacketSymbolEncoder packetEncoder = new PacketSymbolEncoder(packetCodec);
    private final PacketSymbolDecoder packetDecoder = new PacketSymbolDecoder(packetCodec);
    private final QrSymbolRenderer renderer = new QrSymbolRenderer();
    private final QrSymbolImageDecoder imageDecoder = new QrSymbolImageDecoder();
    private final TransferDisplayPageWriter pageWriter = new TransferDisplayPageWriter();

    /**
     * Writes encrypted QR frames and a local display page.
     *
     * @param outputDirectory output directory
     * @param plaintext plaintext bytes
     * @param passphrase operator passphrase
     * @return transfer summary
     * @throws IOException when writing fails
     * @throws GeneralSecurityException when sealing fails
     */
    public MvpTransferSummary writeTransfer(Path outputDirectory, byte[] plaintext, char[] passphrase)
            throws IOException, GeneralSecurityException {
        return writeTransfer(outputDirectory, UUID.randomUUID(), plaintext, passphrase);
    }

    /**
     * Writes encrypted QR frames and a local display page using a caller-provided transfer id.
     *
     * @param outputDirectory output directory
     * @param transferId transfer identifier
     * @param plaintext plaintext bytes
     * @param passphrase operator passphrase
     * @return transfer summary
     * @throws IOException when writing fails
     * @throws GeneralSecurityException when sealing fails
     */
    public MvpTransferSummary writeTransfer(Path outputDirectory, UUID transferId, byte[] plaintext, char[] passphrase)
            throws IOException, GeneralSecurityException {
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        byte[] sealedBytes = sealedBox.encode(sealedBox.seal(Objects.requireNonNull(plaintext, "plaintext"), passphrase));
        List<TransferChunk> chunks = chunker.chunk(Objects.requireNonNull(transferId, "transferId"), sealedBytes, maxChunkBytes());
        Files.createDirectories(outputDirectory);
        List<String> frameFiles = writeFrames(outputDirectory, chunks);
        Path manifest = writeManifest(outputDirectory, transferId, chunks.size(), sealedBytes.length);
        Path page = pageWriter.write(outputDirectory, frameFiles);
        return new MvpTransferSummary(transferId, chunks.size(), outputDirectory, page, manifest);
    }

    /**
     * Reads encrypted QR frames from a directory and recovers plaintext.
     *
     * @param inputDirectory directory containing QR PNG frames
     * @param passphrase operator passphrase
     * @return plaintext bytes
     * @throws IOException when reading fails
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public byte[] readTransfer(Path inputDirectory, char[] passphrase)
            throws IOException, GeneralSecurityException {
        TransferReassembler reassembler = new TransferReassembler();
        for (Path framePath : framePaths(inputDirectory)) {
            reassembler.accept(readChunk(framePath));
        }
        return readTransfer(reassembler, passphrase);
    }

    /**
     * Recovers plaintext from already decoded transfer chunks.
     *
     * @param chunks decoded chunks
     * @param passphrase operator passphrase
     * @return plaintext bytes
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    public byte[] readTransferChunks(Collection<TransferChunk> chunks, char[] passphrase)
            throws GeneralSecurityException {
        TransferReassembler reassembler = new TransferReassembler();
        Objects.requireNonNull(chunks, "chunks").forEach(reassembler::accept);
        return readTransfer(reassembler, passphrase);
    }

    /**
     * Decodes one camera-captured or generated QR frame into a transfer chunk.
     *
     * @param imageBytes PNG, JPEG, or browser canvas image bytes
     * @return decoded transfer chunk
     * @throws IOException when the image cannot be read
     */
    public TransferChunk decodeFrame(byte[] imageBytes) throws IOException {
        return decodeChunk(readFrame(Objects.requireNonNull(imageBytes, "imageBytes")));
    }

    /**
     * Inspects a QR transfer directory without decrypting the payload.
     *
     * @param inputDirectory directory containing QR PNG frames
     * @return transfer diagnostics
     * @throws IOException when the directory cannot be read
     */
    public MvpTransferInspection inspectTransfer(Path inputDirectory) throws IOException {
        TransferReassembler reassembler = new TransferReassembler();
        int framesRead = 0;
        int decodedChunks = 0;
        int duplicateFrames = 0;
        int invalidFrames = 0;
        for (Path framePath : framePaths(inputDirectory)) {
            framesRead++;
            try {
                TransferChunk chunk = readChunk(framePath);
                int acceptedBefore = reassembler.acceptedChunks();
                reassembler.accept(chunk);
                if (reassembler.acceptedChunks() == acceptedBefore) {
                    duplicateFrames++;
                } else {
                    decodedChunks++;
                }
            } catch (PacketIntegrityException | IOException exception) {
                invalidFrames++;
            }
        }
        return new MvpTransferInspection(
                reassembler.transferId(),
                framesRead,
                decodedChunks,
                reassembler.acceptedChunks(),
                reassembler.expectedChunks(),
                duplicateFrames,
                invalidFrames,
                oneBased(reassembler.missingIndexes()));
    }

    private int maxChunkBytes() {
        return PROFILE.maxPayloadBytes() - chunkCodec.encodedOverheadBytes();
    }

    private List<String> writeFrames(Path outputDirectory, List<TransferChunk> chunks) throws IOException {
        List<String> frameFiles = new ArrayList<>(chunks.size());
        for (TransferChunk chunk : chunks) {
            String fileName = FRAME_PATTERN.formatted(chunk.index() + 1, chunk.totalChunks());
            Files.write(outputDirectory.resolve(fileName), renderedFrame(chunk).imageBytes());
            frameFiles.add(fileName);
        }
        return List.copyOf(frameFiles);
    }

    private Path writeManifest(Path outputDirectory, UUID transferId, int frameCount, int sealedByteCount)
            throws IOException {
        Properties properties = new Properties();
        properties.setProperty("format", "optilink.mvp.qr.v1");
        properties.setProperty("transferId", transferId.toString());
        properties.setProperty("frameCount", Integer.toString(frameCount));
        properties.setProperty("profile", PROFILE.name());
        properties.setProperty("maxChunkBytes", Integer.toString(maxChunkBytes()));
        properties.setProperty("sealedByteCount", Integer.toString(sealedByteCount));
        properties.setProperty("displayFramePeriodMillis", Integer.toString(DISPLAY_FRAME_PERIOD_MILLIS));
        Path manifest = outputDirectory.resolve(MANIFEST_FILE);
        try (OutputStream output = Files.newOutputStream(manifest)) {
            properties.store(output, "OptiLink MVP QR transfer manifest");
        }
        return manifest;
    }

    private RenderedOpticalFrame renderedFrame(TransferChunk chunk) {
        BinaryPayload payload = new BinaryPayload(chunkCodec.encode(chunk));
        OpticalPacket packet = new OpticalPacket(header(chunk, payload.length()), payload);
        return renderer.render(packetEncoder.encode(packet, PROFILE));
    }

    private PacketHeader header(TransferChunk chunk, int payloadLength) {
        return new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                chunk.transferId(),
                chunk.index() + 1L,
                0,
                payloadLength,
                0);
    }

    private TransferChunk readChunk(Path framePath) throws IOException {
        return decodeChunk(readFrame(framePath));
    }

    private TransferChunk decodeChunk(RenderedOpticalFrame frame) {
        OpticalPacket packet = packetDecoder.decode(imageDecoder.decode(frame));
        TransferChunk chunk = chunkCodec.decode(packet.payload().bytes());
        verifyPacketMatchesChunk(packet, chunk);
        return chunk;
    }

    private RenderedOpticalFrame readFrame(Path framePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(framePath);
        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("unsupported optical frame image: " + framePath);
        }
        return new RenderedOpticalFrame(PROFILE, PNG_MIME_TYPE, image.getWidth(), image.getHeight(), imageBytes);
    }

    private RenderedOpticalFrame readFrame(byte[] imageBytes) throws IOException {
        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("unsupported optical frame image");
        }
        return new RenderedOpticalFrame(PROFILE, PNG_MIME_TYPE, image.getWidth(), image.getHeight(), imageBytes);
    }

    private List<Path> framePaths(Path inputDirectory) throws IOException {
        try (var stream = Files.list(Objects.requireNonNull(inputDirectory, "inputDirectory"))) {
            List<Path> paths = stream
                    .filter(path -> path.getFileName().toString().endsWith(".png"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
            if (paths.isEmpty()) {
                throw new IOException("no QR frame PNG files found in " + inputDirectory);
            }
            return paths;
        }
    }

    private void verifyComplete(TransferReassembler reassembler) {
        if (!reassembler.isComplete()) {
            throw new PacketIntegrityException("transfer is incomplete; missing frame(s): "
                    + missingFrameText(oneBased(reassembler.missingIndexes())));
        }
    }

    private byte[] readTransfer(TransferReassembler reassembler, char[] passphrase)
            throws GeneralSecurityException {
        verifyComplete(reassembler);
        return sealedBox.open(sealedBox.decode(reassembler.assemble()), passphrase);
    }

    private List<Integer> oneBased(List<Integer> zeroBasedIndexes) {
        return zeroBasedIndexes.stream().map(index -> index + 1).toList();
    }

    private String missingFrameText(List<Integer> missingFrames) {
        if (missingFrames.isEmpty()) {
            return "unknown";
        }
        return missingFrames.toString();
    }

    private void verifyPacketMatchesChunk(OpticalPacket packet, TransferChunk chunk) {
        if (!packet.header().sessionId().equals(chunk.transferId())
                || packet.header().sequenceNumber() != chunk.index() + 1L) {
            throw new PacketIntegrityException("transfer packet metadata does not match chunk");
        }
    }
}

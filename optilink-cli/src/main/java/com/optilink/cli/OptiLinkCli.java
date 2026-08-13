package com.optilink.cli;

import com.optilink.core.BinaryPayload;
import com.optilink.encoder.PacketSymbolEncoder;
import com.optilink.encoder.QrSymbolRenderer;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.protocol.BinaryPacketCodec;
import com.optilink.protocol.OpticalPacket;
import com.optilink.protocol.PacketHeader;
import com.optilink.protocol.PacketType;
import com.optilink.transfer.MvpTransferInspection;
import com.optilink.transfer.MvpTransferSummary;
import com.optilink.transfer.QrTransferMvpService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Command-line entry point for smoke tests and MVP QR optical transfers.
 */
public final class OptiLinkCli {
    private static final String DEFAULT_PASSPHRASE_ENV = "OPTILINK_PASSPHRASE";

    private OptiLinkCli() {
    }

    /**
     * Runs the CLI.
     *
     * @param args command arguments
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        if (args.length >= 2 && "--qr".equals(args[0])) {
            writeQr(Path.of(args[1]), message(args, 2));
            return;
        }
        if (args.length > 0 && "send".equals(args[0])) {
            send(options(args, 1));
            return;
        }
        if (args.length > 0 && "receive".equals(args[0])) {
            receive(options(args, 1));
            return;
        }
        if (args.length > 0 && "inspect".equals(args[0])) {
            inspect(options(args, 1));
            return;
        }
        if (args.length > 0 && "--help".equals(args[0])) {
            System.out.println(usage());
            return;
        }
        String message = message(args, 0);
        BinaryPayload payload = new BinaryPayload(message.getBytes(StandardCharsets.UTF_8));
        OpticalPacket packet = packet(payload);
        OpticalProfile profile = new OpticalProfile("cli", 10, 10, ErrorCorrectionLevel.Q, 80, 512);
        var symbol = new PacketSymbolEncoder(new BinaryPacketCodec()).encode(packet, profile);
        System.out.println("encoded-symbol-bytes=" + symbol.frameBytes().length);
    }

    private static void send(Map<String, String> options) throws IOException, GeneralSecurityException {
        Path outputDirectory = Path.of(required(options, "--out"));
        byte[] plaintext = plaintext(options);
        char[] passphrase = passphrase(options);
        MvpTransferSummary summary = new QrTransferMvpService().writeTransfer(outputDirectory, plaintext, passphrase);
        System.out.printf("transfer-id=%s frames=%d display=%s manifest=%s%n",
                summary.transferId(),
                summary.frameCount(),
                summary.displayPage().toAbsolutePath(),
                summary.manifest().toAbsolutePath());
    }

    private static void receive(Map<String, String> options) throws IOException, GeneralSecurityException {
        byte[] plaintext = new QrTransferMvpService().readTransfer(Path.of(required(options, "--in")), passphrase(options));
        if (options.containsKey("--out")) {
            writeOutput(Path.of(options.get("--out")), plaintext);
            return;
        }
        System.out.println(new String(plaintext, StandardCharsets.UTF_8));
    }

    private static void inspect(Map<String, String> options) throws IOException {
        MvpTransferInspection inspection =
                new QrTransferMvpService().inspectTransfer(Path.of(required(options, "--in")));
        System.out.println("transfer-id=" + inspection.transferId().map(UUID::toString).orElse("unknown"));
        System.out.println("frames-read=" + inspection.framesRead());
        System.out.println("decoded-chunks=" + inspection.decodedChunks());
        System.out.println("unique-chunks=" + inspection.uniqueChunks());
        System.out.println("expected-chunks=" + inspection.expectedChunks());
        System.out.println("duplicate-frames=" + inspection.duplicateFrames());
        System.out.println("invalid-frames=" + inspection.invalidFrames());
        System.out.println("complete=" + inspection.complete());
        System.out.println("missing-frames=" + missingFrames(inspection));
    }

    private static void writeQr(Path outputPath, String message) throws IOException {
        BinaryPacketCodec codec = new BinaryPacketCodec();
        BinaryPayload payload = new BinaryPayload(message.getBytes(StandardCharsets.UTF_8));
        OpticalProfile profile = new OpticalProfile("cli-qr", 10, 18, ErrorCorrectionLevel.M, 85, 512);
        var symbol = new PacketSymbolEncoder(codec).encode(packet(payload), profile);
        var frame = new QrSymbolRenderer().render(symbol);
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(outputPath, frame.imageBytes());
        System.out.println("qr-frame=" + outputPath.toAbsolutePath());
    }

    private static String message(String[] args, int startIndex) {
        return args.length <= startIndex ? "OptiLink" : String.join(" ", java.util.Arrays.copyOfRange(args, startIndex, args.length));
    }

    private static Map<String, String> options(String[] args, int startIndex) {
        Map<String, String> options = new HashMap<>();
        for (int index = startIndex; index < args.length; index += 2) {
            if (index + 1 >= args.length || !args[index].startsWith("--")) {
                throw new IllegalArgumentException("invalid command options\n" + usage());
            }
            options.put(args[index], args[index + 1]);
        }
        return Map.copyOf(options);
    }

    private static byte[] plaintext(Map<String, String> options) throws IOException {
        if (options.containsKey("--file")) {
            return Files.readAllBytes(Path.of(options.get("--file")));
        }
        return required(options, "--text").getBytes(StandardCharsets.UTF_8);
    }

    private static char[] passphrase(Map<String, String> options) {
        String envName = options.getOrDefault("--passphrase-env", DEFAULT_PASSPHRASE_ENV);
        String value = System.getenv(envName);
        if (value == null || value.length() < 8) {
            throw new IllegalArgumentException("set " + envName + " to a passphrase with at least 8 characters");
        }
        return value.toCharArray();
    }

    private static String required(Map<String, String> options, String name) {
        String value = options.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("missing required option " + name + "\n" + usage());
        }
        return value;
    }

    private static void writeOutput(Path outputPath, byte[] plaintext) throws IOException {
        Path parent = outputPath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(outputPath, plaintext);
        System.out.println("output=" + outputPath.toAbsolutePath());
    }

    private static String usage() {
        return """
                Usage:
                  optilink-cli --qr <output.png> <message>
                  optilink-cli send --out <dir> --text <message> [--passphrase-env OPTILINK_PASSPHRASE]
                  optilink-cli send --out <dir> --file <path> [--passphrase-env OPTILINK_PASSPHRASE]
                  optilink-cli receive --in <dir> [--out <path>] [--passphrase-env OPTILINK_PASSPHRASE]
                  optilink-cli inspect --in <dir>
                """;
    }

    private static String missingFrames(MvpTransferInspection inspection) {
        return inspection.missingFrameNumbers().isEmpty() ? "none" : inspection.missingFrameNumbers().toString();
    }

    private static OpticalPacket packet(BinaryPayload payload) {
        PacketHeader header = new PacketHeader(
                BinaryPacketCodec.CURRENT_VERSION,
                PacketType.DATA,
                0,
                UUID.randomUUID(),
                1,
                0,
                payload.length(),
                0);
        return new OpticalPacket(header, payload);
    }
}

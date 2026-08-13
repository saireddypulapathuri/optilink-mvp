package com.optilink.transfer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QrTransferMvpServiceTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void writesAndReadsEncryptedQrTransfer() throws Exception {
        QrTransferMvpService service = new QrTransferMvpService();
        byte[] message = "practical offline optical MVP".getBytes(StandardCharsets.UTF_8);
        char[] passphrase = "correct horse battery staple".toCharArray();

        MvpTransferSummary summary = service.writeTransfer(temporaryDirectory, message, passphrase);
        byte[] recovered = service.readTransfer(temporaryDirectory, passphrase);

        assertTrue(summary.frameCount() >= 1);
        assertTrue(Files.exists(summary.displayPage()));
        assertTrue(Files.exists(summary.manifest()));
        assertTrue(service.inspectTransfer(temporaryDirectory).complete());
        assertArrayEquals(message, recovered);
    }

    @Test
    void inspectionReportsMissingFramesWithoutPassphrase() throws Exception {
        QrTransferMvpService service = new QrTransferMvpService();
        byte[] message = "x".repeat(1_800).getBytes(StandardCharsets.UTF_8);

        service.writeTransfer(temporaryDirectory, message, "correct horse battery staple".toCharArray());
        List<Path> frames;
        try (var stream = Files.list(temporaryDirectory)) {
            frames = stream
                    .filter(path -> path.getFileName().toString().endsWith(".png"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
        Files.delete(frames.get(1));

        MvpTransferInspection inspection = service.inspectTransfer(temporaryDirectory);

        assertFalse(inspection.complete());
        assertEquals(List.of(2), inspection.missingFrameNumbers());
        assertEquals(0, inspection.invalidFrames());
    }
}

package com.optilink.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.optilink.transfer.QrTransferMvpService;

class TransferControllerTest {
    @TempDir
    private Path temporaryDirectory;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        ProductTransferService service = new ProductTransferService(temporaryDirectory, new QrTransferMvpService());
        ScanSessionService scanService = new ScanSessionService(new QrTransferMvpService());
        webTestClient = WebTestClient.bindToController(new TransferController(service, scanService)).build();
    }

    @Test
    void createsInspectsServesAndReceivesTextTransfer() {
        TransferView transfer = webTestClient.post()
                .uri("/api/transfers/text")
                .bodyValue(new CreateTextTransferRequest("product flow", "correct horse battery staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(transfer);
        assertTrue(transfer.frameCount() >= 1);

        webTestClient.get()
                .uri(transfer.displayUrl())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        TransferInspectionView inspection = webTestClient.get()
                .uri(transfer.inspectUrl())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferInspectionView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(inspection);
        assertTrue(inspection.complete());

        ReceiveTransferResponse response = webTestClient.post()
                .uri(transfer.receiveUrl())
                .bodyValue(new ReceiveTransferRequest("correct horse battery staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiveTransferResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("product flow", response.text());
        assertTrue(response.utf8Text());
    }

    @Test
    void servesTransmitterAndReceiverRoutes() {
        webTestClient.get()
                .uri("/send")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("sendLink")));

        webTestClient.get()
                .uri("/receive")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertTrue(body.contains("startScan")));
    }

    @Test
    void createsInspectsAndReceivesImageTransfer() {
        String imageBase64 =
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII=";

        TransferView transfer = webTestClient.post()
                .uri("/api/transfers/image")
                .bodyValue(new CreateFileTransferRequest(
                        "pixel.png",
                        "image/png",
                        imageBase64,
                        "correct horse image staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(transfer);
        assertEquals("image", transfer.kind());
        assertEquals("image/png", transfer.contentType());
        assertTrue(transfer.frameCount() >= 1);

        TransferInspectionView inspection = webTestClient.get()
                .uri(transfer.inspectUrl())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferInspectionView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(inspection);
        assertTrue(inspection.complete());

        ReceiveTransferResponse response = webTestClient.post()
                .uri(transfer.receiveUrl())
                .bodyValue(new ReceiveTransferRequest("correct horse image staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiveTransferResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("image", response.kind());
        assertEquals("pixel.png", response.name());
        assertEquals("image/png", response.contentType());
        assertEquals(imageBase64, response.base64());
        assertTrue(response.media());
        assertTrue(response.dataUrl().startsWith("data:image/png;base64,"));
    }

    @Test
    void createsInspectsAndReceivesVideoTransfer() {
        byte[] videoBytes = {
            0, 0, 0, 24, 'f', 't', 'y', 'p', 'm', 'p', '4', '2',
            0, 0, 0, 0, 'm', 'p', '4', '2', 'i', 's', 'o', 'm'
        };
        String videoBase64 = Base64.getEncoder().encodeToString(videoBytes);

        TransferView transfer = webTestClient.post()
                .uri("/api/transfers/video")
                .bodyValue(new CreateFileTransferRequest(
                        "clip.mp4",
                        "video/mp4",
                        videoBase64,
                        "correct horse video staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(transfer);
        assertEquals("video", transfer.kind());
        assertEquals("video/mp4", transfer.contentType());
        assertTrue(transfer.frameCount() >= 1);

        TransferInspectionView inspection = webTestClient.get()
                .uri(transfer.inspectUrl())
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferInspectionView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(inspection);
        assertTrue(inspection.complete());

        ReceiveTransferResponse response = webTestClient.post()
                .uri(transfer.receiveUrl())
                .bodyValue(new ReceiveTransferRequest("correct horse video staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiveTransferResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("video", response.kind());
        assertEquals("clip.mp4", response.name());
        assertEquals("video/mp4", response.contentType());
        assertEquals(videoBytes.length, response.byteCount());
        assertEquals(videoBase64, response.base64());
        assertTrue(response.media());
        assertTrue(response.dataUrl().startsWith("data:video/mp4;base64,"));
    }

    @Test
    void scansGeneratedFramesAndReceivesPayload() throws IOException {
        TransferView transfer = webTestClient.post()
                .uri("/api/transfers/text")
                .bodyValue(new CreateTextTransferRequest("scan me", "correct horse scan staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(transfer);

        ScanSessionView scan = webTestClient.post()
                .uri("/api/scans")
                .bodyValue(new CreateScanSessionRequest("correct horse scan staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ScanSessionView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(scan);

        Path transferDirectory = temporaryDirectory.resolve(transfer.transferId());
        try (var paths = Files.list(transferDirectory)) {
            for (Path frame : paths.filter(path -> path.getFileName().toString().endsWith(".png")).toList()) {
                String imageBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(frame));
                scan = webTestClient.post()
                        .uri("/api/scans/" + scan.sessionId() + "/frames")
                        .bodyValue(new SubmitScanFrameRequest(imageBase64))
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(ScanSessionView.class)
                        .returnResult()
                        .getResponseBody();
            }
        }

        assertNotNull(scan);
        assertTrue(scan.complete());
        assertEquals(transfer.transferId(), scan.transferId());

        ReceiveTransferResponse response = webTestClient.post()
                .uri(scan.receiveUrl())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReceiveTransferResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(response);
        assertEquals("scan me", response.text());
        assertTrue(response.utf8Text());
    }

    @Test
    void rejectsBadPassphrase() {
        TransferView transfer = webTestClient.post()
                .uri("/api/transfers/text")
                .bodyValue(new CreateTextTransferRequest("secret", "correct horse battery staple"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransferView.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(transfer);

        webTestClient.post()
                .uri(transfer.receiveUrl())
                .bodyValue(new ReceiveTransferRequest("wrong horse battery staple"))
                .exchange()
                .expectStatus().isBadRequest();
    }
}

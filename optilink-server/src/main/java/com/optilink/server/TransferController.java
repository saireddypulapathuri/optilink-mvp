package com.optilink.server;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product API for local QR transfer workflows.
 */
@RestController
public final class TransferController {
    private final ProductTransferService transfers;
    private final ScanSessionService scans;

    /**
     * Creates the transfer controller.
     *
     * @param transfers transfer workflow service
     */
    public TransferController(ProductTransferService transfers, ScanSessionService scans) {
        this.transfers = transfers;
        this.scans = scans;
    }

    /**
     * Serves the transmitter and receiver app shells.
     *
     * @return static app shell
     */
    @GetMapping(value = {"/send", "/send/", "/receive", "/receive/"}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<ClassPathResource> appShell() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(new ClassPathResource("static/index.html"));
    }

    /**
     * Lists generated transfers.
     *
     * @return transfer views
     * @throws IOException when storage cannot be read
     */
    @GetMapping("/api/transfers")
    public List<TransferView> listTransfers() throws IOException {
        return transfers.listTransfers();
    }

    /**
     * Creates a text transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    @PostMapping("/api/transfers/text")
    public TransferView createText(@RequestBody CreateTextTransferRequest request)
            throws IOException, GeneralSecurityException {
        return transfers.createText(request);
    }

    /**
     * Creates a file transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    @PostMapping("/api/transfers/file")
    public TransferView createFile(@RequestBody CreateFileTransferRequest request)
            throws IOException, GeneralSecurityException {
        return transfers.createFile(request);
    }

    /**
     * Creates an image transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    @PostMapping("/api/transfers/image")
    public TransferView createImage(@RequestBody CreateFileTransferRequest request)
            throws IOException, GeneralSecurityException {
        return transfers.createImage(request);
    }

    /**
     * Creates a video transfer.
     *
     * @param request request body
     * @return transfer view
     * @throws IOException when storage fails
     * @throws GeneralSecurityException when encryption fails
     */
    @PostMapping("/api/transfers/video")
    public TransferView createVideo(@RequestBody CreateFileTransferRequest request)
            throws IOException, GeneralSecurityException {
        return transfers.createVideo(request);
    }

    /**
     * Returns a generated transfer view.
     *
     * @param transferId transfer id
     * @return transfer view
     * @throws IOException when storage cannot be read
     */
    @GetMapping("/api/transfers/{transferId}")
    public TransferView getTransfer(@PathVariable("transferId") UUID transferId) throws IOException {
        return transfers.getTransfer(transferId);
    }

    /**
     * Inspects a generated transfer.
     *
     * @param transferId transfer id
     * @return inspection view
     * @throws IOException when storage cannot be read
     */
    @GetMapping("/api/transfers/{transferId}/inspect")
    public TransferInspectionView inspect(@PathVariable("transferId") UUID transferId) throws IOException {
        return transfers.inspect(transferId);
    }

    /**
     * Recovers a generated transfer.
     *
     * @param transferId transfer id
     * @param request request body
     * @return recovered plaintext response
     * @throws IOException when storage cannot be read
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    @PostMapping("/api/transfers/{transferId}/receive")
    public ReceiveTransferResponse receive(
            @PathVariable("transferId") UUID transferId,
            @RequestBody ReceiveTransferRequest request)
            throws IOException, GeneralSecurityException {
        return transfers.receive(transferId, request);
    }

    /**
     * Creates a live camera receiver session.
     *
     * @param request request body
     * @return scan session view
     */
    @PostMapping("/api/scans")
    public ScanSessionView createScan(@RequestBody CreateScanSessionRequest request) {
        return scans.create(request);
    }

    /**
     * Returns a live camera receiver session.
     *
     * @param sessionId session id
     * @return scan session view
     */
    @GetMapping("/api/scans/{sessionId}")
    public ScanSessionView getScan(@PathVariable("sessionId") UUID sessionId) {
        return scans.get(sessionId);
    }

    /**
     * Submits one camera frame to a receiver session.
     *
     * @param sessionId session id
     * @param request request body
     * @return scan session view
     * @throws IOException when image bytes cannot be read
     */
    @PostMapping("/api/scans/{sessionId}/frames")
    public ScanSessionView submitScanFrame(
            @PathVariable("sessionId") UUID sessionId,
            @RequestBody SubmitScanFrameRequest request)
            throws IOException {
        return scans.submit(sessionId, request);
    }

    /**
     * Recovers a completed receiver scan session.
     *
     * @param sessionId session id
     * @return recovered plaintext response
     * @throws GeneralSecurityException when authentication or decryption fails
     */
    @PostMapping("/api/scans/{sessionId}/receive")
    public ReceiveTransferResponse receiveScan(@PathVariable("sessionId") UUID sessionId)
            throws GeneralSecurityException {
        return scans.receive(sessionId);
    }

    /**
     * Serves generated display pages, frame PNGs, and manifests.
     *
     * @param transferId transfer id
     * @param fileName file name
     * @return file resource
     * @throws IOException when the file is missing or invalid
     */
    @GetMapping("/transfers/{transferId}/{fileName:.+}")
    public ResponseEntity<FileSystemResource> transferFile(
            @PathVariable("transferId") UUID transferId,
            @PathVariable("fileName") String fileName)
            throws IOException {
        TransferFile file = transfers.file(transferId, fileName);
        return ResponseEntity.ok()
                .contentType(file.mediaType())
                .body(new FileSystemResource(file.path()));
    }

    /**
     * Handles invalid requests.
     *
     * @param exception exception
     * @return error response
     */
    @ExceptionHandler({IllegalArgumentException.class, GeneralSecurityException.class})
    public ResponseEntity<ApiError> badRequest(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError(exception.getMessage(), Instant.now()));
    }

    /**
     * Handles missing transfers and files.
     *
     * @param exception exception
     * @return error response
     */
    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<ApiError> notFound(NoSuchFileException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("not found: " + exception.getFile(), Instant.now()));
    }
}

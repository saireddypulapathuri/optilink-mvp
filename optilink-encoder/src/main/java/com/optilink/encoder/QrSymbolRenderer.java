package com.optilink.encoder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalSymbol;
import com.optilink.optical.QrPayloadEnvelope;
import com.optilink.optical.RenderedOpticalFrame;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Renders optical symbols as QR-compatible PNG frames.
 */
public final class QrSymbolRenderer {
    private static final String PNG_FORMAT = "PNG";
    private static final String PNG_MIME_TYPE = "image/png";
    private static final int QR_MODULE_BASE = 17;
    private static final int QR_MODULES_PER_VERSION = 4;
    private static final int DEFAULT_PIXELS_PER_MODULE = 8;
    private static final int QUIET_ZONE_MODULES = 4;
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    private final QRCodeWriter writer;
    private final int pixelsPerModule;

    /**
     * Creates a renderer with conservative module sizing.
     */
    public QrSymbolRenderer() {
        this(new QRCodeWriter(), DEFAULT_PIXELS_PER_MODULE);
    }

    /**
     * Creates a renderer.
     *
     * @param writer QR writer
     * @param pixelsPerModule pixels used for each QR module
     */
    public QrSymbolRenderer(QRCodeWriter writer, int pixelsPerModule) {
        this.writer = Objects.requireNonNull(writer, "writer");
        if (pixelsPerModule < 1) {
            throw new IllegalArgumentException("pixelsPerModule must be positive");
        }
        this.pixelsPerModule = pixelsPerModule;
    }

    /**
     * Renders a symbol as a PNG frame.
     *
     * @param symbol optical symbol
     * @return rendered PNG frame
     */
    public RenderedOpticalFrame render(OpticalSymbol symbol) {
        Objects.requireNonNull(symbol, "symbol");
        String content = QrPayloadEnvelope.encode(symbol.frameBytes());
        int dimension = qrDimension(symbol);
        try {
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, dimension, dimension, hints(symbol));
            return new RenderedOpticalFrame(symbol.profile(), PNG_MIME_TYPE, dimension, dimension, toPng(matrix));
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("failed to render QR optical frame", exception);
        }
    }

    private int qrDimension(OpticalSymbol symbol) {
        int modules = QR_MODULE_BASE + QR_MODULES_PER_VERSION * symbol.profile().qrVersion();
        return (modules + QUIET_ZONE_MODULES * 2) * pixelsPerModule;
    }

    private Map<EncodeHintType, Object> hints(OpticalSymbol symbol) {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, zxingLevel(symbol.profile().errorCorrectionLevel()));
        hints.put(EncodeHintType.QR_VERSION, symbol.profile().qrVersion());
        hints.put(EncodeHintType.MARGIN, QUIET_ZONE_MODULES);
        return hints;
    }

    private com.google.zxing.qrcode.decoder.ErrorCorrectionLevel zxingLevel(ErrorCorrectionLevel level) {
        return switch (level) {
            case L -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L;
            case M -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M;
            case Q -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q;
            case H -> com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H;
        };
    }

    private byte[] toPng(BitMatrix matrix) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, PNG_FORMAT, output, new MatrixToImageConfig(BLACK, WHITE));
        return output.toByteArray();
    }
}

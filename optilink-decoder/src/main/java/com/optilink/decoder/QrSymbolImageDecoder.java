package com.optilink.decoder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.optilink.optical.OpticalSymbol;
import com.optilink.optical.QrPayloadEnvelope;
import com.optilink.optical.RenderedOpticalFrame;
import com.optilink.protocol.PacketIntegrityException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 * Decodes QR-compatible PNG optical frames into optical symbols.
 */
public final class QrSymbolImageDecoder {
    private final MultiFormatReader reader;

    /**
     * Creates a QR image decoder.
     */
    public QrSymbolImageDecoder() {
        this(new MultiFormatReader());
    }

    /**
     * Creates a QR image decoder.
     *
     * @param reader ZXing reader
     */
    public QrSymbolImageDecoder(MultiFormatReader reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    /**
     * Decodes a rendered QR optical frame.
     *
     * @param frame rendered optical frame
     * @return decoded optical symbol
     */
    public OpticalSymbol decode(RenderedOpticalFrame frame) {
        Objects.requireNonNull(frame, "frame");
        try {
            String text = decodeText(frame.imageBytes());
            return new OpticalSymbol(frame.profile(), QrPayloadEnvelope.decode(text));
        } catch (IOException | NotFoundException | IllegalArgumentException exception) {
            throw new PacketIntegrityException("failed to decode QR optical frame", exception);
        } finally {
            reader.reset();
        }
    }

    private String decodeText(byte[] imageBytes) throws IOException, NotFoundException {
        try {
            return reader.decode(binaryBitmap(imageBytes), hints(false)).getText();
        } catch (NotFoundException exception) {
            reader.reset();
            return reader.decode(binaryBitmap(imageBytes), hints(true)).getText();
        }
    }

    private BinaryBitmap binaryBitmap(byte[] imageBytes) throws IOException {
        var image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("unsupported image format");
        }
        return new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
    }

    private Map<DecodeHintType, Object> hints(boolean pureBarcode) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        if (pureBarcode) {
            hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        }
        return hints;
    }
}

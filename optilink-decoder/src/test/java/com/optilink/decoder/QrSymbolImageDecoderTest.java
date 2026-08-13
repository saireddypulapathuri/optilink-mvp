package com.optilink.decoder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.optilink.optical.ErrorCorrectionLevel;
import com.optilink.optical.OpticalProfile;
import com.optilink.optical.QrPayloadEnvelope;
import com.optilink.optical.RenderedOpticalFrame;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import org.junit.jupiter.api.Test;

class QrSymbolImageDecoderTest {

    @Test
    void decodesOptiLinkQrEnvelopeFromPng() throws Exception {
        byte[] source = {9, 8, 7};
        OpticalProfile profile = new OpticalProfile("qr", 10, 8, ErrorCorrectionLevel.M, 80, 32);
        RenderedOpticalFrame frame = frame(profile, QrPayloadEnvelope.encode(source));

        var symbol = new QrSymbolImageDecoder().decode(frame);

        assertArrayEquals(source, symbol.frameBytes());
    }

    private RenderedOpticalFrame frame(OpticalProfile profile, String content) throws Exception {
        var hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M);
        var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256, hints);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        return new RenderedOpticalFrame(profile, "image/png", 256, 256, output.toByteArray());
    }
}

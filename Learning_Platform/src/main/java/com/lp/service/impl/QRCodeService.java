package com.lp.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class QRCodeService {

    public byte[] generateQRCode(String upiLink) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(upiLink, BarcodeFormat.QR_CODE, 300, 300);
        
        // Convert BitMatrix to ByteArrayOutputStream
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", byteArrayOutputStream);
        
        // Return the byte array of the generated QR code
        return byteArrayOutputStream.toByteArray();
    }

    public void saveQRCode(String upiLink, String filePath) throws WriterException, IOException {
        byte[] qrCode = generateQRCode(upiLink);
        Path path = Paths.get(filePath);
        Files.write(path, qrCode);
        System.out.println(path);
    }
}


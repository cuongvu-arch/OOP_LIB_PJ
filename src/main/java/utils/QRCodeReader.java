package utils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class QRCodeReader {

    public static String readQRCode(String filePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        BufferedImage bufferedImage = ImageIO.read(fileInputStream);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }
}
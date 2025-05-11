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

/**
 * Lớp tiện ích để đọc và giải mã nội dung từ hình ảnh mã QR sử dụng thư viện ZXing.
 */
public class QRCodeReader {

    /**
     * Đọc và giải mã mã QR từ một tệp ảnh được chỉ định.
     *
     * @param filePath Đường dẫn đến tệp ảnh chứa mã QR (ví dụ: "qr/mycode.png").
     * @return Chuỗi văn bản được mã hóa trong mã QR.
     * @throws Exception Nếu không thể đọc ảnh hoặc giải mã QR.
     */
    public static String readQRCode(String filePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        BufferedImage bufferedImage = ImageIO.read(fileInputStream);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
    }
}
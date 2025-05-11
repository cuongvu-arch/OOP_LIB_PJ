package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Lớp tiện ích dùng để tạo và lưu mã QR sử dụng thư viện ZXing.
 */
public class QRCodeGenerator {

    /**
     * Tạo ảnh mã QR từ chuỗi văn bản đầu vào với kích thước xác định.
     *
     * @param text   Chuỗi văn bản sẽ được mã hóa thành mã QR.
     * @param width  Chiều rộng của ảnh QR.
     * @param height Chiều cao của ảnh QR.
     * @return {@link BufferedImage} chứa hình ảnh mã QR đã tạo.
     * @throws WriterException Nếu có lỗi khi mã hóa QR.
     * @throws IOException     Nếu có lỗi trong quá trình xử lý ảnh.
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Black : White
            }
        }
        return image;
    }

    /**
     * Lưu ảnh mã QR vào tệp với định dạng PNG.
     *
     * @param image    Ảnh mã QR cần lưu.
     * @param filePath Đường dẫn tới nơi lưu file (bao gồm cả tên file và đuôi .png).
     * @throws IOException Nếu có lỗi khi ghi ảnh xuống file.
     */
    public static void saveQRCode(BufferedImage image, String filePath) throws IOException {
        File qrFile = new File(filePath);
        File parentDir = qrFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(image, "png", qrFile);
    }
}
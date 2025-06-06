import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.nio.file.Paths;

public class GenerateTestQR {
    public static void main(String[] args) {
        String text = "Hello from QR Code Reader GUI! This is a test message.";
        int width = 300;
        int height = 300;
        
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            
            java.nio.file.Path path = Paths.get("test-images/qr-test-hello.png");
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            System.out.println("✅ Test QR code generated: " + path.toAbsolutePath());
            System.out.println("Content: " + text);
        } catch (Exception e) {
            System.out.println("❌ Failed to generate test QR code: " + e.getMessage());
        }
    }
}

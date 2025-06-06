import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.nio.file.Paths;

public class GenerateMoreTestQR {
    public static void main(String[] args) {
        // Create multiple test QR codes
        generateQR("https://github.com/kotlin/kotlin", "test-images/qr-github-kotlin.png");
        generateQR("WiFi:T:WPA;S:TestNetwork;P:password123;;", "test-images/qr-wifi-test.png");
        generateQR("BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nORG:Test Company\nTEL:+1234567890\nEMAIL:john@example.com\nEND:VCARD", "test-images/qr-vcard-test.png");
        generateQR("This is a longer text message for testing the QR code reader application. It contains multiple sentences and should test the application's ability to handle longer content.", "test-images/qr-long-text.png");
        generateQR("https://www.example.com/very/long/url/path/that/might/be/used/for/testing/purposes", "test-images/qr-long-url.png");
    }
    
    private static void generateQR(String text, String filename) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
            
            java.nio.file.Path path = Paths.get(filename);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            System.out.println("✅ Generated: " + filename);
            System.out.println("   Content: " + (text.length() > 50 ? text.substring(0, 50) + "..." : text));
            System.out.println();
        } catch (Exception e) {
            System.out.println("❌ Failed to generate " + filename + ": " + e.getMessage());
        }
    }
}

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates comprehensive set of QR code test images
 * Used to test different content types and scenarios
 */
public class GenerateTestSuite {
    public static void main(String[] args) {
        try {
            // Create test-images directory if it doesn't exist
            Files.createDirectories(Paths.get("test-images"));
            
            System.out.println("🔄 Generating comprehensive QR code test suite...");
            System.out.println("================================================");
            
            // 1. URL QR Codes
            generateQR("https://github.com/kotlin", "qr-github-kotlin.png", "GitHub Kotlin repository");
            generateQR("https://kotlinlang.org/docs/getting-started.html", "qr-kotlin-docs.png", "Kotlin documentation");
            generateQR("https://www.example.com/very/long/url/path/that/might/cause/encoding/issues/testing/url/length/limits", "qr-long-url.png", "Long URL test");
            
            // 2. Text QR Codes
            generateQR("Hello World! This is a simple QR code test.", "qr-hello-world.png", "Simple hello message");
            generateQR("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.", "qr-long-text.png", "Long text content");
            generateQR("🎉 Unicode test: émojis, accents, ñ, 中文, العربية", "qr-unicode.png", "Unicode and emoji test");
            
            // 3. vCard Contact
            String vcard = "BEGIN:VCARD\\n" +
                          "VERSION:3.0\\n" +
                          "FN:Jean Dupont\\n" +
                          "ORG:QR Code Reader Corp\\n" +
                          "TEL:+33-1-23-45-67-89\\n" +
                          "EMAIL:jean.dupont@example.com\\n" +
                          "URL:https://example.com\\n" +
                          "END:VCARD";
            generateQR(vcard, "qr-vcard-test.png", "vCard contact information");
            
            // 4. WiFi Configuration
            String wifi = "WIFI:T:WPA;S:TestNetwork;P:password123;H:false;";
            generateQR(wifi, "qr-wifi-test.png", "WiFi configuration");
            
            // 5. Phone Numbers and Communication
            generateQR("tel:+33123456789", "qr-phone.png", "Phone number");
            generateQR("smsto:+33123456789:Hello from QR Code!", "qr-sms.png", "SMS message");
            
            // 6. Email
            generateQR("mailto:contact@example.com?subject=QR%20Test&body=Hello%20from%20QR%20code!", "qr-email.png", "Email with subject and body");
            
            // 7. Geographic coordinates
            generateQR("geo:48.8566,2.3522", "qr-location.png", "Geographic location (Paris)");
            
            // 8. Calendar event
            String event = "BEGIN:VEVENT\\n" +
                          "SUMMARY:QR Code Reader Meeting\\n" +
                          "DTSTART:20250606T140000Z\\n" +
                          "DTEND:20250606T150000Z\\n" +
                          "DESCRIPTION:Testing calendar event QR codes\\n" +
                          "END:VEVENT";
            generateQR(event, "qr-calendar.png", "Calendar event");
            
            // 9. JSON data
            String json = "{\\\"name\\\":\\\"QR Test\\\",\\\"version\\\":\\\"2.0\\\",\\\"features\\\":[\\\"GUI\\\",\\\"batch\\\",\\\"history\\\"]}";
            generateQR(json, "qr-json.png", "JSON data");
            
            // 10. Large text (test limits)
            StringBuilder largeText = new StringBuilder();
            for (int i = 0; i < 30; i++) {
                largeText.append("Line ").append(i + 1).append(": This is a test of large content capacity. ");
            }
            generateQR(largeText.toString(), "qr-large-content.png", "Large text content");
            
            // 11. Special characters and formats
            generateQR("Special chars: @#$%^&*()_+-=[]{}|;':\",./<>?", "qr-special-chars.png", "Special characters");
            generateQR("Line 1\\nLine 2\\nLine 3\\nMultiple lines test", "qr-multiline.png", "Multi-line text");
            
            // 12. Numbers and codes
            generateQR("123456789012345678901234567890", "qr-numbers.png", "Long number sequence");
            generateQR("ABC123XYZ789", "qr-alphanumeric.png", "Alphanumeric code");
            
            System.out.println("================================================");
            System.out.println("✅ Complete QR code test suite generated!");
            System.out.println("📁 Check the test-images/ directory");
            System.out.println("🧪 Total images: 16 different QR code types");
            
        } catch (Exception e) {
            System.out.println("❌ Error generating QR codes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateQR(String content, String filename, String description) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            
            java.nio.file.Path path = Paths.get("test-images", filename);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            System.out.println("✅ " + filename + " - " + description);
            
        } catch (Exception e) {
            System.out.println("❌ Failed to generate " + filename + ": " + e.getMessage());
        }
    }
}

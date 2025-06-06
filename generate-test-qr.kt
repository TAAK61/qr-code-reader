import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.nio.file.Paths

fun main() {
    val text = "Hello from QR Code Reader GUI! This is a test message."
    val width = 300
    val height = 300
    
    try {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix: BitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
        
        val path = Paths.get("test-images/qr-test-hello.png")
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path)
        
        println("✅ Test QR code generated: ${path.toAbsolutePath()}")
        println("Content: $text")
    } catch (e: Exception) {
        println("❌ Failed to generate test QR code: ${e.message}")
    }
}

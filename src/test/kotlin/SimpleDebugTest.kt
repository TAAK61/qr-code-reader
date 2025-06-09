import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.fail
import qr.QRCodeReader
import java.io.File

class SimpleDebugTest {

    @Test
    fun debugQRCodeReading() {
        try {
            println("=== DEBUG TEST START ===")
            val reader = QRCodeReader()
            println("QRCodeReader created successfully")
            
            val testFile = File("test-images/qr-hello-world.png")
            println("Test file exists: ${testFile.exists()}")
            println("Test file path: ${testFile.absolutePath}")
            println("Test file size: ${testFile.length()} bytes")
            
            if (!testFile.exists()) {
                fail<Unit>("Test file does not exist: ${testFile.absolutePath}")
                return
            }
            
            println("About to call readQRCode...")
            val result = reader.readQRCode(testFile.absolutePath)
            println("Result: $result")
            println("=== DEBUG TEST SUCCESS ===")
        } catch (e: Exception) {
            println("=== DEBUG TEST EXCEPTION ===")
            println("Exception type: ${e.javaClass.name}")
            println("Exception message: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            fail<Unit>("Test failed with exception: ${e.message}")
        }
    }
}

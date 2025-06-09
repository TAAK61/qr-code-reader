import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import qr.QRCodeGenerator
import qr.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Test QR Code generation functionality - Task 72
 */
class QRGeneratorTest {
    
    private val qrGenerator = QRCodeGenerator()
    private val qrReader = QRCodeReader()
    
    @Test
    fun testBasicQRCodeGeneration() {
        val testContent = "Hello, QR Code!"
        val outputPath = "test-output/qr-test-basic.png"
        
        // Ensure output directory exists
        Files.createDirectories(Paths.get("test-output"))
        
        val result = qrGenerator.generateQRCode(
            content = testContent,
            outputPath = outputPath
        )
        
        assertTrue(result.success, "QR code generation should succeed")
        assertNull(result.error, "Should not have errors")
        assertEquals(outputPath, result.filePath, "File path should match")
        
        // Verify file was created
        val file = File(outputPath)
        assertTrue(file.exists(), "QR code file should exist")
        assertTrue(file.length() > 0, "QR code file should not be empty")
        
        // Test reading the generated QR code
        val readContent = qrReader.readQRCode(outputPath)
        assertEquals(testContent, readContent, "Read content should match original")
        
        // Cleanup
        file.delete()
    }
    
    @Test
    fun testQRCodeGenerationAsImage() {
        val testContent = "Test image generation"
        val config = QRCodeGenerator.QRCodeConfig(
            width = 200,
            height = 200,
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
        
        val result = qrGenerator.generateQRCodeImage(testContent, config)
        
        assertTrue(result.success, "QR code image generation should succeed")
        assertNotNull(result.image, "Should have generated image")
        assertNull(result.error, "Should not have errors")
        
        val metadata = result.metadata
        assertEquals(200, metadata["width"], "Width should match config")
        assertEquals(200, metadata["height"], "Height should match config")
        assertEquals(testContent.length, metadata["contentLength"], "Content length should match")
        assertEquals("H", metadata["errorCorrectionLevel"], "Error correction level should match")
    }
    
    @Test
    fun testQRCodeTemplates() {
        val outputDir = "test-output"
        Files.createDirectories(Paths.get(outputDir))
        
        // Test URL template
        val urlResult = QRCodeGenerator.Templates.generateURL(
            url = "https://example.com",
            outputPath = "$outputDir/qr-url.png"
        )
        assertTrue(urlResult.success, "URL QR generation should succeed")
        
        // Test WiFi template
        val wifiResult = QRCodeGenerator.Templates.generateWiFi(
            ssid = "TestNetwork",
            password = "password123",
            security = "WPA",
            hidden = false,
            outputPath = "$outputDir/qr-wifi.png"
        )
        assertTrue(wifiResult.success, "WiFi QR generation should succeed")
        
        // Test vCard template
        val vcardResult = QRCodeGenerator.Templates.generateVCard(
            name = "John Doe",
            organization = "Test Corp",
            phone = "+1234567890",
            email = "john@example.com",
            outputPath = "$outputDir/qr-vcard.png"
        )
        assertTrue(vcardResult.success, "vCard QR generation should succeed")
        
        // Cleanup
        File("$outputDir/qr-url.png").delete()
        File("$outputDir/qr-wifi.png").delete()
        File("$outputDir/qr-vcard.png").delete()
    }
    
    @Test
    fun testContentValidation() {
        // Test valid content
        val validResult = qrGenerator.validateContent("Valid content")
        assertTrue(validResult.valid, "Valid content should pass validation")
        assertTrue(validResult.errors.isEmpty(), "Should have no errors")
        
        // Test empty content
        val emptyResult = qrGenerator.validateContent("")
        assertFalse(emptyResult.valid, "Empty content should fail validation")
        assertTrue(emptyResult.errors.isNotEmpty(), "Should have errors")
        
        // Test very long content
        val longContent = "a".repeat(3000)
        val longResult = qrGenerator.validateContent(longContent)
        assertFalse(longResult.valid, "Too long content should fail validation")
        assertTrue(longResult.errors.any { it.contains("too long") }, "Should have length error")
    }
    
    @Test
    fun testBatchGeneration() {
        val contentList = listOf(
            "First QR Code",
            "Second QR Code", 
            "Third QR Code"
        )
        val outputDir = "test-output/batch"
        
        val results = qrGenerator.generateQRCodesBatch(
            contentList = contentList,
            outputDirectory = outputDir,
            fileNamePrefix = "batch-test"
        )
        
        assertEquals(3, results.size, "Should generate 3 QR codes")
        assertTrue(results.all { it.success }, "All generations should succeed")
        
        // Verify files were created
        for (i in contentList.indices) {
            val file = File("$outputDir/batch-test-${i + 1}.png")
            assertTrue(file.exists(), "Batch file ${i + 1} should exist")
            
            // Test reading content
            val readContent = qrReader.readQRCode(file.absolutePath)
            assertEquals(contentList[i], readContent, "Read content should match for file ${i + 1}")
            
            file.delete()
        }
        
        // Cleanup directory
        File(outputDir).delete()
    }
}

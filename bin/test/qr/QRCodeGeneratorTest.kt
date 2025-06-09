package qr

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Comprehensive unit tests for QRCodeGenerator
 * 
 * Task 72: Test QR code generation functionality
 */
class QRCodeGeneratorTest {
    
    private lateinit var generator: QRCodeGenerator
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeEach
    fun setUp() {
        generator = QRCodeGenerator()
    }
    
    @Test
    fun `test basic QR code generation to file`() {
        val content = "Hello, QR Code!"
        val outputPath = tempDir.resolve("test-qr.png").toString()
        
        val result = generator.generateQRCode(content, outputPath)
        
        assertTrue(result.success, "QR code generation should succeed")
        assertNull(result.error, "No error should be present")
        assertEquals(outputPath, result.filePath, "File path should match")
        assertTrue(File(outputPath).exists(), "Generated file should exist")
        assertTrue(File(outputPath).length() > 0, "Generated file should not be empty")
    }
    
    @Test
    fun `test QR code generation with custom configuration`() {
        val content = "Custom QR Code"
        val outputPath = tempDir.resolve("custom-qr.png").toString()
        val config = QRCodeGenerator.QRCodeConfig(
            width = 500,
            height = 500,
            errorCorrectionLevel = ErrorCorrectionLevel.H,
            margin = 2
        )
        
        val result = generator.generateQRCode(content, outputPath, config)
        
        assertTrue(result.success, "QR code generation should succeed")
        assertEquals(500, result.metadata["width"])
        assertEquals(500, result.metadata["height"])
        assertEquals("H", result.metadata["errorCorrectionLevel"])
    }
    
    @Test
    fun `test QR code generation as BufferedImage`() {
        val content = "Image QR Code"
        
        val result = generator.generateQRCodeImage(content)
        
        assertTrue(result.success, "QR code generation should succeed")
        assertNotNull(result.image, "BufferedImage should be generated")
        assertEquals(300, result.image!!.width, "Default width should be 300")
        assertEquals(300, result.image!!.height, "Default height should be 300")
    }
    
    @Test
    fun `test batch QR code generation`() {
        val contentList = listOf(
            "QR Code 1",
            "QR Code 2", 
            "QR Code 3"
        )
        val outputDir = tempDir.toString()
        
        val results = generator.generateQRCodesBatch(contentList, outputDir, "test-batch")
        
        assertEquals(3, results.size, "Should generate 3 QR codes")
        assertTrue(results.all { it.success }, "All generations should succeed")
        
        // Check files exist
        assertTrue(File(tempDir.resolve("test-batch-1.png").toString()).exists())
        assertTrue(File(tempDir.resolve("test-batch-2.png").toString()).exists())
        assertTrue(File(tempDir.resolve("test-batch-3.png").toString()).exists())
    }
    
    @Test
    fun `test QR code generation with empty content fails`() {
        val outputPath = tempDir.resolve("empty-qr.png").toString()
        
        val result = generator.generateQRCode("", outputPath)
        
        assertFalse(result.success, "Empty content should fail")
        assertNotNull(result.error, "Error message should be present")
        assertFalse(File(outputPath).exists(), "File should not be created on failure")
    }
    
    @Test
    fun `test QR code generation with very long content`() {
        val longContent = "A".repeat(3000) // Exceeds typical QR code limits
        val outputPath = tempDir.resolve("long-qr.png").toString()
        
        val result = generator.generateQRCode(longContent, outputPath)
        
        // This should either succeed or fail gracefully
        if (!result.success) {
            assertNotNull(result.error, "Error message should be present for overly long content")
        }
    }
    
    @Test
    fun `test URL template generation`() {
        val url = "https://www.example.com"
        val outputPath = tempDir.resolve("url-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateURL(url, outputPath)
        
        assertTrue(result.success, "URL QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test WiFi template generation`() {
        val outputPath = tempDir.resolve("wifi-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateWiFi(
            ssid = "TestNetwork",
            password = "password123",
            security = "WPA",
            hidden = false,
            outputPath = outputPath
        )
        
        assertTrue(result.success, "WiFi QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test vCard template generation`() {
        val outputPath = tempDir.resolve("vcard-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateVCard(
            name = "John Doe",
            organization = "Test Corp",
            phone = "+1234567890",
            email = "john@example.com",
            outputPath = outputPath
        )
        
        assertTrue(result.success, "vCard QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test SMS template generation`() {
        val outputPath = tempDir.resolve("sms-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateSMS(
            phoneNumber = "+1234567890",
            message = "Hello from QR code!",
            outputPath = outputPath
        )
        
        assertTrue(result.success, "SMS QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test email template generation`() {
        val outputPath = tempDir.resolve("email-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateEmail(
            email = "test@example.com",
            subject = "Test Subject",
            body = "Test message body",
            outputPath = outputPath
        )
        
        assertTrue(result.success, "Email QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test location template generation`() {
        val outputPath = tempDir.resolve("location-qr.png").toString()
        
        val result = QRCodeGenerator.Templates.generateLocation(
            latitude = 48.8566,
            longitude = 2.3522,
            outputPath = outputPath
        )
        
        assertTrue(result.success, "Location QR code generation should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test content validation - valid content`() {
        val content = "Valid QR code content"
        
        val validation = generator.validateContent(content)
        
        assertTrue(validation.valid, "Valid content should pass validation")
        assertTrue(validation.errors.isEmpty(), "No errors should be present")
    }
    
    @Test
    fun `test content validation - empty content`() {
        val validation = generator.validateContent("")
        
        assertFalse(validation.valid, "Empty content should fail validation")
        assertTrue(validation.errors.isNotEmpty(), "Errors should be present")
        assertTrue(validation.errors.any { it.contains("empty") }, "Should contain empty content error")
    }
    
    @Test
    fun `test content validation - very long content`() {
        val longContent = "A".repeat(3000)
        
        val validation = generator.validateContent(longContent)
        
        assertFalse(validation.valid, "Very long content should fail validation")
        assertTrue(validation.errors.any { it.contains("too long") }, "Should contain length error")
    }
    
    @Test
    fun `test content validation - content with warnings`() {
        val contentWithNewlines = "Line 1\nLine 2\nLine 3"
        
        val validation = generator.validateContent(contentWithNewlines)
        
        assertTrue(validation.valid, "Content with newlines should still be valid")
        assertTrue(validation.warnings.isNotEmpty(), "Warnings should be present")
        assertTrue(validation.warnings.any { it.contains("line breaks") }, "Should warn about line breaks")
    }
    
    @Test
    fun `test metadata in generation result`() {
        val content = "Metadata test"
        val outputPath = tempDir.resolve("metadata-qr.png").toString()
        val config = QRCodeGenerator.QRCodeConfig(width = 400, height = 400)
        
        val result = generator.generateQRCode(content, outputPath, config)
        
        assertTrue(result.success, "Generation should succeed")
        assertEquals(400, result.metadata["width"])
        assertEquals(400, result.metadata["height"])
        assertEquals(content.length, result.metadata["contentLength"])
        assertTrue((result.metadata["fileSizeBytes"] as Long) > 0, "File size should be positive")
        assertEquals("PNG", result.metadata["format"])
    }
    
    @Test
    fun `test QR code generation with special characters`() {
        val content = "Special chars: @#$%^&*()_+-=[]{}|;':\",./<>?"
        val outputPath = tempDir.resolve("special-chars-qr.png").toString()
        
        val result = generator.generateQRCode(content, outputPath)
        
        assertTrue(result.success, "QR code with special characters should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test QR code generation with Unicode characters`() {
        val content = "Unicode test: émojis 🎉, accents ñ, 中文, العربية"
        val outputPath = tempDir.resolve("unicode-qr.png").toString()
        
        val result = generator.generateQRCode(content, outputPath)
        
        assertTrue(result.success, "QR code with Unicode characters should succeed")
        assertTrue(File(outputPath).exists(), "File should be created")
    }
    
    @Test
    fun `test different error correction levels`() {
        val content = "Error correction test"
        val levels = listOf(
            ErrorCorrectionLevel.L,
            ErrorCorrectionLevel.M,
            ErrorCorrectionLevel.Q,
            ErrorCorrectionLevel.H
        )
        
        levels.forEach { level ->
            val outputPath = tempDir.resolve("error-correction-${level.name}-qr.png").toString()
            val config = QRCodeGenerator.QRCodeConfig(errorCorrectionLevel = level)
            
            val result = generator.generateQRCode(content, outputPath, config)
            
            assertTrue(result.success, "QR code with error correction level $level should succeed")
            assertTrue(File(outputPath).exists(), "File should be created for level $level")
            assertEquals(level.toString(), result.metadata["errorCorrectionLevel"])
        }
    }
    
    @Test
    fun `test generated QR code is valid image`() {
        val content = "Image validation test"
        val outputPath = tempDir.resolve("image-validation-qr.png").toString()
        
        val result = generator.generateQRCode(content, outputPath)
        
        assertTrue(result.success, "QR code generation should succeed")
        
        // Try to read the generated image
        val bufferedImage = ImageIO.read(File(outputPath))
        assertNotNull(bufferedImage, "Generated file should be a valid image")
        assertEquals(300, bufferedImage.width, "Image width should match default config")
        assertEquals(300, bufferedImage.height, "Image height should match default config")
    }
}

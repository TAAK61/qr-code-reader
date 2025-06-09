package qr

import org.junit.Test
import org.junit.Assert.*
import utils.ImageProcessor
import java.awt.Color
import java.awt.image.BufferedImage

class QRCodeReaderTest {
    
    @Test
    fun testQRCodeReaderExists() {
        val reader = QRCodeReader()
        assertNotNull(reader)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testFileNotFound() {
        val reader = QRCodeReader()
        reader.readQRCode("nonexistent.png")
    }
    
    @Test
    fun testImageProcessingIntegration() {
        val reader = QRCodeReader()
        
        // Create a test image
        val testImage = createTestImage(100, 100)
        
        // Test that processing details method works without throwing exceptions
        val result = reader.readQRCodeWithProcessingDetails(testImage)
        
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain success key", result.containsKey("success"))
        
        // Even if no QR code is found, processing details should be available
        if (result["success"] == false) {
            assertTrue("Error message should be present", result.containsKey("error"))
        }
    }
    
    @Test
    fun testImageProcessingConfiguration() {
        val reader = QRCodeReader()
        
        // Test configuring image processing options
        val customOptions = ImageProcessor.ImageProcessingOptions(
            enhanceContrast = true,
            contrastFactor = 2.0,
            reduceNoise = false,
            resizeIfLarge = true,
            maxDimension = 500
        )
        
        reader.configureImageProcessing(customOptions)
        
        // Test that the configuration doesn't cause errors
        val testImage = createTestImage(200, 200)
        val result = reader.readQRCodeWithProcessingDetails(testImage)
        
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain success key", result.containsKey("success"))
    }
    
    @Test
    fun testBatchProcessing() {
        val reader = QRCodeReader()
        
        // Test batch processing with non-existent files
        val imagePaths = listOf("nonexistent1.png", "nonexistent2.png")
        val results = reader.readQRCodesBatch(imagePaths)
        
        assertNotNull("Results should not be null", results)
        assertEquals("Should have 2 results", 2, results.size)
        
        // All results should indicate failure
        results.forEach { result ->
            assertFalse("Each result should indicate failure", result["success"] as Boolean)
            assertTrue("Each result should contain error message", result.containsKey("error"))
        }
    }
    
    @Test
    fun testProcessingDetailsStructure() {
        val reader = QRCodeReader()
        val testImage = createTestImage(150, 150)
        
        val result = reader.readQRCodeWithProcessingDetails(testImage)
        
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain success key", result.containsKey("success"))
        
        // Even if QR code reading fails, processing details should be present
        // (unless the image processing itself fails)
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            assertTrue("Processing details should contain original size", 
                processingDetails.containsKey("originalSize"))
            assertTrue("Processing details should contain processed size", 
                processingDetails.containsKey("processedSize"))
            assertTrue("Processing details should contain processing time", 
                processingDetails.containsKey("imageProcessingTimeMs"))
        }
    }
    
    private fun createTestImage(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        
        // Fill with white background
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, width, height)
        
        // Add some pattern (not a QR code, just for testing image processing)
        graphics.color = Color.BLACK
        for (i in 0 until width step 10) {
            graphics.drawLine(i, 0, i, height)
        }
        for (i in 0 until height step 10) {
            graphics.drawLine(0, i, width, i)
        }
        
        graphics.dispose()
        return image
    }
}

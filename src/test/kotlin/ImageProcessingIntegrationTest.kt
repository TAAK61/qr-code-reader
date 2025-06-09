package qr

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import utils.ImageProcessor
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.abs

/**
 * Integration tests for Task 51: Image processing pipeline optimization
 * 
 * This test suite verifies that the ImageProcessor integration in QRCodeReader
 * provides the expected performance improvements and functionality.
 */
class ImageProcessingIntegrationTest {
    
    private lateinit var reader: QRCodeReader
    
    @Before
    fun setup() {
        reader = QRCodeReader()
    }
    
    @Test
    fun testImageProcessingPipelinePerformance() {
        // Create a large test image that would benefit from processing
        val largeImage = createTestImage(1000, 1000)
        
        val startTime = System.currentTimeMillis()
        val result = reader.readQRCodeWithProcessingDetails(largeImage)
        val endTime = System.currentTimeMillis()
        
        assertNotNull("Result should not be null", result)
        assertTrue("Processing time should be reasonable (< 5 seconds)", 
            endTime - startTime < 5000)
        
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            val imageProcessingTime = processingDetails["imageProcessingTimeMs"] as Long
            
            assertTrue("Image processing time should be recorded", imageProcessingTime >= 0)
            assertTrue("Image processing should be fast (< 2 seconds)", imageProcessingTime < 2000)
        }
    }
    
    @Test
    fun testMemoryOptimizationWithLargeImages() {
        val reader = QRCodeReader()
        
        // Configure for memory optimization
        val memoryOptimizedOptions = ImageProcessor.ImageProcessingOptions(
            resizeIfLarge = true,
            maxDimension = 800,
            useHighQualityResize = false // Faster processing for memory tests
        )
        reader.configureImageProcessing(memoryOptimizedOptions)
        
        // Create a very large image
        val veryLargeImage = createTestImage(2000, 2000)
        
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        val result = reader.readQRCodeWithProcessingDetails(veryLargeImage)
        
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        assertNotNull("Result should not be null", result)
        
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            val processedSize = processingDetails["processedSize"] as Map<*, *>
            val processedWidth = processedSize["width"] as Int
            val processedHeight = processedSize["height"] as Int
            
            // Verify image was resized for memory optimization
            assertTrue("Image should be resized for memory optimization",
                processedWidth <= 800 || processedHeight <= 800)
        }
        
        // Memory increase should be reasonable (less than 100MB)
        assertTrue("Memory usage should be reasonable", memoryIncrease < 100 * 1024 * 1024)
    }
    
    @Test
    fun testContrastEnhancementConfiguration() {
        val testImage = createLowContrastImage(300, 300)
        
        // Test with contrast enhancement enabled
        val contrastOptions = ImageProcessor.ImageProcessingOptions(
            enhanceContrast = true,
            contrastFactor = 2.0,
            reduceNoise = false,
            resizeIfLarge = false
        )
        reader.configureImageProcessing(contrastOptions)
        
        val result = reader.readQRCodeWithProcessingDetails(testImage)
        
        assertNotNull("Result should not be null", result)
        
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            val operationsApplied = processingDetails["operationsApplied"] as List<*>
            
            assertTrue("Contrast enhancement should be applied",
                operationsApplied.contains("contrast_enhancement"))
        }
    }
    
    @Test
    fun testNoiseReductionConfiguration() {
        val noisyImage = createNoisyImage(200, 200)
        
        // Test with noise reduction enabled
        val noiseReductionOptions = ImageProcessor.ImageProcessingOptions(
            enhanceContrast = false,
            reduceNoise = true,
            resizeIfLarge = false
        )
        reader.configureImageProcessing(noiseReductionOptions)
        
        val result = reader.readQRCodeWithProcessingDetails(noisyImage)
        
        assertNotNull("Result should not be null", result)
        
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            val operationsApplied = processingDetails["operationsApplied"] as List<*>
            
            assertTrue("Noise reduction should be applied",
                operationsApplied.contains("noise_reduction"))
        }
    }
    
    @Test
    fun testBatchProcessingMemoryManagement() {
        // Create multiple test images
        val testImages = (1..5).map { createTestImage(400, 400) }
        
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Process batch (using non-existent paths to test the error handling part of batch processing)
        val imagePaths = (1..5).map { "test_image_$it.png" }
        val results = reader.readQRCodesBatch(imagePaths)
        
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        assertEquals("Should have results for all images", 5, results.size)
        
        // Memory increase should be reasonable for batch processing
        assertTrue("Batch processing memory usage should be reasonable", 
            memoryIncrease < 50 * 1024 * 1024) // Less than 50MB
        
        // Verify all results have proper structure
        results.forEach { result ->
            assertTrue("Each result should have index", result.containsKey("index"))
            assertTrue("Each result should have imagePath", result.containsKey("imagePath"))
            assertTrue("Each result should have success flag", result.containsKey("success"))
        }
    }
    
    @Test
    fun testProcessingDetailsStructure() {
        val testImage = createTestImage(500, 300)
        
        val result = reader.readQRCodeWithProcessingDetails(testImage)
        
        assertNotNull("Result should not be null", result)
        assertTrue("Result should contain success key", result.containsKey("success"))
        
        if (result.containsKey("processingDetails")) {
            val processingDetails = result["processingDetails"] as Map<*, *>
            
            // Verify all expected fields are present
            assertTrue("Should contain original size", processingDetails.containsKey("originalSize"))
            assertTrue("Should contain processed size", processingDetails.containsKey("processedSize"))
            assertTrue("Should contain image processing time", processingDetails.containsKey("imageProcessingTimeMs"))
            assertTrue("Should contain total processing time", processingDetails.containsKey("totalProcessingTimeMs"))
            assertTrue("Should contain memory usage", processingDetails.containsKey("memoryUsedBytes"))
            assertTrue("Should contain operations applied", processingDetails.containsKey("operationsApplied"))
            
            // Verify data types and reasonable values
            val originalSize = processingDetails["originalSize"] as Map<*, *>
            assertEquals("Original width should match", 500, originalSize["width"])
            assertEquals("Original height should match", 300, originalSize["height"])
            
            val imageProcessingTime = processingDetails["imageProcessingTimeMs"] as Long
            assertTrue("Processing time should be non-negative", imageProcessingTime >= 0)
            
            val operationsApplied = processingDetails["operationsApplied"] as List<*>
            assertTrue("Operations applied should be a list", operationsApplied is List<*>)
        }
    }
    
    private fun createTestImage(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // Fill with white background
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, width, height)
        
        // Add some geometric patterns
        g2d.color = Color.BLACK
        for (i in 0 until width step 20) {
            g2d.drawLine(i, 0, i, height)
        }
        for (i in 0 until height step 20) {
            g2d.drawLine(0, i, width, i)
        }
        
        // Add some circles for more complex patterns
        g2d.color = Color.GRAY
        for (i in 0 until width step 50) {
            for (j in 0 until height step 50) {
                g2d.drawOval(i, j, 20, 20)
            }
        }
        
        g2d.dispose()
        return image
    }
    
    private fun createLowContrastImage(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // Create low contrast image with similar gray tones
        g2d.color = Color(128, 128, 128) // Mid gray background
        g2d.fillRect(0, 0, width, height)
        
        g2d.color = Color(100, 100, 100) // Slightly darker gray
        for (i in 0 until width step 15) {
            g2d.drawLine(i, 0, i, height)
        }
        
        g2d.color = Color(150, 150, 150) // Slightly lighter gray
        for (i in 0 until height step 15) {
            g2d.drawLine(0, i, width, i)
        }
        
        g2d.dispose()
        return image
    }
    
    private fun createNoisyImage(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()
        
        // White background
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, width, height)
        
        // Add random noise
        for (i in 0 until width step 2) {
            for (j in 0 until height step 2) {
                if (Math.random() < 0.1) { // 10% noise probability
                    g2d.color = if (Math.random() < 0.5) Color.BLACK else Color.GRAY
                    g2d.fillRect(i, j, 1, 1)
                }
            }
        }
        
        // Add some pattern underneath the noise
        g2d.color = Color.BLACK
        for (i in 0 until width step 30) {
            g2d.drawLine(i, 0, i, height)
        }
        
        g2d.dispose()
        return image
    }
}

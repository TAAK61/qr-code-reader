package qr

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * Example test class demonstrating the use of test fixtures
 * This shows how the TestFixtures class simplifies test setup and reduces code duplication
 */
@DisplayName("QR Code Reader - Using Test Fixtures")
class QRCodeReaderFixturesExampleTest : BaseQRCodeTest() {
    
    @Test
    @DisplayName("Should read QR code using fixture helper methods")
    fun shouldReadQRCodeUsingFixtures() {
        // Using fixture to get test image path with automatic skip if not available
        val imagePath = getTestImageOrSkip(TestFixtures.TestImages.HELLO_WORLD)
        
        // Using fixture validation helper
        val content = validateQRCodeReading(
            imagePath = imagePath,
            expectedContent = TestFixtures.SampleContent.SIMPLE_TEXT,
            contentType = "TEXT",
            maxDurationMs = 2000
        )
        
        println("✅ Successfully read QR code using fixtures: ${content.take(50)}...")
    }
    
    @Test
    @DisplayName("Should handle multiple test images using fixtures")
    fun shouldHandleMultipleImagesUsingFixtures() {
        val testImages = listOf(
            TestFixtures.TestImages.HELLO_WORLD to "TEXT",
            TestFixtures.TestImages.GITHUB_KOTLIN to "URL",
            TestFixtures.TestImages.EMAIL to "EMAIL",
            TestFixtures.TestImages.WIFI_TEST to "WIFI"
        )
        
        testImages.forEach { (imageName, contentType) ->
            if (TestFixtures.TestImages.exists(imageName)) {
                val imagePath = TestFixtures.TestImages.getPath(imageName)
                val content = validateQRCodeReading(
                    imagePath = imagePath,
                    contentType = contentType,
                    maxDurationMs = 3000
                )
                
                println("✅ Successfully processed $contentType QR code from $imageName")
            } else {
                println("⚠️ Skipping $imageName - image not available")
            }
        }
    }
    
    @Test
    @DisplayName("Should measure performance using fixtures")
    fun shouldMeasurePerformanceUsingFixtures() {
        val imagePath = getTestImageOrSkip(TestFixtures.TestImages.LARGE_CONTENT)
        
        // Using fixture performance measurement
        val (content, duration) = TestFixtures.measureExecutionTime {
            qrCodeReader.readQRCode(imagePath)
        }
        
        assertNotNull(content)
        assertTrue(content.isNotEmpty())
        assertTrue(duration < 5000, "Should complete within 5 seconds, took ${duration}ms")
        
        println("✅ Performance test completed in ${duration}ms")
    }
    
    @Test
    @DisplayName("Should create and test temporary images using fixtures")
    fun shouldCreateTemporaryImagesUsingFixtures() {
        // Create a test image using fixtures
        val testImage = TestFixtures.createTestImage(200, 200)
        val tempImageFile = TestFixtures.saveImageToTempFile(testImage, tempDir, "temp-test.png")
        
        assertTrue(tempImageFile.exists())
        assertEquals("temp-test.png", tempImageFile.name)
        assertTrue(tempImageFile.length() > 0)
        
        println("✅ Created temporary test image: ${tempImageFile.absolutePath}")
    }
    
    @Test
    @DisplayName("Should use retry mechanism for potentially flaky operations")
    fun shouldUseRetryMechanismForFlakyOperations() {
        var attemptCount = 0
        
        // Simulate a flaky operation that succeeds on the second attempt
        val result = TestFixtures.withRetry(maxAttempts = 3, delayMs = 50) {
            attemptCount++
            if (attemptCount < 2) {
                throw RuntimeException("Simulated failure on attempt $attemptCount")
            }
            "Success on attempt $attemptCount"
        }
        
        assertEquals("Success on attempt 2", result)
        assertEquals(2, attemptCount)
        
        println("✅ Retry mechanism worked: $result")
    }
    
    @Test
    @DisplayName("Should validate different content types using fixtures")
    fun shouldValidateDifferentContentTypesUsingFixtures() {
        val testCases = listOf(
            Triple("https://github.com/kotlin", "https://github.com/kotlin", "URL"),
            Triple("mailto:test@example.com", "test@example.com", "EMAIL"),
            Triple("tel:+33123456789", "+33123456789", "PHONE"),
            Triple("WIFI:T:WPA;S:Test;P:pass;", "Test", "WIFI"),
            Triple("""{"key":"value"}""", """{"key":"value"}""", "JSON"),
            Triple("Line 1\nLine 2", "Line 1", "MULTILINE")
        )
        
        testCases.forEach { (actual, expected, contentType) ->
            val isValid = TestFixtures.validateQRContent(actual, expected, contentType)
            assertTrue(isValid, "Content validation failed for $contentType: $actual")
            println("✅ Validated $contentType content: ${actual.take(30)}...")
        }
    }
    
    @Test
    @DisplayName("Should list available test images using fixtures")
    fun shouldListAvailableTestImagesUsingFixtures() {
        val availableImages = TestFixtures.TestImages.listAvailable()
        
        println("📁 Available test images:")
        if (availableImages.isNotEmpty()) {
            availableImages.forEach { imageName ->
                println("  - $imageName")
            }
            assertTrue(availableImages.isNotEmpty())
        } else {
            println("  No test images found in test-images directory")
        }
        
        // Test specific image existence
        val testImageExists = TestFixtures.TestImages.exists(TestFixtures.TestImages.HELLO_WORLD)
        println("🔍 ${TestFixtures.TestImages.HELLO_WORLD} exists: $testImageExists")
    }
}

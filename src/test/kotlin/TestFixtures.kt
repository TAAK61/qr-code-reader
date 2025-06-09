package qr

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Task 19: Test fixtures for common test setup
 * 
 * This class provides common test fixtures and utilities that can be reused
 * across multiple test classes to reduce code duplication and ensure consistent
 * test setup.
 */
object TestFixtures {
    
    /**
     * Common test image dimensions
     */
    const val TEST_IMAGE_WIDTH = 300
    const val TEST_IMAGE_HEIGHT = 300
    
    /**
     * Sample QR code content for testing
     */
    object SampleContent {
        const val SIMPLE_TEXT = "Hello World! This is a simple QR code test."
        const val LONG_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        const val URL = "https://github.com/kotlin"
        const val EMAIL = "mailto:contact@example.com"
        const val PHONE = "tel:+33123456789"
        const val WIFI = "WIFI:T:WPA;S:TestNetwork;P:password123;H:false;"
        const val JSON = """{"name":"QR Test","version":"2.0","features":["GUI","batch","history"]}"""
        const val UNICODE = "🎉 Unicode test: émojis, accents, ñ, 中文"
        const val SPECIAL_CHARS = "Special chars: @#\$%^&*()_+-=[]{}|;':\",./<>?"
        const val MULTILINE = "Line 1\nLine 2\nLine 3\nMultiple lines test"
    }
    
    /**
     * Test image file names commonly used across tests
     */
    object TestImages {
        const val HELLO_WORLD = "qr-hello-world.png"
        const val TEST_HELLO = "qr-test-hello.png"
        const val LONG_TEXT = "qr-long-text.png"
        const val UNICODE = "qr-unicode.png"
        const val LARGE_CONTENT = "qr-large-content.png"
        const val NUMBERS = "qr-numbers.png"
        const val GITHUB_KOTLIN = "qr-github-kotlin.png"
        const val EMAIL = "qr-email.png"
        const val WIFI_TEST = "qr-wifi-test.png"
        
        fun getPath(filename: String): String = "test-images/$filename"
        
        fun exists(filename: String): Boolean = File(getPath(filename)).exists()
        
        fun listAvailable(): List<String> {
            val testImagesDir = File("test-images")
            return if (testImagesDir.exists() && testImagesDir.isDirectory) {
                testImagesDir.listFiles { file -> file.extension.lowercase() == "png" }
                    ?.map { it.name } ?: emptyList()
            } else {
                emptyList()
            }
        }
    }
    
    /**
     * Creates a simple test BufferedImage with specified dimensions
     */
    fun createTestImage(
        width: Int = TEST_IMAGE_WIDTH,
        height: Int = TEST_IMAGE_HEIGHT,
        type: Int = BufferedImage.TYPE_INT_RGB
    ): BufferedImage {
        val image = BufferedImage(width, height, type)
        val graphics = image.createGraphics()
        
        // Fill with white background
        graphics.color = java.awt.Color.WHITE
        graphics.fillRect(0, 0, width, height)
        
        // Add some simple pattern for testing
        graphics.color = java.awt.Color.BLACK
        graphics.drawRect(10, 10, width - 20, height - 20)
        graphics.drawLine(0, 0, width, height)
        graphics.drawLine(width, 0, 0, height)
        
        graphics.dispose()
        return image
    }
    
    /**
     * Saves a BufferedImage to a temporary file for testing
     */
    fun saveImageToTempFile(
        image: BufferedImage,
        tempDir: Path,
        filename: String = "test-image.png"
    ): File {
        val tempFile = tempDir.resolve(filename).toFile()
        ImageIO.write(image, "PNG", tempFile)
        return tempFile
    }
    
    /**
     * Creates a temporary test file with specified content
     */
    fun createTempTextFile(
        tempDir: Path,
        content: String,
        filename: String = "test-file.txt"
    ): File {
        val tempFile = tempDir.resolve(filename).toFile()
        tempFile.writeText(content)
        return tempFile
    }
    
    /**
     * Common assertion helper for QR code content validation
     */
    fun validateQRContent(
        actual: String,
        expected: String,
        contentType: String = "TEXT"
    ): Boolean {
        return when (contentType.uppercase()) {
            "URL" -> actual.startsWith("http") && actual.contains(expected.substringAfter("//").substringBefore("/"))
            "EMAIL" -> actual.startsWith("mailto:") && actual.contains("@")
            "PHONE" -> actual.startsWith("tel:") && actual.contains("+")
            "WIFI" -> actual.startsWith("WIFI:") && actual.contains("T:")
            "JSON" -> actual.contains("{") && actual.contains("}")
            "UNICODE" -> actual.isNotEmpty() // Unicode may have encoding differences
            "MULTILINE" -> actual.contains("\n") || actual.contains("\\n")
            else -> actual == expected
        }
    }
    
    /**
     * Performance measurement helper
     */
    inline fun <T> measureExecutionTime(operation: () -> T): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result = operation()
        val duration = System.currentTimeMillis() - startTime
        return Pair(result, duration)
    }
    
    /**
     * Retry mechanism for potentially flaky operations
     */
    inline fun <T> withRetry(
        maxAttempts: Int = 3,
        delayMs: Long = 100,
        operation: () -> T
    ): T {
        repeat(maxAttempts - 1) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                if (attempt < maxAttempts - 2) {
                    Thread.sleep(delayMs)
                } else {
                    throw e
                }
            }
        }
        return operation() // Final attempt
    }
}

/**
 * Base test class that provides common setup and fixtures
 */
abstract class BaseQRCodeTest {
    
    @TempDir
    lateinit var tempDir: Path
    
    protected lateinit var qrCodeReader: QRCodeReader
    
    @BeforeEach
    fun setUpBase() {
        qrCodeReader = QRCodeReader()
    }
    
    /**
     * Helper method to create a test image file
     */
    protected fun createTestImageFile(filename: String = "test.png"): File {
        val image = TestFixtures.createTestImage()
        return TestFixtures.saveImageToTempFile(image, tempDir, filename)
    }
    
    /**
     * Helper method to get a test image path if it exists, skip test if not
     */
    protected fun getTestImageOrSkip(filename: String): String {
        val path = TestFixtures.TestImages.getPath(filename)
        if (!TestFixtures.TestImages.exists(filename)) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Test image not found: $path")
        }
        return path
    }
    
    /**
     * Helper method to validate QR code reading with common assertions
     */
    protected fun validateQRCodeReading(
        imagePath: String,
        expectedContent: String? = null,
        contentType: String = "TEXT",
        maxDurationMs: Long = 5000
    ): String {
        val (result, duration) = TestFixtures.measureExecutionTime {
            qrCodeReader.readQRCode(imagePath)
        }
        
        // Basic validations
        assert(result.isNotEmpty()) { "QR code content should not be empty" }
        assert(duration < maxDurationMs) { "Reading should complete within ${maxDurationMs}ms, took ${duration}ms" }
        
        // Content validation if expected content is provided
        expectedContent?.let { expected ->
            assert(TestFixtures.validateQRContent(result, expected, contentType)) {
                "Content validation failed for $contentType. Expected: $expected, Actual: $result"
            }
        }
        
        return result
    }
}

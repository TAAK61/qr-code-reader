package qr

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.util.stream.Stream

/**
 * Task 16: Parameterized tests for different image formats and QR code types
 * 
 * This test class implements comprehensive parameterized testing for:
 * - Different QR code content types (URLs, text, vCard, WiFi, etc.)
 * - Various image formats and sizes
 * - Edge cases and error scenarios
 * - Performance characteristics across different content types
 */
@DisplayName("QR Code Reader - Parameterized Tests")
class QRCodeParameterizedTest {
    
    private lateinit var qrCodeReader: QRCodeReader
    
    @BeforeEach
    fun setUp() {
        qrCodeReader = QRCodeReader()
    }
    
    /**
     * Test data provider for different QR code types and their expected content
     */
    companion object {
        @JvmStatic
        fun qrCodeTestData(): Stream<Arguments> {
            return Stream.of(
                // Simple text QR codes
                Arguments.of("qr-hello-world.png", "Hello World! This is a simple QR code test.", "TEXT"),
                Arguments.of("qr-test-hello.png", "Hello from QR Code Reader GUI! This is a test message.", "TEXT"),
                Arguments.of("qr-numbers.png", "123456789012345678901234567890", "NUMERIC"),
                Arguments.of("qr-alphanumeric.png", "ABC123XYZ789", "ALPHANUMERIC"),
                
                // URL QR codes
                Arguments.of("qr-github-kotlin.png", "https://github.com/kotlin", "URL"),
                Arguments.of("qr-kotlin-docs.png", "https://kotlinlang.org/docs/getting-started.html", "URL"),
                Arguments.of("qr-long-url.png", "https://www.example.com/very/long/url/path/that/might/cause/encoding/issues/testing/url/length/limits", "URL"),
                
                // Contact information
                Arguments.of("qr-phone.png", "tel:+33123456789", "CONTACT"),
                Arguments.of("qr-email.png", "mailto:contact@example.com?subject=QR%20Test&body=Hello%20from%20QR%20code!", "CONTACT"),
                Arguments.of("qr-sms.png", "smsto:+33123456789:Hello from QR Code!", "CONTACT"),
                
                // WiFi configuration
                Arguments.of("qr-wifi-test.png", "WIFI:T:WPA;S:TestNetwork;P:password123;H:false;", "WIFI"),
                
                // Geographic location
                Arguments.of("qr-location.png", "geo:48.8566,2.3522", "LOCATION"),
                
                // Structured data
                Arguments.of("qr-json.png", "{\"name\":\"QR Test\",\"version\":\"2.0\",\"features\":[\"GUI\",\"batch\",\"history\"]}", "JSON"),
                
                // Unicode and special characters
                Arguments.of("qr-unicode.png", "🎉 Unicode test: émojis, accents, ñ, 中文, العربية", "UNICODE"),
                Arguments.of("qr-special-chars.png", "Special chars: @#\$%^&*()_+-=[]{}|;':\",./<>?", "SPECIAL"),
                Arguments.of("qr-multiline.png", "Line 1\nLine 2\nLine 3\nMultiple lines test", "MULTILINE"),
                
                // Large content
                Arguments.of("qr-long-text.png", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.", "LARGE_TEXT")
            )
        }
        
        @JvmStatic
        fun imageFormatTestData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("qr-hello-world.png", "PNG"),
                Arguments.of("qr-test-hello.png", "PNG")
            )
        }
        
        @JvmStatic
        fun errorScenarios(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("nonexistent.png", IllegalArgumentException::class.java),
                Arguments.of("empty-file.png", IllegalArgumentException::class.java)
            )
        }
        
        @JvmStatic
        fun contentTypeCategories(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("URL", listOf("qr-github-kotlin.png", "qr-kotlin-docs.png", "qr-long-url.png")),
                Arguments.of("TEXT", listOf("qr-hello-world.png", "qr-test-hello.png", "qr-long-text.png")),
                Arguments.of("CONTACT", listOf("qr-phone.png", "qr-email.png", "qr-sms.png")),
                Arguments.of("STRUCTURED", listOf("qr-wifi-test.png", "qr-vcard-test.png", "qr-json.png"))
            )
        }
    }
    
    @ParameterizedTest
    @MethodSource("qrCodeTestData")
    @DisplayName("Should read various QR code types correctly")
    fun shouldReadDifferentQRCodeTypes(filename: String, expectedContent: String, contentType: String) {
        // Given
        val testImagePath = "test-images/$filename"
        val testFile = File(testImagePath)
        
        // Skip test if image doesn't exist (test may run before image generation)
        if (!testFile.exists()) {
            println("⚠️ Test image not found: $testImagePath - skipping test")
            return
        }
        
        // When
        val result = qrCodeReader.readQRCode(testImagePath)
        
        // Then
        assertNotNull(result, "QR code content should not be null")
        assertTrue(result.isNotEmpty(), "QR code content should not be empty")
        
        // Verify content matches expected (allowing for minor encoding differences)
        when (contentType) {
            "URL" -> {
                assertTrue(result.startsWith("http"), "URL QR codes should start with http")
                assertTrue(result.contains(expectedContent.substringAfter("//").substringBefore("/")), 
                          "URL should contain expected domain")
            }
            "TEXT", "NUMERIC", "ALPHANUMERIC" -> {
                assertEquals(expectedContent, result, "Text content should match exactly")
            }
            "CONTACT" -> {
                assertTrue(result.contains(expectedContent.substringBefore(":")), 
                          "Contact QR should contain expected protocol")
            }
            "WIFI" -> {
                assertTrue(result.startsWith("WIFI:"), "WiFi QR should start with WIFI:")
                assertTrue(result.contains("TestNetwork"), "WiFi QR should contain network name")
            }
            "LOCATION" -> {
                assertTrue(result.startsWith("geo:"), "Location QR should start with geo:")
            }
            "JSON" -> {
                assertTrue(result.contains("{") && result.contains("}"), "JSON should contain braces")
            }
            "UNICODE" -> {
                assertTrue(result.contains("Unicode"), "Unicode test should contain 'Unicode'")
            }
            "SPECIAL" -> {
                assertTrue(result.contains("Special chars"), "Special chars test should contain identifier")
            }
            "MULTILINE" -> {
                assertTrue(result.contains("\n") || result.contains("\\n"), "Multiline should contain line breaks")
            }
            "LARGE_TEXT" -> {
                assertTrue(result.length > 100, "Large text should be longer than 100 characters")
                assertTrue(result.contains("Lorem ipsum"), "Should contain Lorem ipsum text")
            }
        }
        
        println("✅ Successfully read $contentType QR code from $filename: ${result.take(50)}${if(result.length > 50) "..." else ""}")
    }
    
    @ParameterizedTest
    @MethodSource("imageFormatTestData")
    @DisplayName("Should handle different image formats")
    fun shouldHandleDifferentImageFormats(filename: String, format: String) {
        // Given
        val testImagePath = "test-images/$filename"
        val testFile = File(testImagePath)
        
        if (!testFile.exists()) {
            println("⚠️ Test image not found: $testImagePath - skipping test")
            return
        }
        
        // When
        val image = ImageIO.read(testFile)
        val result = qrCodeReader.readQRCode(image)
        
        // Then
        assertNotNull(result, "Should be able to read QR code from $format image")
        assertTrue(result.isNotEmpty(), "Content should not be empty")
        
        // Verify image properties
        assertNotNull(image, "Image should be loaded successfully")
        assertTrue(image.width > 0 && image.height > 0, "Image should have valid dimensions")
        
        println("✅ Successfully processed $format image: ${image.width}x${image.height}")
    }
    
    @ParameterizedTest
    @ValueSource(strings = ["qr-hello-world.png", "qr-long-text.png", "qr-unicode.png"])
    @DisplayName("Should consistently read the same QR code multiple times")
    fun shouldBeConsistentAcrossMultipleReads(filename: String) {
        // Given
        val testImagePath = "test-images/$filename"
        val testFile = File(testImagePath)
        
        if (!testFile.exists()) {
            println("⚠️ Test image not found: $testImagePath - skipping test")
            return
        }
        
        // When - read the same QR code multiple times
        val results = mutableListOf<String>()
        repeat(5) {
            results.add(qrCodeReader.readQRCode(testImagePath))
        }
        
        // Then - all results should be identical
        val firstResult = results.first()
        assertTrue(results.all { it == firstResult }, "All reads should produce identical results")
        assertNotNull(firstResult)
        assertTrue(firstResult.isNotEmpty())
        
        println("✅ Consistent results across 5 reads for $filename")
    }
    
    @ParameterizedTest    @CsvSource(
        "qr-hello-world.png, 43",
        "qr-long-text.png, 191",
        "qr-large-content.png, 1000"
    )
    @DisplayName("Should handle different content lengths appropriately")
    fun shouldHandleDifferentContentLengths(filename: String, expectedMinLength: Int) {
        // Given
        val testImagePath = "test-images/$filename"
        val testFile = File(testImagePath)
        
        if (!testFile.exists()) {
            println("⚠️ Test image not found: $testImagePath - skipping test")
            return
        }
        
        // When
        val startTime = System.currentTimeMillis()
        val result = qrCodeReader.readQRCode(testImagePath)
        val duration = System.currentTimeMillis() - startTime
        
        // Then
        assertNotNull(result)
        assertTrue(result.length >= expectedMinLength, 
                  "Content should be at least $expectedMinLength characters, but was ${result.length}")
        
        // Performance check - longer content shouldn't take disproportionately longer
        assertTrue(duration < 5000, "Reading should complete within 5 seconds, took ${duration}ms")
        
        println("✅ Read ${result.length} characters in ${duration}ms from $filename")
    }
    
    @ParameterizedTest
    @MethodSource("contentTypeCategories")
    @DisplayName("Should categorize QR code content types correctly")
    fun shouldCategorizeContentTypesCorrectly(category: String, filenames: List<String>) {
        // Given & When
        val results = mutableListOf<String>()
        
        filenames.forEach { filename ->
            val testImagePath = "test-images/$filename"
            val testFile = File(testImagePath)
            
            if (testFile.exists()) {
                try {
                    val content = qrCodeReader.readQRCode(testImagePath)
                    results.add(content)
                } catch (e: Exception) {
                    println("⚠️ Failed to read $filename: ${e.message}")
                }
            }
        }
        
        // Then - verify category-specific patterns
        assertTrue(results.isNotEmpty(), "Should have at least one result for category $category")
        
        when (category) {
            "URL" -> {
                assertTrue(results.all { it.startsWith("http") }, 
                          "All URL QR codes should start with http")
            }
            "TEXT" -> {
                assertTrue(results.all { it.isNotEmpty() && !it.startsWith("http") }, 
                          "Text QR codes should contain readable text")
            }
            "CONTACT" -> {
                assertTrue(results.any { it.contains("tel:") || it.contains("mailto:") || it.contains("smsto:") }, 
                          "Contact QR codes should contain contact protocols")
            }
            "STRUCTURED" -> {
                assertTrue(results.any { it.contains(":") || it.contains("{") }, 
                          "Structured QR codes should contain delimiters")
            }
        }
        
        println("✅ Successfully categorized $category: ${results.size} QR codes")
    }
    
    @Test
    @DisplayName("Should handle BufferedImage input correctly")
    fun shouldHandleBufferedImageInput() {
        // Given
        val testImagePath = "test-images/qr-hello-world.png"
        val testFile = File(testImagePath)
        
        if (!testFile.exists()) {
            println("⚠️ Test image not found: $testImagePath - skipping test")
            return
        }
        
        val bufferedImage = ImageIO.read(testFile)
        
        // When
        val result = qrCodeReader.readQRCode(bufferedImage)
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        assertTrue(result.contains("Hello"), "Should contain 'Hello' text")
        
        println("✅ Successfully read QR code from BufferedImage")
    }
    
    @ParameterizedTest
    @MethodSource("errorScenarios")
    @DisplayName("Should handle error scenarios appropriately")
    fun shouldHandleErrorScenarios(filename: String, expectedException: Class<out Exception>) {
        // Given
        val testImagePath = "test-images/$filename"
        
        // When & Then
        assertThrows(expectedException) {
            qrCodeReader.readQRCode(testImagePath)
        }
        
        println("✅ Correctly threw ${expectedException.simpleName} for $filename")
    }
    
    @Test
    @DisplayName("Should provide meaningful error messages")
    fun shouldProvideMeaningfulErrorMessages() {
        // Given
        val nonExistentFile = "test-images/does-not-exist.png"
        
        // When & Then
        val exception = assertThrows(IllegalArgumentException::class.java) {
            qrCodeReader.readQRCode(nonExistentFile)
        }
        
        assertTrue(exception.message!!.contains("non trouvé") || exception.message!!.contains("not found"),
                  "Error message should indicate file not found: ${exception.message}")
        
        println("✅ Meaningful error message: ${exception.message}")
    }
    
    @Test
    @DisplayName("Should handle various image sizes")
    fun shouldHandleVariousImageSizes() {
        // Given - test with available images of different potential sizes
        val testImages = listOf("qr-hello-world.png", "qr-test-hello.png", "qr-large-content.png")
        
        testImages.forEach { filename ->
            val testImagePath = "test-images/$filename"
            val testFile = File(testImagePath)
            
            if (testFile.exists()) {
                // When
                val image = ImageIO.read(testFile)
                val result = qrCodeReader.readQRCode(image)
                
                // Then
                assertNotNull(result)
                assertTrue(result.isNotEmpty())
                
                println("✅ Successfully read ${image.width}x${image.height} image: $filename")
            }
        }
    }
}

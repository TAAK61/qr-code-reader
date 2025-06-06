package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.awt.Color
import java.awt.image.BufferedImage

class ImageUtilsTest {

    @Test
    fun testConvertToGrayscale() {
        // Create a test image with colored pixels
        val width = 10
        val height = 10
        val testImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = testImage.createGraphics()

        // Fill with red color
        graphics.color = Color.RED
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()

        // Convert to grayscale
        val grayscaleImage = ImageUtils.convertToGrayscale(testImage)

        // Check that the image type is grayscale
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, grayscaleImage.type)

        // Check dimensions are preserved
        assertEquals(width, grayscaleImage.width)
        assertEquals(height, grayscaleImage.height)

        // Sample a pixel to verify it's gray (R=G=B)
        val rgb = grayscaleImage.getRGB(5, 5)
        val color = Color(rgb)

        // In grayscale, all color components should be approximately equal
        assertEquals(color.red, color.green)
        assertEquals(color.green, color.blue)

        println("[DEBUG_LOG] Grayscale test passed. RGB values: R=${color.red}, G=${color.green}, B=${color.blue}")
    }

    @Test
    fun testEnhanceContrast() {
        // Create a test image with a gray value that will be affected by contrast enhancement
        val width = 10
        val height = 10
        val testImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val graphics = testImage.createGraphics()

        // Fill with a gray value of 100 (not medium gray)
        val originalGray = 100
        graphics.color = Color(originalGray, originalGray, originalGray)
        graphics.fillRect(0, 0, width, height)
        graphics.dispose()

        // Enhance contrast
        val enhancedImage = ImageUtils.enhanceContrast(testImage)

        // Check dimensions are preserved
        assertEquals(width, enhancedImage.width)
        assertEquals(height, enhancedImage.height)

        // Sample a pixel to verify contrast was enhanced
        val rgb = enhancedImage.getRGB(5, 5)
        val color = Color(rgb)

        // With the contrast factor of 1.5, a gray value of 100 should change
        println("[DEBUG_LOG] Original gray: $originalGray")
        println("[DEBUG_LOG] Enhanced contrast values: R=${color.red}, G=${color.green}, B=${color.blue}")

        // Calculate the expected value using the same formula as in ImageUtils
        val factor = 1.5f
        val expected = (factor * (originalGray - 128) + 128).toInt()
        println("[DEBUG_LOG] Expected value: $expected")

        // The value should be different from the original gray value
        assertNotEquals(originalGray, color.red)
        // The value should match our calculated expected value
        assertEquals(expected, color.red)
    }
}

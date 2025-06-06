package utils.image

import utils.InputValidator
import utils.Logger
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * Utility class for converting images to grayscale.
 * 
 * This class provides different algorithms for grayscale conversion,
 * each optimized for specific use cases.
 */
class GrayscaleUtils {
    
    companion object {
        private const val TAG = "GrayscaleUtils"
    }
    
    /**
     * Enumeration of available grayscale conversion algorithms.
     */
    enum class Algorithm {
        /** Simple average of RGB values */
        AVERAGE,
        /** Luminance-based conversion (ITU-R BT.709) */
        LUMINANCE,
        /** Desaturated conversion preserving brightness */
        DESATURATE,
        /** Simple conversion using only the red channel */
        RED_CHANNEL
    }
    
    /**
     * Converts an image to grayscale using the specified algorithm.
     * 
     * @param image The source image to convert
     * @param algorithm The conversion algorithm to use
     * @return The grayscale image
     */
    fun convertToGrayscale(image: BufferedImage, algorithm: Algorithm = Algorithm.LUMINANCE): BufferedImage {
        InputValidator.validateImageDimensions(image.width, image.height)
        
        Logger.debug(TAG, "Converting image to grayscale using $algorithm algorithm")
        val startTime = System.currentTimeMillis()
        
        val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val rgb = image.getRGB(x, y)
                val grayValue = when (algorithm) {
                    Algorithm.AVERAGE -> calculateAverageGray(rgb)
                    Algorithm.LUMINANCE -> calculateLuminanceGray(rgb)
                    Algorithm.DESATURATE -> calculateDesaturatedGray(rgb)
                    Algorithm.RED_CHANNEL -> calculateRedChannelGray(rgb)
                }
                
                val grayRgb = Color(grayValue, grayValue, grayValue).rgb
                grayImage.setRGB(x, y, grayRgb)
            }
        }
        
        val processingTime = System.currentTimeMillis() - startTime
        Logger.debug(TAG, "Grayscale conversion completed in ${processingTime}ms")
        
        return grayImage
    }
    
    /**
     * Calculates grayscale value using simple RGB average.
     */
    private fun calculateAverageGray(rgb: Int): Int {
        val color = Color(rgb)
        return (color.red + color.green + color.blue) / 3
    }
    
    /**
     * Calculates grayscale value using luminance formula (ITU-R BT.709).
     * This provides the most perceptually accurate grayscale conversion.
     */
    private fun calculateLuminanceGray(rgb: Int): Int {
        val color = Color(rgb)
        return (0.2126 * color.red + 0.7152 * color.green + 0.0722 * color.blue).toInt()
    }
    
    /**
     * Calculates grayscale value using desaturation method.
     */
    private fun calculateDesaturatedGray(rgb: Int): Int {
        val color = Color(rgb)
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        return (max + min) / 2
    }
    
    /**
     * Calculates grayscale value using only the red channel.
     * This is the fastest method but may not preserve image details well.
     */
    private fun calculateRedChannelGray(rgb: Int): Int {
        return Color(rgb).red
    }
    
    /**
     * Checks if an image is already in grayscale.
     * 
     * @param image The image to check
     * @return true if the image is grayscale, false otherwise
     */
    fun isGrayscale(image: BufferedImage): Boolean {
        // Sample a few pixels to determine if image is grayscale
        val samplePoints = listOf(
            Pair(0, 0),
            Pair(image.width / 2, image.height / 2),
            Pair(image.width - 1, image.height - 1),
            Pair(image.width / 4, image.height / 4),
            Pair(3 * image.width / 4, 3 * image.height / 4)
        )
        
        for ((x, y) in samplePoints) {
            val color = Color(image.getRGB(x, y))
            if (color.red != color.green || color.green != color.blue) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Converts an image to grayscale with performance optimization.
     * Uses the fastest appropriate method based on image characteristics.
     * 
     * @param image The source image
     * @return The optimized grayscale image
     */
    fun convertToGrayscaleOptimized(image: BufferedImage): BufferedImage {
        return when {
            isGrayscale(image) -> {
                Logger.debug(TAG, "Image is already grayscale, skipping conversion")
                image
            }
            image.width * image.height > 1_000_000 -> {
                Logger.debug(TAG, "Large image detected, using fast conversion")
                convertToGrayscale(image, Algorithm.RED_CHANNEL)
            }
            else -> {
                convertToGrayscale(image, Algorithm.LUMINANCE)
            }
        }
    }
}
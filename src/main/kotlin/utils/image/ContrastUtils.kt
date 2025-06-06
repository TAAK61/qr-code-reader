package utils.image

import utils.InputValidator
import utils.Logger
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Utility class for enhancing image contrast.
 * 
 * This class provides various contrast enhancement algorithms
 * to improve QR code detection accuracy.
 */
class ContrastUtils {
    
    companion object {
        private const val TAG = "ContrastUtils"
    }
    
    /**
     * Enumeration of available contrast enhancement methods.
     */
    enum class Method {
        /** Linear contrast stretching */
        LINEAR,
        /** Histogram equalization */
        HISTOGRAM_EQUALIZATION,
        /** Adaptive contrast enhancement */
        ADAPTIVE,
        /** Gamma correction */
        GAMMA_CORRECTION
    }
    
    /**
     * Enhances the contrast of an image using the specified method.
     * 
     * @param image The source image
     * @param factor The enhancement factor (typically 1.0-3.0)
     * @param method The contrast enhancement method
     * @return The contrast-enhanced image
     */
    fun enhanceContrast(
        image: BufferedImage, 
        factor: Double = 1.5, 
        method: Method = Method.LINEAR
    ): BufferedImage {
        InputValidator.validateImageDimensions(image.width, image.height)
        InputValidator.validateContrastFactor(factor)
        
        Logger.debug(TAG, "Enhancing contrast using $method method with factor $factor")
        val startTime = System.currentTimeMillis()
        
        val enhancedImage = when (method) {
            Method.LINEAR -> enhanceLinearContrast(image, factor)
            Method.HISTOGRAM_EQUALIZATION -> enhanceWithHistogramEqualization(image)
            Method.ADAPTIVE -> enhanceAdaptiveContrast(image, factor)
            Method.GAMMA_CORRECTION -> enhanceWithGammaCorrection(image, factor)
        }
        
        val processingTime = System.currentTimeMillis() - startTime
        Logger.debug(TAG, "Contrast enhancement completed in ${processingTime}ms")
        
        return enhancedImage
    }
    
    /**
     * Enhances contrast using linear stretching.
     */
    private fun enhanceLinearContrast(image: BufferedImage, factor: Double): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))
                
                val newRed = clampColor((color.red - 128) * factor + 128)
                val newGreen = clampColor((color.green - 128) * factor + 128)
                val newBlue = clampColor((color.blue - 128) * factor + 128)
                
                val newColor = Color(newRed, newGreen, newBlue)
                enhanced.setRGB(x, y, newColor.rgb)
            }
        }
        
        return enhanced
    }
    
    /**
     * Enhances contrast using histogram equalization.
     */
    private fun enhanceWithHistogramEqualization(image: BufferedImage): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        
        // Calculate histogram
        val histogram = IntArray(256)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))
                val gray = (0.3 * color.red + 0.59 * color.green + 0.11 * color.blue).toInt()
                histogram[gray]++
            }
        }
        
        // Calculate cumulative distribution
        val cdf = IntArray(256)
        cdf[0] = histogram[0]
        for (i in 1 until 256) {
            cdf[i] = cdf[i - 1] + histogram[i]
        }
        
        // Normalize CDF
        val totalPixels = image.width * image.height
        val lookupTable = IntArray(256) { i ->
            ((cdf[i].toDouble() / totalPixels) * 255).toInt()
        }
        
        // Apply equalization
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))
                val gray = (0.3 * color.red + 0.59 * color.green + 0.11 * color.blue).toInt()
                val newValue = lookupTable[gray]
                
                val newColor = Color(newValue, newValue, newValue)
                enhanced.setRGB(x, y, newColor.rgb)
            }
        }
        
        return enhanced
    }
    
    /**
     * Enhances contrast using adaptive method.
     */
    private fun enhanceAdaptiveContrast(image: BufferedImage, factor: Double): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        val windowSize = 7 // Size of the local window
        val halfWindow = windowSize / 2
        
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val localMean = calculateLocalMean(image, x, y, halfWindow)
                val color = Color(image.getRGB(x, y))
                
                val newRed = clampColor((color.red - localMean) * factor + localMean)
                val newGreen = clampColor((color.green - localMean) * factor + localMean)
                val newBlue = clampColor((color.blue - localMean) * factor + localMean)
                
                val newColor = Color(newRed, newGreen, newBlue)
                enhanced.setRGB(x, y, newColor.rgb)
            }
        }
        
        return enhanced
    }
    
    /**
     * Enhances contrast using gamma correction.
     */
    private fun enhanceWithGammaCorrection(image: BufferedImage, gamma: Double): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        val gammaCorrection = 1.0 / gamma
        
        // Create lookup table for gamma correction
        val lookupTable = IntArray(256) { i ->
            (255 * (i / 255.0).pow(gammaCorrection)).toInt()
        }
        
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y))
                
                val newRed = lookupTable[color.red]
                val newGreen = lookupTable[color.green]
                val newBlue = lookupTable[color.blue]
                
                val newColor = Color(newRed, newGreen, newBlue)
                enhanced.setRGB(x, y, newColor.rgb)
            }
        }
        
        return enhanced
    }
    
    /**
     * Calculates the local mean around a pixel.
     */
    private fun calculateLocalMean(image: BufferedImage, centerX: Int, centerY: Int, radius: Int): Int {
        var sum = 0
        var count = 0
        
        for (x in max(0, centerX - radius)..min(image.width - 1, centerX + radius)) {
            for (y in max(0, centerY - radius)..min(image.height - 1, centerY + radius)) {
                val color = Color(image.getRGB(x, y))
                sum += (color.red + color.green + color.blue) / 3
                count++
            }
        }
        
        return if (count > 0) sum / count else 0
    }
    
    /**
     * Clamps a color value to the valid range [0, 255].
     */
    private fun clampColor(value: Double): Int {
        return when {
            value < 0 -> 0
            value > 255 -> 255
            else -> value.toInt()
        }
    }
    
    /**
     * Analyzes image contrast and suggests the best enhancement method.
     * 
     * @param image The image to analyze
     * @return Recommended contrast enhancement method
     */
    fun recommendContrastMethod(image: BufferedImage): Method {
        val contrast = calculateImageContrast(image)
        
        return when {
            contrast < 0.2 -> Method.HISTOGRAM_EQUALIZATION
            contrast < 0.5 -> Method.ADAPTIVE
            contrast < 0.8 -> Method.LINEAR
            else -> Method.GAMMA_CORRECTION
        }
    }
    
    /**
     * Calculates the overall contrast of an image.
     * 
     * @param image The image to analyze
     * @return Contrast value between 0.0 and 1.0
     */
    fun calculateImageContrast(image: BufferedImage): Double {
        var minBrightness = 255.0
        var maxBrightness = 0.0
        
        // Sample every 10th pixel for performance
        for (x in 0 until image.width step 10) {
            for (y in 0 until image.height step 10) {
                val color = Color(image.getRGB(x, y))
                val brightness = (color.red + color.green + color.blue) / 3.0
                
                minBrightness = min(minBrightness, brightness)
                maxBrightness = max(maxBrightness, brightness)
            }
        }
        
        return (maxBrightness - minBrightness) / 255.0
    }
}
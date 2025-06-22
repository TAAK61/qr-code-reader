package qr

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import utils.ImageProcessor
import utils.PerformanceProfiler
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Color
import java.awt.image.ConvolveOp
import java.awt.image.Kernel
import kotlin.math.*

/**
 * Enhanced QR code reader for damaged or partially visible QR codes
 * Task 77: Add support for reading damaged or partially visible QR codes
 */
class EnhancedQRReader {
    
    private val qrReader = QRCodeReader()
    private val imageProcessor = ImageProcessor()
    
    data class RecoveryResult(
        val content: String?,
        val confidence: Double,
        val method: String,
        val attemptsUsed: Int,
        val processingTimeMs: Long
    )
    
    /**
     * Attempt to read QR code with multiple recovery strategies
     */
    fun readDamagedQRCode(image: BufferedImage): RecoveryResult {
        return PerformanceProfiler.measureOperation("readDamagedQR") {
            val startTime = System.currentTimeMillis()
            var attempts = 0
            var bestResult: String? = null
            var bestConfidence = 0.0
            var bestMethod = "none"
            
            // Strategy 1: Direct reading
            attempts++
            try {
                val result = readQRCodeDirect(image)
                if (result != null) {
                    return@measureOperation RecoveryResult(
                        content = result,
                        confidence = 1.0,
                        method = "direct",
                        attemptsUsed = attempts,
                        processingTimeMs = System.currentTimeMillis() - startTime
                    )
                }
            } catch (e: Exception) {
                // Continue to next strategy
            }
            
            // Strategy 2: Image enhancement + reading
            attempts++
            try {
                val enhanced = enhanceImage(image)
                val result = readQRCodeDirect(enhanced)
                if (result != null) {
                    return@measureOperation RecoveryResult(
                        content = result,
                        confidence = 0.9,
                        method = "enhanced",
                        attemptsUsed = attempts,
                        processingTimeMs = System.currentTimeMillis() - startTime
                    )
                }
            } catch (e: Exception) {
                // Continue to next strategy
            }
            
            // Strategy 3: Multiple binarizers
            attempts++
            try {
                val result = tryMultipleBinarizers(image)
                if (result != null) {
                    bestResult = result
                    bestConfidence = 0.8
                    bestMethod = "multi-binarizer"
                }
            } catch (e: Exception) {
                // Continue to next strategy
            }
            
            // Strategy 4: Rotation attempts
            if (bestResult == null) {
                attempts++
                try {
                    val result = tryRotations(image)
                    if (result != null) {
                        bestResult = result
                        bestConfidence = 0.7
                        bestMethod = "rotation"
                    }
                } catch (e: Exception) {
                    // Continue to next strategy
                }
            }
            
            // Strategy 5: Scaling attempts
            if (bestResult == null) {
                attempts++
                try {
                    val result = tryScaling(image)
                    if (result != null) {
                        bestResult = result
                        bestConfidence = 0.6
                        bestMethod = "scaling"
                    }
                } catch (e: Exception) {
                    // Continue to next strategy
                }
            }
            
            // Strategy 6: Perspective correction
            if (bestResult == null) {
                attempts++
                try {
                    val result = tryPerspectiveCorrection(image)
                    if (result != null) {
                        bestResult = result
                        bestConfidence = 0.5
                        bestMethod = "perspective"
                    }
                } catch (e: Exception) {
                    // Continue to next strategy
                }
            }
            
            // Strategy 7: Noise reduction + sharpening
            if (bestResult == null) {
                attempts++
                try {
                    val result = tryNoiseReductionAndSharpening(image)
                    if (result != null) {
                        bestResult = result
                        bestConfidence = 0.4
                        bestMethod = "denoise-sharpen"
                    }
                } catch (e: Exception) {
                    // Final attempt failed
                }
            }
            
            RecoveryResult(
                content = bestResult,
                confidence = bestConfidence,
                method = bestMethod,
                attemptsUsed = attempts,
                processingTimeMs = System.currentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Direct QR code reading
     */
    private fun readQRCodeDirect(image: BufferedImage): String? {
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = qrReader.decode(bitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Enhance image for better QR code detection
     */    private fun enhanceImage(image: BufferedImage): BufferedImage {
        val options = ImageProcessor.ImageProcessingOptions(
            enhanceContrast = true,
            contrastFactor = 2.0,
            reduceNoise = true,
            resizeIfLarge = false,
            useHighQualityResize = true
        )
        return imageProcessor.processImage(image, options).processedImage
    }
    
    /**
     * Try multiple binarizers
     */
    private fun tryMultipleBinarizers(image: BufferedImage): String? {
        val source = BufferedImageLuminanceSource(image)
        
        // Try HybridBinarizer
        try {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = qrReader.decode(bitmap)
            return result.text
        } catch (e: Exception) {
            // Continue
        }
        
        // Try GlobalHistogramBinarizer
        try {
            val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
            val result = qrReader.decode(bitmap)
            return result.text
        } catch (e: Exception) {
            // Failed both
        }
        
        return null
    }
    
    /**
     * Try different rotations
     */
    private fun tryRotations(image: BufferedImage): String? {
        val angles = listOf(90.0, 180.0, 270.0, 45.0, 135.0, 225.0, 315.0)
        
        for (angle in angles) {
            try {
                val rotated = rotateImage(image, angle)
                val result = readQRCodeDirect(rotated)
                if (result != null) return result
            } catch (e: Exception) {
                // Continue with next angle
            }
        }
        
        return null
    }
    
    /**
     * Try different scaling factors
     */
    private fun tryScaling(image: BufferedImage): String? {
        val scales = listOf(0.5, 1.5, 2.0, 0.75, 1.25, 0.25, 3.0)
        
        for (scale in scales) {
            try {
                val scaled = scaleImage(image, scale)
                val result = readQRCodeDirect(scaled)
                if (result != null) return result
            } catch (e: Exception) {
                // Continue with next scale
            }
        }
        
        return null
    }
    
    /**
     * Try perspective correction (simple skew correction)
     */
    private fun tryPerspectiveCorrection(image: BufferedImage): String? {
        val skewAngles = listOf(-15.0, -10.0, -5.0, 5.0, 10.0, 15.0)
        
        for (angle in skewAngles) {
            try {
                val corrected = applySkewCorrection(image, angle)
                val result = readQRCodeDirect(corrected)
                if (result != null) return result
            } catch (e: Exception) {
                // Continue with next angle
            }
        }
        
        return null
    }
    
    /**
     * Try noise reduction and sharpening
     */
    private fun tryNoiseReductionAndSharpening(image: BufferedImage): String? {
        try {
            // Apply median filter for noise reduction
            val denoised = applyMedianFilter(image)
            
            // Apply sharpening
            val sharpened = applySharpeningFilter(denoised)
            
            return readQRCodeDirect(sharpened)
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Rotate image by specified angle
     */
    private fun rotateImage(image: BufferedImage, angleDegrees: Double): BufferedImage {
        val angle = Math.toRadians(angleDegrees)
        val sin = abs(sin(angle))
        val cos = abs(cos(angle))
        
        val newWidth = (image.width * cos + image.height * sin).toInt()
        val newHeight = (image.width * sin + image.height * cos).toInt()
        
        val rotated = BufferedImage(newWidth, newHeight, image.type)
        val g2d = rotated.createGraphics()
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.translate(newWidth / 2, newHeight / 2)
        g2d.rotate(angle)
        g2d.translate(-image.width / 2, -image.height / 2)
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        
        return rotated
    }
    
    /**
     * Scale image by specified factor
     */
    private fun scaleImage(image: BufferedImage, scale: Double): BufferedImage {
        val newWidth = (image.width * scale).toInt()
        val newHeight = (image.height * scale).toInt()
        
        val scaled = BufferedImage(newWidth, newHeight, image.type)
        val g2d = scaled.createGraphics()
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null)
        g2d.dispose()
        
        return scaled
    }
    
    /**
     * Apply simple skew correction
     */
    private fun applySkewCorrection(image: BufferedImage, skewAngle: Double): BufferedImage {
        val angle = Math.toRadians(skewAngle)
        val corrected = BufferedImage(image.width, image.height, image.type)
        val g2d = corrected.createGraphics()
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.translate(image.width / 2, image.height / 2)
        g2d.shear(tan(angle), 0.0)
        g2d.translate(-image.width / 2, -image.height / 2)
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        
        return corrected
    }
    
    /**
     * Apply median filter for noise reduction
     */
    private fun applyMedianFilter(image: BufferedImage): BufferedImage {
        val filtered = BufferedImage(image.width, image.height, image.type)
        val windowSize = 3
        val offset = windowSize / 2
        
        for (y in offset until image.height - offset) {
            for (x in offset until image.width - offset) {
                val values = mutableListOf<Int>()
                
                for (dy in -offset..offset) {
                    for (dx in -offset..offset) {
                        val rgb = image.getRGB(x + dx, y + dy)
                        val gray = ((rgb shr 16) and 0xFF) // Just red component for grayscale
                        values.add(gray)
                    }
                }
                
                values.sort()
                val median = values[values.size / 2]
                val newRgb = (255 shl 24) or (median shl 16) or (median shl 8) or median
                filtered.setRGB(x, y, newRgb)
            }
        }
        
        return filtered
    }
    
    /**
     * Apply sharpening filter
     */
    private fun applySharpeningFilter(image: BufferedImage): BufferedImage {
        val sharpeningKernel = floatArrayOf(
            0f, -1f, 0f,
            -1f, 5f, -1f,
            0f, -1f, 0f
        )
        
        val kernel = Kernel(3, 3, sharpeningKernel)
        val op = ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null)
        
        return op.filter(image, null)
    }
    
    /**
     * Get damage assessment of QR code
     */
    fun assessQRCodeDamage(image: BufferedImage): Map<String, Any> {
        val assessment = mutableMapOf<String, Any>()
        
        // Basic metrics
        assessment["imageWidth"] = image.width
        assessment["imageHeight"] = image.height
        assessment["imageSize"] = image.width * image.height
        
        // Try to detect QR code patterns
        val hasFinderPatterns = detectFinderPatterns(image)
        assessment["finderPatternsDetected"] = hasFinderPatterns
        
        // Estimate contrast
        val contrast = estimateContrast(image)
        assessment["contrast"] = contrast
        assessment["lowContrast"] = contrast < 50
        
        // Estimate noise level
        val noiseLevel = estimateNoiseLevel(image)
        assessment["noiseLevel"] = noiseLevel
        assessment["highNoise"] = noiseLevel > 0.3
        
        // Overall damage assessment
        val damageScore = calculateDamageScore(hasFinderPatterns, contrast, noiseLevel)
        assessment["damageScore"] = damageScore
        assessment["damageLevel"] = when {
            damageScore < 0.3 -> "Low"
            damageScore < 0.6 -> "Medium"
            damageScore < 0.8 -> "High"
            else -> "Severe"
        }
        
        return assessment
    }
    
    private fun detectFinderPatterns(image: BufferedImage): Boolean {
        // Simplified finder pattern detection
        // Look for 1:1:3:1:1 ratio patterns (black:white:black:white:black)
        val threshold = 128
        var patternsFound = 0
        
        // Check horizontal lines
        for (y in 0 until image.height step 10) {
            for (x in 0 until image.width - 20 step 5) {
                if (checkFinderPatternRatio(image, x, y, true, threshold)) {
                    patternsFound++
                }
            }
        }
        
        // Check vertical lines
        for (x in 0 until image.width step 10) {
            for (y in 0 until image.height - 20 step 5) {
                if (checkFinderPatternRatio(image, x, y, false, threshold)) {
                    patternsFound++
                }
            }
        }
        
        return patternsFound >= 3 // At least 3 potential finder patterns
    }
    
    private fun checkFinderPatternRatio(
        image: BufferedImage, 
        startX: Int, 
        startY: Int, 
        horizontal: Boolean, 
        threshold: Int
    ): Boolean {
        val length = 15
        val pixels = IntArray(length)
        
        for (i in 0 until length) {
            val x = if (horizontal) startX + i else startX
            val y = if (horizontal) startY else startY + i
            
            if (x >= image.width || y >= image.height) return false
            
            val rgb = image.getRGB(x, y)
            val gray = ((rgb shr 16) and 0xFF)
            pixels[i] = if (gray < threshold) 1 else 0 // 1 for black, 0 for white
        }
        
        // Look for 1:1:3:1:1 pattern (simplified)
        var transitions = 0
        for (i in 1 until length) {
            if (pixels[i] != pixels[i-1]) transitions++
        }
        
        return transitions >= 4 && transitions <= 8 // Reasonable number of transitions
    }
    
    private fun estimateContrast(image: BufferedImage): Double {
        var minGray = 255
        var maxGray = 0
        
        val sampleSize = 1000
        val random = kotlin.random.Random.Default
        
        repeat(sampleSize) {
            val x = random.nextInt(image.width)
            val y = random.nextInt(image.height)
            val rgb = image.getRGB(x, y)
            val gray = ((rgb shr 16) and 0xFF)
            
            minGray = minOf(minGray, gray)
            maxGray = maxOf(maxGray, gray)
        }
        
        return (maxGray - minGray).toDouble()
    }
    
    private fun estimateNoiseLevel(image: BufferedImage): Double {
        var totalVariation = 0.0
        var samples = 0
        
        for (y in 1 until image.height - 1) {
            for (x in 1 until image.width - 1) {
                val center = getGrayValue(image, x, y)
                val neighbors = listOf(
                    getGrayValue(image, x-1, y),
                    getGrayValue(image, x+1, y),
                    getGrayValue(image, x, y-1),
                    getGrayValue(image, x, y+1)
                )
                
                val avgNeighbor = neighbors.average()
                totalVariation += abs(center - avgNeighbor)
                samples++
            }
        }
        
        return (totalVariation / samples) / 255.0 // Normalize to 0-1
    }
    
    private fun getGrayValue(image: BufferedImage, x: Int, y: Int): Double {
        val rgb = image.getRGB(x, y)
        return ((rgb shr 16) and 0xFF).toDouble()
    }
    
    private fun calculateDamageScore(hasFinderPatterns: Boolean, contrast: Double, noiseLevel: Double): Double {
        var score = 0.0
        
        if (!hasFinderPatterns) score += 0.4
        if (contrast < 50) score += 0.3
        if (noiseLevel > 0.3) score += 0.3
        
        return minOf(score, 1.0)
    }
}

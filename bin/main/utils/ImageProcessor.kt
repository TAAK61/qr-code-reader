package utils

import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.ConvolveOp
import java.awt.image.IndexColorModel
import java.awt.image.Kernel
import java.awt.image.RescaleOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

/**
 * Optimized image processing pipeline for QR code detection.
 * 
 * Implements Tasks 51, 54, 55, 57, 58, 60:
 * - Task 51: Optimize the image processing pipeline for better performance
 * - Task 54: Optimize memory usage during image processing
 * - Task 55: Implement lazy loading for large images
 * - Task 57: Optimize the contrast enhancement algorithm
 * - Task 58: Implement more efficient noise reduction algorithm
 * - Task 60: Optimize the resizing algorithm for better quality and performance
 */
class ImageProcessor {
    
    companion object {
        // Memory optimization constants
        private const val MAX_IMAGE_SIZE = 2048 // Maximum dimension for processing
        private const val MEMORY_THRESHOLD = 50 * 1024 * 1024 // 50MB threshold
        private val PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors()
        
        // Processing options cache (Task 54: Memory optimization)
        private val processingOptionsCache = mutableMapOf<String, ImageProcessingOptions>()
    }
    
    /**
     * Configuration for image processing operations
     */
    data class ImageProcessingOptions(
        val enhanceContrast: Boolean = true,
        val contrastFactor: Double = 1.5,
        val reduceNoise: Boolean = true,
        val resizeIfLarge: Boolean = true,
        val maxDimension: Int = MAX_IMAGE_SIZE,
        val useHighQualityResize: Boolean = true,
        val preserveAspectRatio: Boolean = true
    ) {
        // Generate cache key for options
        fun toCacheKey(): String = "${enhanceContrast}_${contrastFactor}_${reduceNoise}_${resizeIfLarge}_${maxDimension}_${useHighQualityResize}"
    }
    
    /**
     * Results of image processing operation
     */
    data class ProcessingResult(
        val processedImage: BufferedImage,
        val originalSize: Pair<Int, Int>,
        val processedSize: Pair<Int, Int>,
        val processingTimeMs: Long,
        val memoryUsedBytes: Long,
        val operationsApplied: List<String>
    )
    
    /**
     * Task 55: Lazy loading wrapper for large images
     */
    class LazyImage(private val imageData: ByteArray) {
        private var _bufferedImage: BufferedImage? = null
        private var _metadata: ImageMetadata? = null
        
        data class ImageMetadata(
            val width: Int,
            val height: Int,
            val estimatedMemoryUsage: Long,
            val isLarge: Boolean
        )
        
        val metadata: ImageMetadata
            get() {
                if (_metadata == null) {
                    // Load metadata without loading full image
                    val input = ByteArrayInputStream(imageData)
                    val reader = ImageIO.getImageReaders(ImageIO.createImageInputStream(input)).next()
                    reader.input = ImageIO.createImageInputStream(input)
                    
                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)
                    val estimatedMemory = (width * height * 4).toLong() // 4 bytes per pixel (ARGB)
                    val isLarge = estimatedMemory > MEMORY_THRESHOLD
                    
                    _metadata = ImageMetadata(width, height, estimatedMemory, isLarge)
                    reader.dispose()
                }
                return _metadata!!
            }
        
        val bufferedImage: BufferedImage
            get() {
                if (_bufferedImage == null) {
                    _bufferedImage = ImageIO.read(ByteArrayInputStream(imageData))
                }
                return _bufferedImage!!
            }
        
        fun requiresDownsampling(maxDimension: Int): Boolean {
            return metadata.width > maxDimension || metadata.height > maxDimension
        }
    }
    
    /**
     * Task 51: Optimized image processing pipeline
     */
    fun processImage(image: BufferedImage, options: ImageProcessingOptions = ImageProcessingOptions()): ProcessingResult {
        val startTime = System.currentTimeMillis()
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()
        
        val originalSize = Pair(image.width, image.height)
        val operationsApplied = mutableListOf<String>()
        
        var processedImage = image
        
        // Task 55: Check if image needs downsampling for memory optimization
        if (options.resizeIfLarge && (image.width > options.maxDimension || image.height > options.maxDimension)) {
            processedImage = optimizedResize(processedImage, options.maxDimension, options.useHighQualityResize)
            operationsApplied.add("resize")
        }
        
        // Task 58: Efficient noise reduction
        if (options.reduceNoise) {
            processedImage = efficientNoiseReduction(processedImage)
            operationsApplied.add("noise_reduction")
        }
        
        // Task 57: Optimized contrast enhancement
        if (options.enhanceContrast) {
            processedImage = optimizedContrastEnhancement(processedImage, options.contrastFactor)
            operationsApplied.add("contrast_enhancement")
        }
        
        val endTime = System.currentTimeMillis()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        
        return ProcessingResult(
            processedImage = processedImage,
            originalSize = originalSize,
            processedSize = Pair(processedImage.width, processedImage.height),
            processingTimeMs = endTime - startTime,
            memoryUsedBytes = finalMemory - initialMemory,
            operationsApplied = operationsApplied
        )
    }
    
    /**
     * Task 60: Optimized resizing algorithm for better quality and performance
     */
    private fun optimizedResize(image: BufferedImage, maxDimension: Int, highQuality: Boolean): BufferedImage {
        val originalWidth = image.width
        val originalHeight = image.height
        
        // Calculate new dimensions preserving aspect ratio
        val scale = min(maxDimension.toDouble() / originalWidth, maxDimension.toDouble() / originalHeight)
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()
        
        // Use different algorithms based on quality requirements and image size
        return if (highQuality && (originalWidth > newWidth * 2 || originalHeight > newHeight * 2)) {
            // Multi-step downsampling for high quality
            multiStepResize(image, newWidth, newHeight)
        } else {
            // Fast single-step resize for moderate scaling
            fastResize(image, newWidth, newHeight)
        }
    }
    
    /**
     * Multi-step resize for high quality downsampling
     */
    private fun multiStepResize(image: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        var currentImage = image
        var currentWidth = image.width
        var currentHeight = image.height
        
        // Downsample in steps of 50% until we get close to target
        while (currentWidth > targetWidth * 2 || currentHeight > targetHeight * 2) {
            currentWidth = max(currentWidth / 2, targetWidth)
            currentHeight = max(currentHeight / 2, targetHeight)
            currentImage = fastResize(currentImage, currentWidth, currentHeight)
        }
        
        // Final resize to exact target dimensions
        if (currentWidth != targetWidth || currentHeight != targetHeight) {
            currentImage = fastResize(currentImage, targetWidth, targetHeight)
        }
        
        return currentImage
    }
    
    /**
     * Fast resize implementation with optimized rendering hints
     */
    private fun fastResize(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = resized.createGraphics()
        
        // Optimized rendering hints for performance vs quality balance
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        
        g2d.drawImage(image, 0, 0, width, height, null)
        g2d.dispose()
        
        return resized
    }
      /**
     * Task 57: Optimized contrast enhancement algorithm
     */
    private fun optimizedContrastEnhancement(image: BufferedImage, factor: Double): BufferedImage {
        // Check if image is indexed (palette-based) - RescaleOp doesn't work on indexed images
        val isIndexed = image.type == BufferedImage.TYPE_BYTE_INDEXED || 
                       image.colorModel is IndexColorModel
        
        // Use RescaleOp for hardware-accelerated contrast adjustment when possible
        if (!isIndexed && factor > 0.1 && factor < 5.0) {
            try {
                val rescaleOp = RescaleOp(factor.toFloat(), 0f, null)
                return rescaleOp.filter(image, null)
            } catch (e: IllegalArgumentException) {
                // If RescaleOp fails for any reason, fall back to manual enhancement
            }
        }
        
        // Fallback to manual pixel manipulation for indexed images or extreme values
        return manualContrastEnhancement(image, factor)
    }
    
    /**
     * Manual contrast enhancement for cases where RescaleOp isn't suitable
     */
    private fun manualContrastEnhancement(image: BufferedImage, factor: Double): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        
        // Process pixels in chunks for better cache locality
        val chunkSize = 1000
        for (y in 0 until image.height step chunkSize) {
            val endY = min(y + chunkSize, image.height)
            for (x in 0 until image.width step chunkSize) {
                val endX = min(x + chunkSize, image.width)
                processPixelChunk(image, enhanced, x, y, endX, endY, factor)
            }
        }
        
        return enhanced
    }
    
    /**
     * Process a chunk of pixels for contrast enhancement
     */
    private fun processPixelChunk(source: BufferedImage, dest: BufferedImage, 
                                 startX: Int, startY: Int, endX: Int, endY: Int, factor: Double) {
        for (y in startY until endY) {
            for (x in startX until endX) {
                val rgb = source.getRGB(x, y)
                val color = Color(rgb)
                
                // Apply contrast enhancement with clamping
                val red = clamp((color.red - 128) * factor + 128)
                val green = clamp((color.green - 128) * factor + 128)
                val blue = clamp((color.blue - 128) * factor + 128)
                
                val enhancedColor = Color(red, green, blue)
                dest.setRGB(x, y, enhancedColor.rgb)
            }
        }
    }
    
    /**
     * Task 58: Implement more efficient noise reduction algorithm
     */
    private fun efficientNoiseReduction(image: BufferedImage): BufferedImage {
        // Use optimized 3x3 median filter for noise reduction
        return medianFilter3x3(image)
    }
    
    /**
     * Optimized 3x3 median filter implementation
     */
    private fun medianFilter3x3(image: BufferedImage): BufferedImage {
        val width = image.width
        val height = image.height
        val filtered = BufferedImage(width, height, image.type)
        
        // Pre-allocate arrays for median calculation to avoid repeated allocation
        val redValues = IntArray(9)
        val greenValues = IntArray(9)
        val blueValues = IntArray(9)
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var index = 0
                
                // Collect neighboring pixel values
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val rgb = image.getRGB(x + dx, y + dy)
                        val color = Color(rgb)
                        redValues[index] = color.red
                        greenValues[index] = color.green
                        blueValues[index] = color.blue
                        index++
                    }
                }
                
                // Find median values
                redValues.sort()
                greenValues.sort()
                blueValues.sort()
                
                val medianColor = Color(redValues[4], greenValues[4], blueValues[4])
                filtered.setRGB(x, y, medianColor.rgb)
            }
        }
        
        // Copy border pixels unchanged
        copyBorderPixels(image, filtered)
        
        return filtered
    }
    
    /**
     * Copy border pixels from source to destination
     */
    private fun copyBorderPixels(source: BufferedImage, dest: BufferedImage) {
        val width = source.width
        val height = source.height
        
        // Top and bottom rows
        for (x in 0 until width) {
            dest.setRGB(x, 0, source.getRGB(x, 0))
            dest.setRGB(x, height - 1, source.getRGB(x, height - 1))
        }
        
        // Left and right columns
        for (y in 0 until height) {
            dest.setRGB(0, y, source.getRGB(0, y))
            dest.setRGB(width - 1, y, source.getRGB(width - 1, y))
        }
    }
    
    /**
     * Utility function to clamp values to 0-255 range
     */
    private fun clamp(value: Double): Int {
        return max(0, min(255, value.toInt()))
    }
    
    /**
     * Task 54: Memory-optimized batch processing
     */
    fun processBatch(images: List<BufferedImage>, options: ImageProcessingOptions = ImageProcessingOptions()): List<ProcessingResult> {
        val results = mutableListOf<ProcessingResult>()
        
        // Process in chunks to manage memory usage
        val chunkSize = calculateOptimalChunkSize(images, options)
        
        for (chunk in images.chunked(chunkSize)) {
            // Process chunk in parallel if beneficial
            if (chunk.size > 1 && PROCESSORS_COUNT > 1) {
                val chunkResults = chunk.parallelStream()
                    .map { image -> processImage(image, options) }
                    .toList()
                results.addAll(chunkResults)
            } else {
                chunk.forEach { image ->
                    results.add(processImage(image, options))
                }
            }
            
            // Force garbage collection between chunks to manage memory
            if (images.size > chunkSize) {
                System.gc()
            }
        }
        
        return results
    }
    
    /**
     * Calculate optimal chunk size based on available memory and image sizes
     */
    private fun calculateOptimalChunkSize(images: List<BufferedImage>, options: ImageProcessingOptions): Int {
        if (images.isEmpty()) return 1
        
        val runtime = Runtime.getRuntime()
        val availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
        
        // Estimate memory per image
        val sampleImage = images.first()
        val estimatedMemoryPerImage = estimateMemoryUsage(sampleImage, options)
        
        // Calculate safe chunk size (use only 50% of available memory for safety)
        val safeMemory = availableMemory / 2
        val chunkSize = max(1, (safeMemory / estimatedMemoryPerImage).toInt())
        
        return min(chunkSize, PROCESSORS_COUNT * 2) // Don't exceed reasonable parallelism
    }
    
    /**
     * Estimate memory usage for processing an image
     */
    private fun estimateMemoryUsage(image: BufferedImage, options: ImageProcessingOptions): Long {
        var estimatedSize = (image.width * image.height * 4).toLong() // Original image
        
        // Account for intermediate images in processing pipeline
        if (options.resizeIfLarge) {
            estimatedSize += (options.maxDimension * options.maxDimension * 4).toLong()
        }
        
        // Add overhead for processing operations
        estimatedSize *= 2 // Double for safety margin
        
        return estimatedSize
    }
}

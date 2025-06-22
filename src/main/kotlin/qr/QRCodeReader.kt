package qr

import barcode.UnifiedBarcodeReader
import barcode.BarcodeFormat
import barcode.BarcodeNotFoundException
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.GlobalHistogramBinarizer
import utils.ImageProcessor
import utils.PerformanceProfiler
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * QRCodeReader class with optional plugin system integration, optimized image processing, encryption support, and performance profiling.
 * 
 * This class maintains backward compatibility while providing optional
 * plugin system integration for enhanced barcode reading capabilities.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 * Task 51: Optimize the image processing pipeline for better performance
 * Task 56: Profile the application to identify performance bottlenecks
 * Task 73: Add support for encrypted QR codes
 * Task 77: Add support for reading damaged or partially visible QR codes
 */
class QRCodeReader {
    private val reader = MultiFormatReader()
    private var unifiedReader: UnifiedBarcodeReader? = null
    private val imageProcessor = ImageProcessor()
    private val encryption = QRCodeEncryption()
    
    // Flag to enable/disable plugin system (for gradual migration)
    private val usePluginSystem = false // Disabled by default to avoid circular dependency
    
    // Image processing configuration
    private var imageProcessingOptions = ImageProcessor.ImageProcessingOptions()
    
    // Enhanced reader for damaged QR codes
    private val enhancedReader = EnhancedQRReader()
    
    // vCard and WiFi processors
    val vCardProcessor = VCardProcessor
    val wifiProcessor = WiFiProcessor
    
    // Batch exporter
    val batchExporter = BatchExporter()

    /**
     * Configure image processing options for optimal QR code detection
     */
    fun configureImageProcessing(options: ImageProcessor.ImageProcessingOptions) {
        this.imageProcessingOptions = options
    }
    
    /**
     * Initialize plugin system support (optional)
     * Only call this if you want to use the plugin system
     */
    fun enablePluginSystem() {
        if (unifiedReader == null) {
            unifiedReader = UnifiedBarcodeReader()
        }
    }
    
    fun readQRCode(imagePath: String): String {
        if (usePluginSystem && unifiedReader != null) {
            return try {
                unifiedReader!!.readQRCode(imagePath)
            } catch (e: Exception) {
                // Fallback to legacy implementation
                readQRCodeLegacy(imagePath)
            }
        } else {
            return readQRCodeLegacy(imagePath)
        }
    }
      fun readQRCode(image: BufferedImage): String {
        if (usePluginSystem && unifiedReader != null) {
            return try {
                unifiedReader!!.readQRCode(image)
            } catch (e: Exception) {
                // Fallback to legacy implementation
                readQRCodeLegacy(image)
            }
        } else {
            return readQRCodeLegacy(image)
        }
    }
      /**
     * Legacy QR code reading implementation with optimized image processing and performance profiling
     * Kept for backward compatibility and as a fallback
     */
    private fun readQRCodeLegacy(imagePath: String): String {
        return PerformanceProfiler.measureOperation("readQRCodeFromFile") {
            val file = File(imagePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Fichier non trouvé: $imagePath")
            }
            
            val image = PerformanceProfiler.measureOperation("imageLoad") {
                ImageIO.read(file) ?: throw IllegalArgumentException("Image invalide")
            }
            readQRCodeLegacy(image)
        }
    }
      /**
     * Legacy QR code reading implementation for BufferedImage with image processing optimization and performance profiling
     */
    private fun readQRCodeLegacy(image: BufferedImage): String {
        return PerformanceProfiler.measureOperation("readQRCodeFromBuffer") {
            // Task 51: Apply optimized image processing pipeline with profiling
            val processingResult = PerformanceProfiler.measureOperation("imageProcessing") {
                imageProcessor.processImage(image, imageProcessingOptions)
            }
            val optimizedImage = processingResult.processedImage
            
            val source = BufferedImageLuminanceSource(optimizedImage)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            return try {
                val result = reader.decode(bitmap)
                result.text
            } catch (e: NotFoundException) {
                // Try with original image if processed image fails
                try {
                    val originalSource = BufferedImageLuminanceSource(image)
                    val originalBitmap = BinaryBitmap(HybridBinarizer(originalSource))
                    val originalResult = reader.decode(originalBitmap)
                    originalResult.text
                } catch (e2: NotFoundException) {
                    // Try with different binarizer
                    try {
                        val globalSource = BufferedImageLuminanceSource(optimizedImage)
                        val globalBitmap = BinaryBitmap(GlobalHistogramBinarizer(globalSource))
                        val globalResult = reader.decode(globalBitmap)
                        globalResult.text
                    } catch (e3: NotFoundException) {
                        throw RuntimeException("Aucun QR code trouvé")
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Erreur lecture QR: ${e.message}")
            }
        }
    }
      /**
     * Enhanced reading method that returns detailed results including image processing metrics
     * Uses the plugin system to provide additional metadata
     */
    fun readQRCodeDetailed(imagePath: String): Map<String, Any> {        return try {
            val result = unifiedReader?.readBarcodeDetailed(imagePath)
            if (result != null) {
                mapOf(
                    "content" to result.content,
                    "format" to result.format.displayName,
                    "confidence" to result.confidence,
                    "processingTimeMs" to result.processingTimeMs,
                    "metadata" to result.metadata,
                    "success" to true
                )
            } else {
                mapOf(
                    "content" to "",
                    "error" to "Plugin system not available",
                    "success" to false
                )
            }
        } catch (e: BarcodeNotFoundException) {
            mapOf(
                "content" to "",
                "error" to "Aucun QR code trouvé",
                "success" to false
            )
        } catch (e: Exception) {
            mapOf(
                "content" to "",
                "error" to "Erreur lecture QR: ${e.message}",
                "success" to false
            )
        }
    }
    
    /**
     * Enhanced QR code reading with detailed processing results
     * Task 51: Provides comprehensive image processing metrics
     */    fun readQRCodeWithProcessingDetails(imagePath: String): Map<String, Any> {
        return try {
            val file = File(imagePath)
            if (!file.exists()) {
                throw IllegalArgumentException("Fichier non trouvé: $imagePath")
            }
            val originalImage = ImageIO.read(file) ?: throw IllegalArgumentException("Image invalide")
            readQRCodeWithProcessingDetails(originalImage)        } catch (e: Exception) {
            val errorResult: Map<String, Any> = hashMapOf(
                "content" to "",
                "error" to (e.message ?: "Erreur inconnue"),
                "success" to false
            )
            return errorResult
        }
    }
    
    /**
     * Enhanced QR code reading with detailed processing results for BufferedImage
     */
    fun readQRCodeWithProcessingDetails(image: BufferedImage): Map<String, Any> {
        return try {
            val startTime = System.currentTimeMillis()
            
            // Apply image processing with detailed results
            val processingResult = imageProcessor.processImage(image, imageProcessingOptions)
            val optimizedImage = processingResult.processedImage
            
            val source = BufferedImageLuminanceSource(optimizedImage)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val result = reader.decode(bitmap)
            val totalTime = System.currentTimeMillis() - startTime
            
            mapOf(
                "content" to result.text,
                "success" to true,
                "processingDetails" to mapOf(                    "originalSize" to mapOf(
                        "width" to processingResult.originalSize.first as Any,
                        "height" to processingResult.originalSize.second as Any
                    ),
                    "processedSize" to mapOf(
                        "width" to processingResult.processedSize.first as Any,
                        "height" to processingResult.processedSize.second as Any
                    ),
                    "imageProcessingTimeMs" to processingResult.processingTimeMs,
                    "totalProcessingTimeMs" to totalTime,
                    "memoryUsedBytes" to processingResult.memoryUsedBytes,
                    "operationsApplied" to processingResult.operationsApplied
                ),
                "qrCodeDetails" to mapOf(
                    "format" to result.barcodeFormat.toString(),
                    "resultPoints" to result.resultPoints?.map { point ->
                        mapOf("x" to point.x, "y" to point.y)
                    }
                )
            )
        } catch (e: NotFoundException) {
            mapOf(
                "content" to "",
                "error" to "Aucun QR code trouvé",
                "success" to false
            )
        } catch (e: Exception) {
            mapOf(
                "content" to "",
                "error" to "Erreur lecture QR: ${e.message}",
                "success" to false
            )
        }
    }
    
    /**
     * Check what barcode formats are supported
     */    fun getSupportedFormats(): Set<String> {
        return unifiedReader?.getSupportedFormats()?.map { it.displayName }?.toSet() ?: emptySet()
    }
      /**
     * Get plugin information
     */    fun getPluginInfo(): Map<String, Any> {
        return unifiedReader?.getPluginInfo() ?: emptyMap()
    }
    
    /**
     * Batch process multiple images with optimized performance
     * Task 51: Efficient batch processing with memory management
     */    fun readQRCodesBatch(imagePaths: List<String>): List<Map<String, Any>> {
        val results = mutableListOf<Map<String, Any>>()
        val successfulImages = mutableListOf<BufferedImage>()
        val imageIndexMap = mutableMapOf<Int, Int>() // original index to processed index
        
        try {
            // Load images first
            imagePaths.forEachIndexed { index, path ->
                try {
                    val file = File(path)
                    if (!file.exists()) {
                        throw IllegalArgumentException("Fichier non trouvé: $path")
                    }
                    val image = ImageIO.read(file) ?: throw IllegalArgumentException("Image invalide: $path")
                    
                    val processedIndex = successfulImages.size
                    successfulImages.add(image)
                    imageIndexMap[processedIndex] = index                } catch (e: Exception) {
                    val errorResult: Map<String, Any> = hashMapOf(
                        "index" to index,
                        "imagePath" to path,
                        "content" to "",
                        "error" to (e.message ?: "Erreur inconnue"),
                        "success" to false
                    )
                    results.add(errorResult)}
            }
            
            // Process images in batches for optimal memory usage
            val processingResults = imageProcessor.processBatch(
                successfulImages, 
                imageProcessingOptions
            )
            
            // Decode QR codes from processed images
            processingResults.forEachIndexed { processedIndex, processingResult ->
                val originalIndex = imageIndexMap[processedIndex] ?: processedIndex
                val imagePath = imagePaths[originalIndex]
                
                try {
                    val source = BufferedImageLuminanceSource(processingResult.processedImage)
                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                    val result = reader.decode(bitmap)
                    
                    results.add(mapOf(
                        "index" to originalIndex,
                        "imagePath" to imagePath,
                        "content" to result.text,
                        "success" to true,
                        "processingDetails" to mapOf(
                            "imageProcessingTimeMs" to processingResult.processingTimeMs,
                            "memoryUsedBytes" to processingResult.memoryUsedBytes,
                            "operationsApplied" to processingResult.operationsApplied
                        )
                    ))                } catch (e: NotFoundException) {
                    results.add(mapOf(
                        "index" to originalIndex,
                        "imagePath" to imagePath,
                        "content" to "",
                        "error" to "Aucun QR code trouvé",
                        "success" to false
                    ))
                } catch (e: Exception) {
                    results.add(mapOf(
                        "index" to originalIndex,
                        "imagePath" to imagePath,
                        "content" to "",
                        "error" to "Erreur lecture QR: ${e.message}",
                        "success" to false
                    ))
                }
            }
        } catch (e: Exception) {
            // Handle any batch-level errors
            imagePaths.forEachIndexed { index, path ->
                if (results.none { (it["index"] as Int) == index }) {
                    results.add(mapOf(
                        "index" to index,
                        "imagePath" to path,
                        "content" to "",
                        "error" to "Erreur batch: ${e.message}",
                        "success" to false
                    ))
                }
            }
        }
        
        return results.sortedBy { it["index"] as Int }
    }
      /**
     * Data class for enhanced QR code reading results with encryption support and URL detection
     */
    data class QRReadResult(
        val content: String,
        val isEncrypted: Boolean = false,
        val decryptionAttempted: Boolean = false,
        val decryptionSuccessful: Boolean = false,
        val rawContent: String? = null,
        val metadata: Map<String, Any> = emptyMap(),
        val isUrl: Boolean = false,
        val urlType: String? = null // "http", "https", "mailto", "tel", etc.
    )
    
    /**
     * Read QR code with automatic encryption detection and optional decryption
     * 
     * @param imagePath Path to the QR code image
     * @param decryptionKey Optional encryption key for decrypting encrypted QR codes
     * @param password Optional password for password-based decryption
     * @return Enhanced QR reading result with encryption information
     */
    fun readQRCodeEnhanced(
        imagePath: String,
        decryptionKey: javax.crypto.SecretKey? = null,
        password: String? = null
    ): QRReadResult {        return try {
            val rawContent = readQRCode(imagePath)
            processQRContent(rawContent, decryptionKey, password)
        } catch (e: Exception) {
            QRReadResult(
                content = "Error reading QR code: ${e.message}",
                metadata = mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
    
    /**
     * Read QR code from BufferedImage with automatic encryption detection and optional decryption
     * 
     * @param image BufferedImage containing the QR code
     * @param decryptionKey Optional encryption key for decrypting encrypted QR codes
     * @param password Optional password for password-based decryption
     * @return Enhanced QR reading result with encryption information
     */
    fun readQRCodeEnhanced(
        image: BufferedImage,
        decryptionKey: javax.crypto.SecretKey? = null,
        password: String? = null
    ): QRReadResult {        return try {
            val rawContent = readQRCode(image)
            processQRContent(rawContent, decryptionKey, password)
        } catch (e: Exception) {
            QRReadResult(
                content = "Error reading QR code: ${e.message}",
                metadata = mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
    
    /**
     * Process QR content, handling encryption detection and decryption
     */
    private fun processQRContent(
        rawContent: String,
        decryptionKey: javax.crypto.SecretKey?,
        password: String?
    ): QRReadResult {
        val isEncrypted = encryption.isEncrypted(rawContent)
          if (!isEncrypted) {
            val (isUrl, urlType) = detectUrl(rawContent)
            return QRReadResult(
                content = rawContent,
                isEncrypted = false,
                rawContent = rawContent,
                isUrl = isUrl,
                urlType = urlType,                metadata = mapOf(
                    "contentLength" to rawContent.length,
                    "readTime" to System.currentTimeMillis(),
                    "isUrl" to isUrl,
                    "urlType" to (urlType ?: "none")
                )
            )
        }
        
        // Content is encrypted, attempt decryption
        var decryptionResult: QRCodeEncryption.DecryptionResult? = null
        
        // Try decryption with provided key first
        if (decryptionKey != null) {
            decryptionResult = encryption.decrypt(rawContent, decryptionKey)
        }
        
        // If key decryption failed or no key provided, try password decryption
        if ((decryptionResult == null || !decryptionResult.success) && password != null) {
            decryptionResult = encryption.decryptWithPassword(rawContent, password)
        }
        
        val metadata = mutableMapOf<String, Any>(
            "isEncrypted" to true,
            "rawContentLength" to rawContent.length,
            "readTime" to System.currentTimeMillis()
        )
          return if (decryptionResult != null && decryptionResult.success) {
            metadata.putAll(decryptionResult.metadata)
            metadata["decryptionTime"] = System.currentTimeMillis()
            
            val decryptedContent = decryptionResult.decryptedContent!!
            val (isUrl, urlType) = detectUrl(decryptedContent)
            
            metadata["isUrl"] = isUrl
            metadata["urlType"] = urlType ?: "none"
            
            QRReadResult(
                content = decryptedContent,
                isEncrypted = true,
                decryptionAttempted = true,
                decryptionSuccessful = true,
                rawContent = rawContent,
                isUrl = isUrl,
                urlType = urlType,
                metadata = metadata
            )
        } else {
            metadata["decryptionError"] = decryptionResult?.error ?: "No decryption key or password provided"
            
            QRReadResult(
                content = "ENCRYPTED: Unable to decrypt content. ${decryptionResult?.error ?: "Provide decryption key or password."}",
                isEncrypted = true,
                decryptionAttempted = decryptionKey != null || password != null,
                decryptionSuccessful = false,
                rawContent = rawContent,
                metadata = metadata
            )
        }
    }
    
    /**
     * Check if QR code content is encrypted
     */
    fun isQRCodeEncrypted(imagePath: String): Boolean {
        return try {
            val content = readQRCode(imagePath)
            encryption.isEncrypted(content)
        } catch (e: Exception) {
            false
        }
    }
      /**
     * Check if QR code content is encrypted from BufferedImage
     */
    fun isQRCodeEncrypted(image: BufferedImage): Boolean {
        return try {
            val content = readQRCode(image)
            encryption.isEncrypted(content)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Detect if content is a URL and determine its type
     * 
     * @param content The content to analyze
     * @return Pair of (isUrl, urlType) where urlType can be "http", "https", "mailto", "tel", "sms", "geo", etc.
     */
    fun detectUrl(content: String): Pair<Boolean, String?> {
        val trimmedContent = content.trim()
        
        return when {
            trimmedContent.startsWith("http://", ignoreCase = true) -> true to "http"
            trimmedContent.startsWith("https://", ignoreCase = true) -> true to "https"
            trimmedContent.startsWith("mailto:", ignoreCase = true) -> true to "mailto"
            trimmedContent.startsWith("tel:", ignoreCase = true) -> true to "tel"
            trimmedContent.startsWith("sms:", ignoreCase = true) || 
            trimmedContent.startsWith("smsto:", ignoreCase = true) -> true to "sms"
            trimmedContent.startsWith("geo:", ignoreCase = true) -> true to "geo"
            trimmedContent.startsWith("wifi:", ignoreCase = true) -> true to "wifi"
            trimmedContent.startsWith("ftp://", ignoreCase = true) -> true to "ftp"
            trimmedContent.startsWith("ftps://", ignoreCase = true) -> true to "ftps"
            // Check for URLs without protocol
            isValidUrlWithoutProtocol(trimmedContent) -> true to "http"
            else -> false to null
        }
    }
    
    /**
     * Check if content looks like a URL without protocol (e.g., "www.example.com" or "example.com")
     */
    private fun isValidUrlWithoutProtocol(content: String): Boolean {
        val urlPattern = Regex(
            "^(www\\.)?[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.[a-zA-Z]{2,}(\\.[a-zA-Z]{2,})*(/.*)?$"
        )
        return urlPattern.matches(content)
    }
    
    /**
     * Open URL in the default system browser or application
     * 
     * @param url The URL to open
     * @param urlType The type of URL (http, https, mailto, etc.)
     * @return true if the URL was successfully opened, false otherwise
     */
    fun openUrl(url: String, urlType: String? = null): Boolean {
        return try {
            val desktop = java.awt.Desktop.getDesktop()
            
            if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                return false
            }
            
            val finalUrl = when {
                urlType == "http" && !url.startsWith("http://", ignoreCase = true) && 
                !url.startsWith("https://", ignoreCase = true) -> "http://$url"
                else -> url
            }
            
            val uri = java.net.URI(finalUrl)
            desktop.browse(uri)
            true
        } catch (e: Exception) {
            println("Failed to open URL: ${e.message}")
            false
        }
    }
    
    /**
     * Read QR code with damage recovery support
     * Task 77: Add support for reading damaged or partially visible QR codes
     */
    fun readQRCodeWithRecovery(image: BufferedImage): EnhancedQRReader.RecoveryResult {
        return enhancedReader.readDamagedQRCode(image)
    }
    
    /**
     * Assess QR code damage level
     */
    fun assessQRCodeDamage(image: BufferedImage): Map<String, Any> {
        return enhancedReader.assessQRCodeDamage(image)
    }
    
    /**
     * Enhanced QR code reading with all advanced features
     */
    fun readQRCodeAdvanced(
        imagePath: String,
        useRecovery: Boolean = false,
        decryptionKey: javax.crypto.SecretKey? = null,
        password: String? = null
    ): QRReadResult {
        return PerformanceProfiler.measureOperation("readQRCodeAdvanced") {
            try {
                val file = File(imagePath)
                if (!file.exists()) {
                    throw IllegalArgumentException("File not found: $imagePath")
                }
                
                val image = ImageIO.read(file)
                readQRCodeAdvanced(image, useRecovery, decryptionKey, password)
            } catch (e: Exception) {
                QRReadResult(
                    content = "Error reading QR code: ${e.message}",
                    metadata = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
    
    /**
     * Enhanced QR code reading with all advanced features from BufferedImage
     */
    fun readQRCodeAdvanced(
        image: BufferedImage,
        useRecovery: Boolean = false,
        decryptionKey: javax.crypto.SecretKey? = null,
        password: String? = null
    ): QRReadResult {
        return PerformanceProfiler.measureOperation("readQRCodeAdvancedImage") {
            // First try normal reading
            try {
                val normalResult = readQRCodeEnhanced(image, decryptionKey, password)
                if (normalResult.content.isNotEmpty() && !normalResult.content.startsWith("Error")) {
                    return@measureOperation normalResult
                }
            } catch (e: Exception) {
                // Continue to recovery if normal reading fails
            }
            
            // If normal reading failed and recovery is enabled, try enhanced recovery
            if (useRecovery) {
                val recoveryResult = enhancedReader.readDamagedQRCode(image)
                if (recoveryResult.content != null) {
                    val processedResult = processQRContent(recoveryResult.content, decryptionKey, password)
                    return@measureOperation processedResult.copy(
                        metadata = processedResult.metadata + mapOf(
                            "recoveryUsed" to true,
                            "recoveryMethod" to recoveryResult.method,
                            "recoveryConfidence" to recoveryResult.confidence,
                            "recoveryAttempts" to recoveryResult.attemptsUsed,
                            "recoveryTimeMs" to recoveryResult.processingTimeMs
                        )
                    )
                }
            }
            
            // All methods failed
            QRReadResult(
                content = "Failed to read QR code",
                metadata = mapOf(
                    "error" to "All reading methods failed",
                    "recoveryAttempted" to useRecovery,
                    "damageAssessment" to if (useRecovery) enhancedReader.assessQRCodeDamage(image) else emptyMap<String, Any>()
                )
            )
        }
    }
}

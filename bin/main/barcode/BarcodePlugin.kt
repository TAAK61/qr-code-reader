package barcode

import java.awt.image.BufferedImage

/**
 * Plugin interface for barcode readers.
 * 
 * This interface defines the contract that all barcode reading plugins must implement.
 * Each plugin is responsible for detecting and decoding a specific type of barcode format.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
interface BarcodePlugin {
    
    /**
     * The name of this plugin (e.g., "QR Code", "Data Matrix", "Code 128")
     */
    val name: String
    
    /**
     * The version of this plugin
     */
    val version: String
    
    /**
     * List of barcode formats supported by this plugin
     */
    val supportedFormats: List<BarcodeFormat>
    
    /**
     * Priority of this plugin (higher values = higher priority)
     * Used when multiple plugins can handle the same format
     */
    val priority: Int
    
    /**
     * Check if this plugin can handle the given image
     * This method should be fast and not perform full decoding
     * 
     * @param image The image to check
     * @return true if this plugin can potentially decode this image
     */
    fun canHandle(image: BufferedImage): Boolean
    
    /**
     * Decode a barcode from the given image
     * 
     * @param image The image containing the barcode
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no supported barcode is found
     * @throws BarcodeDecodingException if decoding fails
     */
    fun decode(image: BufferedImage): BarcodeResult
    
    /**
     * Decode a barcode from the given image file path
     * 
     * @param imagePath Path to the image file
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no supported barcode is found
     * @throws BarcodeDecodingException if decoding fails
     */
    fun decode(imagePath: String): BarcodeResult
    
    /**
     * Get plugin configuration information
     */
    fun getConfiguration(): Map<String, Any>
    
    /**
     * Initialize the plugin with optional configuration
     * 
     * @param config Optional configuration parameters
     */
    fun initialize(config: Map<String, Any> = emptyMap())
    
    /**
     * Clean up plugin resources
     */
    fun shutdown()
}

/**
 * Enumeration of supported barcode formats
 */
enum class BarcodeFormat(val displayName: String, val description: String) {
    QR_CODE("QR Code", "Quick Response Code - 2D matrix barcode"),
    DATA_MATRIX("Data Matrix", "2D matrix barcode using square modules"),
    CODE_128("Code 128", "High-density linear barcode"),
    CODE_39("Code 39", "Alphanumeric linear barcode"),
    EAN_13("EAN-13", "European Article Number 13-digit barcode"),
    EAN_8("EAN-8", "European Article Number 8-digit barcode"),
    UPC_A("UPC-A", "Universal Product Code - 12 digits"),
    UPC_E("UPC-E", "Universal Product Code - 8 digits"),
    PDF_417("PDF417", "Portable Data File 417 - 2D stacked barcode"),
    AZTEC("Aztec", "2D matrix barcode using concentric squares"),
    CODABAR("Codabar", "Linear barcode used in libraries and medical applications"),
    ITF("ITF", "Interleaved 2 of 5 barcode"),
    RSS_14("RSS-14", "Reduced Space Symbology 14-digit barcode"),
    RSS_EXPANDED("RSS Expanded", "Reduced Space Symbology with variable length"),
    MAXICODE("MaxiCode", "2D barcode used by UPS");
    
    companion object {
        fun fromString(name: String): BarcodeFormat? {
            return values().find { it.name.equals(name, ignoreCase = true) }
        }
    }
}

/**
 * Result of a successful barcode decode operation
 */
data class BarcodeResult(
    val content: String,
    val format: BarcodeFormat,
    val confidence: Double = 1.0,
    val metadata: Map<String, Any> = emptyMap(),
    val rawBytes: ByteArray? = null,
    val processingTimeMs: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BarcodeResult

        if (content != other.content) return false
        if (format != other.format) return false
        if (confidence != other.confidence) return false
        if (metadata != other.metadata) return false
        if (rawBytes != null) {
            if (other.rawBytes == null) return false
            if (!rawBytes.contentEquals(other.rawBytes)) return false
        } else if (other.rawBytes != null) return false
        if (processingTimeMs != other.processingTimeMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
        result = 31 * result + processingTimeMs.hashCode()
        return result
    }
}

/**
 * Exception thrown when no barcode is found in an image
 */
class BarcodeNotFoundException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when barcode decoding fails
 */
class BarcodeDecodingException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when plugin loading fails
 */
class PluginLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

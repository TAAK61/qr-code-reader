package barcode

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * QR Code plugin implementation using ZXing library directly.
 * 
 * This plugin provides QR code reading functionality through the plugin interface
 * without circular dependencies on other reader classes.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class QRCodePlugin : BarcodePlugin {
    
    override val name: String = "QR Code Reader"
    override val version: String = "1.0.0"
    override val supportedFormats: List<BarcodeFormat> = listOf(BarcodeFormat.QR_CODE)
    override val priority: Int = 100 // High priority for QR codes
    
    private val multiFormatReader = MultiFormatReader()
    private var isInitialized = false
    
    override fun canHandle(image: BufferedImage): Boolean {
        if (!isInitialized) return false
        
        // Quick heuristic check for QR code patterns
        // Look for finder patterns (squares in corners)
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            // Try a quick detection without full decoding
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE)
            )
            
            multiFormatReader.decode(bitmap, hints)
            true
        } catch (e: Exception) {
            false
        }
    }
      override fun decode(image: BufferedImage): BarcodeResult {
        if (!isInitialized) {
            throw BarcodeDecodingException("Plugin not initialized")
        }

        val startTime = System.currentTimeMillis()

        try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            
            val result = multiFormatReader.decode(bitmap, hints)
            val processingTime = System.currentTimeMillis() - startTime

            return BarcodeResult(
                content = result.text,
                format = BarcodeFormat.QR_CODE,
                confidence = 1.0,
                metadata = mapOf(
                    "plugin" to name,
                    "version" to version,
                    "imageWidth" to image.width,
                    "imageHeight" to image.height,
                    "rawDataLength" to (result.rawBytes?.size ?: 0)
                ),
                rawBytes = result.rawBytes,
                processingTimeMs = processingTime
            )
        } catch (e: NotFoundException) {
            throw BarcodeNotFoundException("No QR code found in image", e)
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode QR code: ${e.message}", e)
        }
    }
    
    override fun decode(imagePath: String): BarcodeResult {
        if (!isInitialized) {
            throw BarcodeDecodingException("Plugin not initialized")
        }
        
        val file = File(imagePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $imagePath")
        }
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Invalid image file: $imagePath")
        return decode(image)
    }
    
    override fun getConfiguration(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "version" to version,
            "supportedFormats" to supportedFormats.map { it.displayName },
            "priority" to priority,
            "initialized" to isInitialized,
            "library" to "ZXing",
            "capabilities" to listOf(
                "Fast QR code detection",
                "High accuracy decoding",
                "Support for damaged QR codes",
                "Multiple encoding support"
            )
        )
    }
    
    override fun initialize(config: Map<String, Any>) {
        try {
            // Configure the MultiFormatReader for QR codes only
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            multiFormatReader.setHints(hints)
            
            isInitialized = true
            println("QR Code plugin initialized successfully")
        } catch (e: Exception) {
            throw PluginLoadException("Failed to initialize QR Code plugin: ${e.message}", e)
        }
    }
    
    override fun shutdown() {
        try {
            isInitialized = false
            // Clean up any resources if needed
            println("QR Code plugin shutdown successfully")
        } catch (e: Exception) {
            println("Warning: Error during QR Code plugin shutdown: ${e.message}")
        }
    }
    
    /**
     * Enhanced QR code detection with additional options
     */
    fun decodeWithOptions(image: BufferedImage, tryHarder: Boolean = true, characterSet: String = "UTF-8"): BarcodeResult {
        if (!isInitialized) {
            throw BarcodeDecodingException("Plugin not initialized")
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val hints = mutableMapOf<DecodeHintType, Any>(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                DecodeHintType.CHARACTER_SET to characterSet
            )
            
            if (tryHarder) {
                hints[DecodeHintType.TRY_HARDER] = true
            }
            
            val result = multiFormatReader.decode(bitmap, hints)
            val processingTime = System.currentTimeMillis() - startTime
            
            return BarcodeResult(
                content = result.text,
                format = BarcodeFormat.QR_CODE,
                confidence = 1.0,
                metadata = mapOf(
                    "plugin" to name,
                    "version" to version,
                    "imageWidth" to image.width,
                    "imageHeight" to image.height,
                    "tryHarder" to tryHarder,
                    "characterSet" to characterSet,
                    "rawDataLength" to (result.rawBytes?.size ?: 0)
                ),
                rawBytes = result.rawBytes,
                processingTimeMs = processingTime
            )
        } catch (e: NotFoundException) {
            throw BarcodeNotFoundException("No QR code found in image", e)
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode QR code: ${e.message}", e)
        }
    }
}

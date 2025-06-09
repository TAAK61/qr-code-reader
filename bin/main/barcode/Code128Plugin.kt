package barcode

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Code 128 plugin implementation using ZXing library.
 * 
 * This plugin provides support for reading Code 128 linear barcodes,
 * which are widely used in shipping and packaging applications.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class Code128Plugin : BarcodePlugin {
    
    override val name: String = "Code 128 Reader"
    override val version: String = "1.0.0"
    override val supportedFormats: List<BarcodeFormat> = listOf(BarcodeFormat.CODE_128)
    override val priority: Int = 70 // Medium priority
    
    private val multiFormatReader = MultiFormatReader()
    private var isInitialized = false
    
    override fun canHandle(image: BufferedImage): Boolean {
        if (!isInitialized) return false
        
        // Quick heuristic check for Code 128 patterns
        // Code 128 is a linear barcode, so we look for horizontal patterns
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.CODE_128),
                DecodeHintType.TRY_HARDER to false
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
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.CODE_128),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            
            val result = multiFormatReader.decode(bitmap, hints)
            val processingTime = System.currentTimeMillis() - startTime
            
            return BarcodeResult(
                content = result.text,
                format = BarcodeFormat.CODE_128,
                confidence = 1.0,
                metadata = mapOf(
                    "plugin" to name,
                    "version" to version,
                    "imageWidth" to image.width,
                    "imageHeight" to image.height,
                    "rawDataLength" to (result.rawBytes?.size ?: 0),
                    "barcodeType" to "linear"
                ),
                rawBytes = result.rawBytes,
                processingTimeMs = processingTime
            )
        } catch (e: NotFoundException) {
            throw BarcodeNotFoundException("No Code 128 barcode found in image", e)
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode Code 128: ${e.message}", e)
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
                "Linear barcode reading",
                "High density encoding",
                "Alphanumeric and ASCII support",
                "Shipping and logistics applications"
            )
        )
    }
    
    override fun initialize(config: Map<String, Any>) {
        try {
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.CODE_128),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            multiFormatReader.setHints(hints)
            
            isInitialized = true
            println("Code 128 plugin initialized successfully")
        } catch (e: Exception) {
            throw PluginLoadException("Failed to initialize Code 128 plugin: ${e.message}", e)
        }
    }
    
    override fun shutdown() {
        try {
            isInitialized = false
            println("Code 128 plugin shutdown successfully")
        } catch (e: Exception) {
            println("Warning: Error during Code 128 plugin shutdown: ${e.message}")
        }
    }
}

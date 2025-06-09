package barcode

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Data Matrix plugin implementation using ZXing library.
 * 
 * This plugin provides support for reading Data Matrix 2D barcodes,
 * which are commonly used in manufacturing and logistics.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class DataMatrixPlugin : BarcodePlugin {
    
    override val name: String = "Data Matrix Reader"
    override val version: String = "1.0.0"
    override val supportedFormats: List<BarcodeFormat> = listOf(BarcodeFormat.DATA_MATRIX)
    override val priority: Int = 80 // Medium-high priority
    
    private val multiFormatReader = MultiFormatReader()
    private var isInitialized = false
    
    override fun canHandle(image: BufferedImage): Boolean {
        if (!isInitialized) return false
        
        // Quick heuristic check for Data Matrix patterns
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.DATA_MATRIX),
                DecodeHintType.TRY_HARDER to false // Quick check only
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
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.DATA_MATRIX),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            
            val result = multiFormatReader.decode(bitmap, hints)
            val processingTime = System.currentTimeMillis() - startTime
            
            return BarcodeResult(
                content = result.text,
                format = BarcodeFormat.DATA_MATRIX,
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
            throw BarcodeNotFoundException("No Data Matrix code found in image", e)
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode Data Matrix: ${e.message}", e)
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
                "2D matrix barcode reading",
                "High density data storage",
                "Error correction capabilities",
                "Industrial applications support"
            )
        )
    }
    
    override fun initialize(config: Map<String, Any>) {
        try {
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.DATA_MATRIX),
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.CHARACTER_SET to "UTF-8"
            )
            multiFormatReader.setHints(hints)
            
            isInitialized = true
            println("Data Matrix plugin initialized successfully")
        } catch (e: Exception) {
            throw PluginLoadException("Failed to initialize Data Matrix plugin: ${e.message}", e)
        }
    }
    
    override fun shutdown() {
        try {
            isInitialized = false
            println("Data Matrix plugin shutdown successfully")
        } catch (e: Exception) {
            println("Warning: Error during Data Matrix plugin shutdown: ${e.message}")
        }
    }
}

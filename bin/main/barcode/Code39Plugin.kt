package barcode

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage

/**
 * Plugin for reading Code 39 barcodes
 * Task 71: Add support for additional barcode formats
 */
class Code39Plugin : BarcodePlugin {
    
    override val name = "Code 39"
    override val version = "1.0.0"
    override val supportedFormats = listOf(BarcodeFormat.CODE_39)
    override val priority = 100
    
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.CODE_39)
        ))
    }
    
    override fun canHandle(image: BufferedImage): Boolean {
        return try {
            // Quick check - Code 39 typically has specific aspect ratios
            val aspectRatio = image.width.toDouble() / image.height
            aspectRatio > 2.0 && aspectRatio < 10.0
        } catch (e: Exception) {
            false
        }
    }
      override fun decode(image: BufferedImage): BarcodeResult {
        return try {
            val source = BufferedImageLuminanceSource(image)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            val result = reader.decode(bitmap)
            
            BarcodeResult(
                content = result.text,
                format = BarcodeFormat.CODE_39,
                confidence = 0.95,
                metadata = mapOf(
                    "checksum" to (result.resultMetadata?.get(ResultMetadataType.SUGGESTED_PRICE) ?: ""),
                    "orientation" to (result.resultMetadata?.get(ResultMetadataType.ORIENTATION) ?: 0),
                    "errorCorrectionLevel" to "None"
                )
            )
        } catch (e: NotFoundException) {
            throw BarcodeNotFoundException("No Code 39 barcode found in image")
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode Code 39 barcode: ${e.message}")
        }
    }
    
    override fun decode(imagePath: String): BarcodeResult {
        val image = javax.imageio.ImageIO.read(java.io.File(imagePath)) 
            ?: throw BarcodeDecodingException("Could not read image file: $imagePath")
        return decode(image)
    }
    
    private fun getSupportedHints(): Map<DecodeHintType, Any> {
        return mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.CODE_39),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.PURE_BARCODE to false
        )
    }
    
    private fun validateInput(image: BufferedImage): Boolean {
        return image.width > 50 && image.height > 20
    }
      override fun getConfiguration(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "version" to version,
            "supportedFormats" to supportedFormats.map { it.name },
            "priority" to priority,
            "features" to listOf(
                "Alphanumeric encoding",
                "Self-checking capability", 
                "Variable length encoding",
                "Start/stop characters"
            )
        )
    }
    
    override fun initialize(config: Map<String, Any>) {
        // Initialize with configuration if needed
    }
    
    override fun shutdown() {
        // Clean up resources if needed
    }
}

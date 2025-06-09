package barcode

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage

/**
 * Plugin for reading UPC-A barcodes
 * Task 71: Add support for additional barcode formats
 */
class UPCAPlugin : BarcodePlugin {
    
    override val name = "UPC-A"
    override val version = "1.0.0"
    override val supportedFormats = listOf(BarcodeFormat.UPC_A)
    override val priority = 100
    
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.UPC_A)
        ))
    }
    
    override fun canHandle(image: BufferedImage): Boolean {
        return try {
            // UPC-A has specific dimensions and aspect ratio
            val aspectRatio = image.width.toDouble() / image.height
            aspectRatio > 2.5 && aspectRatio < 4.0
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
                format = BarcodeFormat.UPC_A,
                confidence = 0.98,
                metadata = mapOf(
                    "checkDigit" to (result.text.lastOrNull()?.toString() ?: ""),
                    "productCode" to result.text.take(11),
                    "countryCode" to result.text.take(1),
                    "manufacturerCode" to result.text.substring(1, 6),
                    "productNumber" to result.text.substring(6, 11)
                )
            )
        } catch (e: NotFoundException) {
            throw BarcodeNotFoundException("No UPC-A barcode found in image")
        } catch (e: Exception) {
            throw BarcodeDecodingException("Failed to decode UPC-A barcode: ${e.message}")
        }
    }
    
    override fun decode(imagePath: String): BarcodeResult {
        val image = javax.imageio.ImageIO.read(java.io.File(imagePath)) 
            ?: throw BarcodeDecodingException("Could not read image file: $imagePath")
        return decode(image)
    }
    
    private fun getSupportedHints(): Map<DecodeHintType, Any> {
        return mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.UPC_A),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.PURE_BARCODE to false,
            DecodeHintType.ASSUME_GS1 to true
        )
    }
    
    private fun validateInput(image: BufferedImage): Boolean {
        return image.width > 80 && image.height > 30
    }
      override fun getConfiguration(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "version" to version,
            "supportedFormats" to supportedFormats.map { it.name },
            "priority" to priority,
            "features" to listOf(
                "12-digit encoding",
                "Check digit validation",
                "GS1 standard compliance",
                "Product identification"
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

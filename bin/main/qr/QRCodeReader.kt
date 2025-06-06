package qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import utils.ImageUtils
import java.io.File
import javax.imageio.ImageIO

class QRCodeReader {
    
    private val reader = MultiFormatReader()
    
    init {
        // Configuration des hints pour améliorer la détection
        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                com.google.zxing.BarcodeFormat.QR_CODE
            )
        )
        reader.setHints(hints)
    }
    
    /**
     * Lit un QR code à partir d'un fichier image
     */
    fun readQRCode(imageFile: File): String? {
        return try {
            val bufferedImage = ImageIO.read(imageFile)
            
            // Préprocessing de l'image pour améliorer la détection
            val processedImage = ImageUtils.preprocessImage(bufferedImage)
            
            val source = BufferedImageLuminanceSource(processedImage)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            val result = reader.decode(bitmap)
            result.text
        } catch (e: Exception) {
            println("Erreur lors de la lecture: ${e.message}")
            null
        }
    }
    
    /**
     * Lit un QR code à partir d'un chemin d'image
     */
    fun readQRCode(imagePath: String): String? {
        val file = File(imagePath)
        return if (file.exists()) {
            readQRCode(file)
        } else {
            println("Le fichier n'existe pas: $imagePath")
            null
        }
    }
}
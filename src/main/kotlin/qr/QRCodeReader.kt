package qr

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QRCodeReader {
    private val reader = MultiFormatReader()
    
    fun readQRCode(imagePath: String): String {
        val file = File(imagePath)
        if (!file.exists()) {
            throw IllegalArgumentException("Fichier non trouvé: $imagePath")
        }
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Image invalide")
        return readQRCode(image)
    }
    
    fun readQRCode(image: BufferedImage): String {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        
        return try {
            val result = reader.decode(bitmap)
            result.text
        } catch (e: NotFoundException) {
            throw RuntimeException("Aucun QR code trouvé")
        } catch (e: Exception) {
            throw RuntimeException("Erreur lecture QR: ${e.message}")
        }
    }
}

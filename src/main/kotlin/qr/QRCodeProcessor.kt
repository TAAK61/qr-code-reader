package qr

import utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class QRCodeProcessor {
    
    /**
     * Traite une image pour améliorer la détection de QR codes
     */
    fun processImageForQR(inputFile: File, outputFile: File): Boolean {
        return try {
            val originalImage = ImageIO.read(inputFile)
            val processedImage = processImage(originalImage)
            
            ImageIO.write(processedImage, "png", outputFile)
            true
        } catch (e: Exception) {
            println("Erreur lors du traitement de l'image: ${e.message}")
            false
        }
    }
    
    /**
     * Applique des filtres pour améliorer la détection
     */
    private fun processImage(image: BufferedImage): BufferedImage {
        var processed = image
        
        // Conversion en niveaux de gris
        processed = ImageUtils.convertToGrayscale(processed)
        
        // Amélioration du contraste
        processed = ImageUtils.enhanceContrast(processed)
        
        // Réduction du bruit
        processed = ImageUtils.reduceNoise(processed)
        
        return processed
    }
    
    /**
     * Redimensionne une image si nécessaire
     */
    fun resizeImage(image: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        val width = image.width
        val height = image.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return image
        }
        
        val ratio = minOf(maxWidth.toDouble() / width, maxHeight.toDouble() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return ImageUtils.resizeImage(image, newWidth, newHeight)
    }
}
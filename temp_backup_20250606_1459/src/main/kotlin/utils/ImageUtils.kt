package utils

import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object ImageUtils {
    /**
     * Préprocessse une image pour améliorer la détection de QR codes
     */
    fun preprocessImage(image: BufferedImage): BufferedImage {
        var processed = image
        processed = convertToGrayscale(processed)
        processed = enhanceContrast(processed)
        processed = reduceNoise(processed)
        return processed
    }

    /**
     * Convertit une image en niveaux de gris
     */
    fun convertToGrayscale(image: BufferedImage): BufferedImage {
        val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g2d = grayImage.createGraphics()
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        return grayImage
    }

    /**
     * Améliore le contraste de l'image
     */
    fun enhanceContrast(image: BufferedImage): BufferedImage {
        val enhanced = BufferedImage(image.width, image.height, image.type)
        val factor = 1.5f // Facteur de contraste

        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val rgb = image.getRGB(x, y)
                val color = Color(rgb)

                val red = minOf(255, maxOf(0, (factor * (color.red - 128) + 128).toInt()))
                val green = minOf(255, maxOf(0, (factor * (color.green - 128) + 128).toInt()))
                val blue = minOf(255, maxOf(0, (factor * (color.blue - 128) + 128).toInt()))

                val newColor = Color(red, green, blue)
                enhanced.setRGB(x, y, newColor.rgb)
            }
        }

        return enhanced
    }

    /**
     * Réduit le bruit dans l'image
     */
    fun reduceNoise(image: BufferedImage): BufferedImage {
        val filtered = BufferedImage(image.width, image.height, image.type)
        val g2d = filtered.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.drawImage(image, 0, 0, null)
        g2d.dispose()
        return filtered
    }

    /**
     * Redimensionne une image
     */
    fun resizeImage(image: BufferedImage, newWidth: Int, newHeight: Int): BufferedImage {
        val resized = BufferedImage(newWidth, newHeight, image.type)
        val g2d = resized.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null)
        g2d.dispose()
        return resized
    }

    fun loadImage(filePath: String): BufferedImage {
        return ImageIO.read(File(filePath))
    }

    fun saveImage(image: BufferedImage, filePath: String) {
        ImageIO.write(image, "png", File(filePath))
    }
}
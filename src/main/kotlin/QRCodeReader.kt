package qr

import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Simple QR Code Reader - Version 1.0
 * 
 * A basic implementation for reading QR codes from image files.
 * This is the initial stable version with core functionality.
 */
class QRCodeReader {
    
    private val reader = MultiFormatReader()
    
    /**
     * Reads a QR code from the specified file path.
     * 
     * @param filePath Path to the image file
     * @return The decoded text content
     * @throws IllegalArgumentException if file doesn't exist or is invalid
     * @throws RuntimeException if no QR code is found
     */
    fun readQRCode(filePath: String): String {
        require(filePath.isNotBlank()) { "File path cannot be empty" }
        
        val file = File(filePath)
        require(file.exists()) { "File not found: $filePath" }
        require(file.isFile) { "Path is not a file: $filePath" }
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Invalid image file: $filePath")
        return decodeQRCode(image)
    }
    
    /**
     * Reads a QR code from a BufferedImage.
     * 
     * @param image The image containing the QR code
     * @return The decoded text content
     * @throws IllegalArgumentException if image is null
     * @throws RuntimeException if no QR code is found
     */
    fun readQRCode(image: BufferedImage?): String {
        requireNotNull(image) { "Image cannot be null" }
        require(image.width > 0 && image.height > 0) { "Image dimensions must be positive" }
        
        return decodeQRCode(image)
    }
    
    /**
     * Decodes QR code from the provided image.
     */
    private fun decodeQRCode(image: BufferedImage): String {
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        
        return try {
            val result = reader.decode(bitmap)
            result.text
        } catch (e: NotFoundException) {
            throw RuntimeException("No QR code found in image", e)
        } catch (e: ChecksumException) {
            throw RuntimeException("QR code is damaged or corrupted", e)
        } catch (e: FormatException) {
            throw RuntimeException("Invalid QR code format", e)
        } catch (e: Exception) {
            throw RuntimeException("Error reading QR code: ${e.message}", e)
        }
    }
}
package qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.crypto.SecretKey

/**
 * QR Code Generator with comprehensive configuration options and encryption support
 * 
 * Task 72: Implement QR code generation functionality
 * Task 73: Add support for encrypted QR codes
 */
class QRCodeGenerator {
    
    /**
     * Configuration options for QR code generation
     */
    data class QRCodeConfig(
        val width: Int = 300,
        val height: Int = 300,
        val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
        val characterSet: String = "UTF-8",
        val margin: Int = 1
    )
    
    /**
     * Result of QR code generation operation
     */
    data class GenerationResult(
        val success: Boolean,
        val filePath: String? = null,
        val image: BufferedImage? = null,
        val error: String? = null,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    /**
     * Result of content validation
     */
    data class ContentValidationResult(
        val valid: Boolean,
        val errors: List<String>,
        val warnings: List<String>,
        val suggestedErrorCorrectionLevel: ErrorCorrectionLevel
    )
    
    private val qrCodeWriter = QRCodeWriter()
    private val encryption = QRCodeEncryption()
    
    /**
     * Generate QR code and save to file
     * 
     * @param content The text content to encode
     * @param outputPath The file path where to save the QR code image
     * @param config Configuration options for the QR code
     * @return Generation result with success status and metadata
     */
    fun generateQRCode(
        content: String,
        outputPath: String,
        config: QRCodeConfig = QRCodeConfig()
    ): GenerationResult {
        return try {
            val hints = buildHints(config)
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, config.width, config.height, hints)
            
            val path = Paths.get(outputPath)
            
            // Ensure parent directories exist
            path.parent?.let { parentPath ->
                File(parentPath.toString()).mkdirs()
            }
            
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path)
            
            val fileSize = File(outputPath).length()
            val metadata = mapOf(
                "width" to config.width,
                "height" to config.height,
                "contentLength" to content.length,
                "fileSizeBytes" to fileSize,
                "errorCorrectionLevel" to config.errorCorrectionLevel.toString(),
                "format" to "PNG"
            )
            
            GenerationResult(
                success = true,
                filePath = outputPath,
                metadata = metadata
            )
        } catch (e: WriterException) {
            GenerationResult(
                success = false,
                error = "Failed to encode QR code: ${e.message}"
            )
        } catch (e: IOException) {
            GenerationResult(
                success = false,
                error = "Failed to save QR code to file: ${e.message}"
            )
        } catch (e: Exception) {
            GenerationResult(
                success = false,
                error = "Unexpected error: ${e.message}"
            )
        }
    }
    
    /**
     * Generate QR code as BufferedImage without saving to file
     * 
     * @param content The text content to encode
     * @param config Configuration options for the QR code
     * @return Generation result with BufferedImage
     */
    fun generateQRCodeImage(
        content: String,
        config: QRCodeConfig = QRCodeConfig()
    ): GenerationResult {
        return try {
            val hints = buildHints(config)
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, config.width, config.height, hints)
            
            val bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)
            
            val metadata = mapOf(
                "width" to config.width,
                "height" to config.height,
                "contentLength" to content.length,
                "errorCorrectionLevel" to config.errorCorrectionLevel.toString()
            )
            
            GenerationResult(
                success = true,
                image = bufferedImage,
                metadata = metadata
            )
        } catch (e: WriterException) {
            GenerationResult(
                success = false,
                error = "Failed to encode QR code: ${e.message}"
            )
        } catch (e: Exception) {
            GenerationResult(
                success = false,
                error = "Unexpected error: ${e.message}"
            )
        }
    }
    
    /**
     * Generate encrypted QR code and save to file
     * 
     * @param content The text content to encrypt and encode
     * @param outputPath The file path where to save the QR code image
     * @param encryptionConfig Encryption configuration
     * @param qrConfig QR code configuration options
     * @return Generation result with encryption metadata
     */
    fun generateEncryptedQRCode(
        content: String,
        outputPath: String,
        encryptionConfig: QRCodeEncryption.EncryptionConfig = QRCodeEncryption.EncryptionConfig(),
        qrConfig: QRCodeConfig = QRCodeConfig()
    ): GenerationResult {
        return try {
            // First encrypt the content
            val encryptionResult = encryption.encrypt(content, encryptionConfig)
            
            if (!encryptionResult.success) {
                return GenerationResult(
                    success = false,
                    error = "Encryption failed: ${encryptionResult.error}"
                )
            }
            
            // Then generate QR code with encrypted content
            val qrResult = generateQRCode(encryptionResult.encryptedContent!!, outputPath, qrConfig)
            
            if (!qrResult.success) {
                return qrResult
            }
            
            // Combine metadata from both operations
            val combinedMetadata = qrResult.metadata.toMutableMap()
            combinedMetadata.putAll(encryptionResult.metadata)
            combinedMetadata["encrypted"] = true
            combinedMetadata["originalContentLength"] = content.length
            
            // Include key information if available
            encryptionResult.key?.let { key ->
                combinedMetadata["keyBase64"] = encryption.exportKey(key)
            }
            
            GenerationResult(
                success = true,
                filePath = qrResult.filePath,
                metadata = combinedMetadata
            )
        } catch (e: Exception) {
            GenerationResult(
                success = false,
                error = "Failed to generate encrypted QR code: ${e.message}"
            )
        }
    }
    
    /**
     * Generate encrypted QR code as BufferedImage without saving to file
     * 
     * @param content The text content to encrypt and encode
     * @param encryptionConfig Encryption configuration
     * @param qrConfig QR code configuration options
     * @return Generation result with BufferedImage and encryption metadata
     */
    fun generateEncryptedQRCodeImage(
        content: String,
        encryptionConfig: QRCodeEncryption.EncryptionConfig = QRCodeEncryption.EncryptionConfig(),
        qrConfig: QRCodeConfig = QRCodeConfig()
    ): GenerationResult {
        return try {
            // First encrypt the content
            val encryptionResult = encryption.encrypt(content, encryptionConfig)
            
            if (!encryptionResult.success) {
                return GenerationResult(
                    success = false,
                    error = "Encryption failed: ${encryptionResult.error}"
                )
            }
            
            // Then generate QR code with encrypted content
            val qrResult = generateQRCodeImage(encryptionResult.encryptedContent!!, qrConfig)
            
            if (!qrResult.success) {
                return qrResult
            }
            
            // Combine metadata from both operations
            val combinedMetadata = qrResult.metadata.toMutableMap()
            combinedMetadata.putAll(encryptionResult.metadata)
            combinedMetadata["encrypted"] = true
            combinedMetadata["originalContentLength"] = content.length
            
            // Include key information if available
            encryptionResult.key?.let { key ->
                combinedMetadata["keyBase64"] = encryption.exportKey(key)
            }
            
            GenerationResult(
                success = true,
                image = qrResult.image,
                metadata = combinedMetadata
            )
        } catch (e: Exception) {
            GenerationResult(
                success = false,
                error = "Failed to generate encrypted QR code image: ${e.message}"
            )
        }
    }
    
    /**
     * Generate multiple QR codes in batch and save to files
     * 
     * @param contentList List of content strings to encode
     * @param outputDirectory Directory where to save the QR code images
     * @param fileNamePrefix Prefix for the generated file names
     * @param config Configuration options for the QR codes
     * @return List of generation results, one for each content item
     */
    fun generateQRCodesBatch(
        contentList: List<String>,
        outputDirectory: String,
        fileNamePrefix: String,
        config: QRCodeConfig = QRCodeConfig()
    ): List<GenerationResult> {
        val results = mutableListOf<GenerationResult>()
        
        // Ensure output directory exists
        val outputDir = File(outputDirectory)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        contentList.forEachIndexed { index, content ->
            val fileName = "$fileNamePrefix-${index + 1}.png"
            val outputPath = File(outputDirectory, fileName).absolutePath
            
            val result = generateQRCode(content, outputPath, config)
            results.add(result)
        }
        
        return results
    }

    /**
     * Build encoding hints from configuration
     */
    private fun buildHints(config: QRCodeConfig): Map<EncodeHintType, Any> {
        return mapOf(
            EncodeHintType.ERROR_CORRECTION to config.errorCorrectionLevel,
            EncodeHintType.CHARACTER_SET to config.characterSet,
            EncodeHintType.MARGIN to config.margin
        )
    }
    
    /**
     * Validate content for QR code generation
     * 
     * @param content The content to validate
     * @return Validation result with suggestions if content is problematic
     */
    fun validateContent(content: String): ContentValidationResult {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        
        // Check content length
        when {
            content.isEmpty() -> errors.add("Content cannot be empty")
            content.length > 2953 -> errors.add("Content too long (max 2953 characters for alphanumeric)")
            content.length > 1000 -> warnings.add("Large content may result in dense QR code that's harder to scan")
        }
        
        // Check for problematic characters
        if (content.contains('\n')) {
            warnings.add("Content contains line breaks - ensure your scanner supports multi-line QR codes")
        }
        
        return ContentValidationResult(
            valid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            suggestedErrorCorrectionLevel = when {
                content.length < 100 -> ErrorCorrectionLevel.H
                content.length < 500 -> ErrorCorrectionLevel.M
                else -> ErrorCorrectionLevel.L
            }
        )
    }
    
    /**
     * Generate QR codes for common data types with appropriate formatting
     */
    object Templates {
        
        /**
         * Generate QR code for URL
         */
        fun generateURL(url: String, outputPath: String, config: QRCodeConfig = QRCodeConfig()): GenerationResult {
            val generator = QRCodeGenerator()
            return generator.generateQRCode(url, outputPath, config)
        }
        
        /**
         * Generate QR code for WiFi configuration
         */
        fun generateWiFi(
            ssid: String,
            password: String,
            security: String = "WPA",
            hidden: Boolean = false,
            outputPath: String,
            config: QRCodeConfig = QRCodeConfig()
        ): GenerationResult {
            val wifiString = "WIFI:T:$security;S:$ssid;P:$password;H:$hidden;"
            val generator = QRCodeGenerator()
            return generator.generateQRCode(wifiString, outputPath, config)
        }
        
        /**
         * Generate QR code for contact information (vCard)
         */
        fun generateVCard(
            name: String,
            organization: String? = null,
            phone: String? = null,
            email: String? = null,
            url: String? = null,
            outputPath: String,
            config: QRCodeConfig = QRCodeConfig()
        ): GenerationResult {
            val vcard = buildString {
                appendLine("BEGIN:VCARD")
                appendLine("VERSION:3.0")
                appendLine("FN:$name")
                organization?.let { appendLine("ORG:$it") }
                phone?.let { appendLine("TEL:$it") }
                email?.let { appendLine("EMAIL:$it") }
                url?.let { appendLine("URL:$it") }
                appendLine("END:VCARD")
            }
            
            val generator = QRCodeGenerator()
            return generator.generateQRCode(vcard, outputPath, config)
        }
        
        /**
         * Generate QR code for SMS
         */
        fun generateSMS(
            phoneNumber: String,
            message: String,
            outputPath: String,
            config: QRCodeConfig = QRCodeConfig()
        ): GenerationResult {
            val smsString = "smsto:$phoneNumber:$message"
            val generator = QRCodeGenerator()
            return generator.generateQRCode(smsString, outputPath, config)
        }
        
        /**
         * Generate QR code for email
         */
        fun generateEmail(
            email: String,
            subject: String? = null,
            body: String? = null,
            outputPath: String,
            config: QRCodeConfig = QRCodeConfig()
        ): GenerationResult {
            val emailString = buildString {
                append("mailto:$email")
                if (subject != null || body != null) {
                    append("?")
                    val params = mutableListOf<String>()
                    subject?.let { params.add("subject=${it.replace(" ", "%20")}") }
                    body?.let { params.add("body=${it.replace(" ", "%20")}") }
                    append(params.joinToString("&"))
                }
            }
            
            val generator = QRCodeGenerator()
            return generator.generateQRCode(emailString, outputPath, config)
        }
        
        /**
         * Generate QR code for geographic location
         */
        fun generateLocation(
            latitude: Double,
            longitude: Double,
            outputPath: String,
            config: QRCodeConfig = QRCodeConfig()
        ): GenerationResult {
            val locationString = "geo:$latitude,$longitude"
            val generator = QRCodeGenerator()
            return generator.generateQRCode(locationString, outputPath, config)
        }
    }
}

package utils

import qr.exceptions.InvalidInputException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Input validation utility for the QR Code Reader application.
 * 
 * This class provides comprehensive input validation methods to ensure
 * data integrity and prevent invalid operations.
 */
object InputValidator {
    
    // Supported image file extensions
    private val SUPPORTED_IMAGE_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "bmp", "gif", "tiff", "webp"
    )
    
    // Maximum file size (50MB)
    private const val MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024L
    
    // Maximum image dimensions
    private const val MAX_IMAGE_WIDTH = 10000
    private const val MAX_IMAGE_HEIGHT = 10000
    
    /**
     * Validates a file path for QR code processing.
     * 
     * @param filePath The file path to validate
     * @throws InvalidInputException if validation fails
     */
    fun validateFilePath(filePath: String?) {
        when {
            filePath.isNullOrBlank() -> 
                throw InvalidInputException("filePath", "non-empty string")
            
            filePath.trim() != filePath -> 
                throw InvalidInputException("filePath", "string without leading/trailing whitespace")
            
            filePath.length > 260 -> // Windows MAX_PATH limit
                throw InvalidInputException("filePath", "path shorter than 260 characters")
        }
        
        val path = try {
            Paths.get(filePath)
        } catch (e: Exception) {
            throw InvalidInputException("filePath", "valid file path", "Invalid path format: ${e.message}")
        }
        
        validatePath(path)
    }
    
    /**
     * Validates a Path object.
     * 
     * @param path The path to validate
     * @throws InvalidInputException if validation fails
     */
    fun validatePath(path: Path) {
        val file = path.toFile()
        
        when {
            !file.exists() -> 
                throw InvalidInputException("path", "existing file", "File does not exist: ${path.toAbsolutePath()}")
            
            !file.isFile() -> 
                throw InvalidInputException("path", "regular file", "Path is not a file: ${path.toAbsolutePath()}")
            
            !file.canRead() -> 
                throw InvalidInputException("path", "readable file", "Cannot read file: ${path.toAbsolutePath()}")
            
            file.length() == 0L -> 
                throw InvalidInputException("path", "non-empty file", "File is empty: ${path.toAbsolutePath()}")
            
            file.length() > MAX_FILE_SIZE_BYTES -> 
                throw InvalidInputException("path", "file smaller than ${MAX_FILE_SIZE_BYTES / (1024 * 1024)}MB")
        }
        
        validateImageFileExtension(file)
    }
    
    /**
     * Validates that a file has a supported image extension.
     * 
     * @param file The file to validate
     * @throws InvalidInputException if the extension is not supported
     */
    fun validateImageFileExtension(file: File) {
        val extension = file.extension.lowercase()
        
        if (extension !in SUPPORTED_IMAGE_EXTENSIONS) {
            throw InvalidInputException(
                "file extension", 
                "one of: ${SUPPORTED_IMAGE_EXTENSIONS.joinToString(", ")}", 
                "Unsupported extension: $extension"
            )
        }
    }
    
    /**
     * Validates image dimensions.
     * 
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @throws InvalidInputException if dimensions are invalid
     */
    fun validateImageDimensions(width: Int, height: Int) {
        when {
            width <= 0 -> 
                throw InvalidInputException("width", "positive integer", "Width must be greater than 0: $width")
            
            height <= 0 -> 
                throw InvalidInputException("height", "positive integer", "Height must be greater than 0: $height")
            
            width > MAX_IMAGE_WIDTH -> 
                throw InvalidInputException("width", "value <= $MAX_IMAGE_WIDTH", "Width too large: $width")
            
            height > MAX_IMAGE_HEIGHT -> 
                throw InvalidInputException("height", "value <= $MAX_IMAGE_HEIGHT", "Height too large: $height")
        }
    }
    
    /**
     * Validates a contrast factor for image processing.
     * 
     * @param factor The contrast factor to validate
     * @throws InvalidInputException if the factor is invalid
     */
    fun validateContrastFactor(factor: Double) {
        when {
            factor.isNaN() -> 
                throw InvalidInputException("contrastFactor", "valid number", "Factor cannot be NaN")
            
            factor.isInfinite() -> 
                throw InvalidInputException("contrastFactor", "finite number", "Factor cannot be infinite")
            
            factor < 0.1 -> 
                throw InvalidInputException("contrastFactor", "value >= 0.1", "Factor too small: $factor")
            
            factor > 10.0 -> 
                throw InvalidInputException("contrastFactor", "value <= 10.0", "Factor too large: $factor")
        }
    }
    
    /**
     * Validates a percentage value (0.0 to 1.0).
     * 
     * @param value The percentage value to validate
     * @param parameterName The name of the parameter for error messages
     * @throws InvalidInputException if the value is invalid
     */
    fun validatePercentage(value: Double, parameterName: String = "percentage") {
        when {
            value.isNaN() -> 
                throw InvalidInputException(parameterName, "valid number", "Value cannot be NaN")
            
            value.isInfinite() -> 
                throw InvalidInputException(parameterName, "finite number", "Value cannot be infinite")
            
            value < 0.0 -> 
                throw InvalidInputException(parameterName, "value >= 0.0", "Value cannot be negative: $value")
            
            value > 1.0 -> 
                throw InvalidInputException(parameterName, "value <= 1.0", "Value cannot exceed 1.0: $value")
        }
    }
    
    /**
     * Validates a positive integer parameter.
     * 
     * @param value The integer value to validate
     * @param parameterName The name of the parameter for error messages
     * @param minValue The minimum allowed value (default: 1)
     * @param maxValue The maximum allowed value (optional)
     * @throws InvalidInputException if the value is invalid
     */
    fun validatePositiveInteger(
        value: Int, 
        parameterName: String, 
        minValue: Int = 1, 
        maxValue: Int? = null
    ) {
        when {
            value < minValue -> 
                throw InvalidInputException(parameterName, "value >= $minValue", "Value too small: $value")
            
            maxValue != null && value > maxValue -> 
                throw InvalidInputException(parameterName, "value <= $maxValue", "Value too large: $value")
        }
    }
    
    /**
     * Validates a timeout value in milliseconds.
     * 
     * @param timeoutMs The timeout value to validate
     * @throws InvalidInputException if the timeout is invalid
     */
    fun validateTimeout(timeoutMs: Long) {
        when {
            timeoutMs <= 0 -> 
                throw InvalidInputException("timeout", "positive value", "Timeout must be positive: $timeoutMs")
            
            timeoutMs > 300_000 -> // 5 minutes max
                throw InvalidInputException("timeout", "value <= 300000ms", "Timeout too large: $timeoutMs")
        }
    }
    
    /**
     * Validates a string parameter.
     * 
     * @param value The string value to validate
     * @param parameterName The name of the parameter for error messages
     * @param allowEmpty Whether empty strings are allowed
     * @param maxLength Maximum allowed length (optional)
     * @throws InvalidInputException if the string is invalid
     */
    fun validateString(
        value: String?, 
        parameterName: String, 
        allowEmpty: Boolean = false, 
        maxLength: Int? = null
    ) {
        when {
            value == null -> 
                throw InvalidInputException(parameterName, "non-null string")
            
            !allowEmpty && value.isBlank() -> 
                throw InvalidInputException(parameterName, "non-empty string")
            
            maxLength != null && value.length > maxLength -> 
                throw InvalidInputException(parameterName, "string with length <= $maxLength", "Length: ${value.length}")
        }
    }
    
    /**
     * Checks if a file path represents a supported image file.
     * 
     * @param filePath The file path to check
     * @return true if the file appears to be a supported image
     */
    fun isSupportedImageFile(filePath: String): Boolean {
        return try {
            val extension = File(filePath).extension.lowercase()
            extension in SUPPORTED_IMAGE_EXTENSIONS
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets the list of supported image extensions.
     * 
     * @return Set of supported file extensions
     */
    fun getSupportedExtensions(): Set<String> = SUPPORTED_IMAGE_EXTENSIONS.toSet()
}
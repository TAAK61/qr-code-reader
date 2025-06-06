package qr.exceptions

/**
 * Base exception class for all QR code processing related errors.
 * 
 * This sealed class hierarchy provides a type-safe way to handle different
 * types of errors that can occur during QR code processing operations.
 * 
 * @property message The error message describing what went wrong
 * @property cause The underlying cause of the exception, if any
 */
sealed class QRCodeException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when a file cannot be found or accessed.
 * 
 * @property filePath The path of the file that could not be found
 */
class FileNotFoundException(
    val filePath: String,
    message: String = "File not found: $filePath"
) : QRCodeException(message)

/**
 * Exception thrown when an image file format is not supported or invalid.
 * 
 * @property fileName The name of the invalid image file
 */
class InvalidImageFormatException(
    val fileName: String,
    message: String = "Invalid or unsupported image format: $fileName"
) : QRCodeException(message)

/**
 * Exception thrown when no QR code is detected in the processed image.
 * 
 * @property imageInfo Additional information about the processed image
 */
class QRCodeNotDetectedException(
    val imageInfo: String = "",
    message: String = "No QR code detected in the image${if (imageInfo.isNotEmpty()) ": $imageInfo" else ""}"
) : QRCodeException(message)

/**
 * Exception thrown when image processing operations fail.
 * 
 * @property operation The processing operation that failed
 */
class ImageProcessingException(
    val operation: String,
    message: String = "Image processing failed during: $operation",
    cause: Throwable? = null
) : QRCodeException(message, cause)

/**
 * Exception thrown when input validation fails.
 * 
 * @property parameter The parameter that failed validation
 * @property expectedFormat The expected format or constraint
 */
class InvalidInputException(
    val parameter: String,
    val expectedFormat: String,
    message: String = "Invalid input for parameter '$parameter'. Expected: $expectedFormat"
) : QRCodeException(message)

/**
 * Exception thrown when QR code decoding fails even after detection.
 * 
 * @property errorCode The specific error code from the decoding library
 */
class QRCodeDecodingException(
    val errorCode: String? = null,
    message: String = "Failed to decode QR code${errorCode?.let { " (Error: $it)" } ?: ""}",
    cause: Throwable? = null
) : QRCodeException(message, cause)

/**
 * Exception thrown when a requested operation times out.
 * 
 * @property timeoutMs The timeout duration in milliseconds
 * @property operation The operation that timed out
 */
class ProcessingTimeoutException(
    val timeoutMs: Long,
    val operation: String,
    message: String = "Operation '$operation' timed out after ${timeoutMs}ms"
) : QRCodeException(message)
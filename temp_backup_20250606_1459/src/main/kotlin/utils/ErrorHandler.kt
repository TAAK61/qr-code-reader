package utils

import qr.exceptions.*
import resources.Messages
import java.io.IOException
import java.nio.file.NoSuchFileException

/**
 * Centralized error handling utility for the QR Code Reader application.
 * 
 * This class provides consistent error handling strategies across all components
 * and translates various exceptions into user-friendly messages.
 */
object ErrorHandler {
    
    /**
     * Error severity levels for categorizing different types of errors.
     */
    enum class ErrorSeverity {
        LOW,     // Warnings, non-critical issues
        MEDIUM,  // Processing errors that can be recovered
        HIGH,    // Critical errors that prevent operation
        CRITICAL // System-level errors
    }
    
    /**
     * Data class representing a handled error with additional context.
     */
    data class ErrorInfo(
        val severity: ErrorSeverity,
        val message: String,
        val userMessage: String,
        val errorCode: String,
        val cause: Throwable? = null,
        val context: Map<String, String> = emptyMap()
    )
    
    /**
     * Handles exceptions and converts them to ErrorInfo objects.
     * 
     * @param exception The exception to handle
     * @param context Additional context information
     * @return ErrorInfo object with categorized error information
     */
    fun handleException(exception: Throwable, context: Map<String, String> = emptyMap()): ErrorInfo {
        return when (exception) {
            is FileNotFoundException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "File not found: ${exception.filePath}",
                userMessage = "${Messages.ERROR_FILE_NOT_FOUND}: ${exception.filePath}",
                errorCode = "FILE_NOT_FOUND",
                cause = exception,
                context = context
            )
            
            is InvalidImageFormatException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "Invalid image format: ${exception.fileName}",
                userMessage = "${Messages.ERROR_INVALID_IMAGE}: ${exception.fileName}",
                errorCode = "INVALID_IMAGE_FORMAT",
                cause = exception,
                context = context
            )
            
            is QRCodeNotDetectedException -> ErrorInfo(
                severity = ErrorSeverity.MEDIUM,
                message = "No QR code detected in image",
                userMessage = Messages.ERROR_NO_QR_CODE,
                errorCode = "QR_CODE_NOT_DETECTED",
                cause = exception,
                context = context
            )
            
            is ImageProcessingException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "Image processing failed during: ${exception.operation}",
                userMessage = "${Messages.ERROR_PROCESSING_FAILED}: ${exception.operation}",
                errorCode = "IMAGE_PROCESSING_FAILED",
                cause = exception,
                context = context
            )
            
            is InvalidInputException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "Invalid input: ${exception.parameter}",
                userMessage = "${Messages.ERROR_INVALID_INPUT}: ${exception.parameter}",
                errorCode = "INVALID_INPUT",
                cause = exception,
                context = context
            )
            
            is QRCodeDecodingException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "QR code decoding failed: ${exception.errorCode ?: "Unknown error"}",
                userMessage = Messages.ERROR_PROCESSING_FAILED,
                errorCode = "QR_DECODING_FAILED",
                cause = exception,
                context = context
            )
            
            is ProcessingTimeoutException -> ErrorInfo(
                severity = ErrorSeverity.MEDIUM,
                message = "Processing timeout: ${exception.operation} (${exception.timeoutMs}ms)",
                userMessage = "Processing timed out. Please try with a smaller image.",
                errorCode = "PROCESSING_TIMEOUT",
                cause = exception,
                context = context
            )
            
            is NoSuchFileException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "File not found: ${exception.file}",
                userMessage = Messages.ERROR_FILE_NOT_FOUND,
                errorCode = "FILE_NOT_FOUND",
                cause = exception,
                context = context
            )
            
            is IOException -> ErrorInfo(
                severity = ErrorSeverity.HIGH,
                message = "IO error: ${exception.message}",
                userMessage = "Error reading file. Please check file permissions.",
                errorCode = "IO_ERROR",
                cause = exception,
                context = context
            )
            
            is OutOfMemoryError -> ErrorInfo(
                severity = ErrorSeverity.CRITICAL,
                message = "Out of memory error",
                userMessage = "Image too large. Please try with a smaller image.",
                errorCode = "OUT_OF_MEMORY",
                cause = exception,
                context = context
            )
            
            else -> ErrorInfo(
                severity = ErrorSeverity.CRITICAL,
                message = "Unexpected error: ${exception.message}",
                userMessage = "An unexpected error occurred. Please try again.",
                errorCode = "UNEXPECTED_ERROR",
                cause = exception,
                context = context
            )
        }
    }
    
    /**
     * Logs error information using the application logger.
     * 
     * @param errorInfo The error information to log
     */
    fun logError(errorInfo: ErrorInfo) {
        val logMessage = buildString {
            append("Error [${errorInfo.errorCode}]: ${errorInfo.message}")
            if (errorInfo.context.isNotEmpty()) {
                append(" | Context: ${errorInfo.context}")
            }
        }
        
        when (errorInfo.severity) {
            ErrorSeverity.LOW -> Logger.warn("ERROR_HANDLER", logMessage, errorInfo.cause)
            ErrorSeverity.MEDIUM -> Logger.error("ERROR_HANDLER", logMessage, errorInfo.cause)
            ErrorSeverity.HIGH -> Logger.error("ERROR_HANDLER", logMessage, errorInfo.cause)
            ErrorSeverity.CRITICAL -> Logger.error("ERROR_HANDLER", logMessage, errorInfo.cause)
        }
    }
    
    /**
     * Handles an exception, logs it, and returns the user-friendly message.
     * 
     * @param exception The exception to handle
     * @param context Additional context information
     * @return User-friendly error message
     */
    fun handleAndGetUserMessage(exception: Throwable, context: Map<String, String> = emptyMap()): String {
        val errorInfo = handleException(exception, context)
        logError(errorInfo)
        return errorInfo.userMessage
    }
    
    /**
     * Creates a recovery suggestion based on the error type.
     * 
     * @param errorInfo The error information
     * @return Recovery suggestion for the user
     */
    fun getRecoverySuggestion(errorInfo: ErrorInfo): String {
        return when (errorInfo.errorCode) {
            "FILE_NOT_FOUND" -> "Please check the file path and ensure the file exists."
            "INVALID_IMAGE_FORMAT" -> "Please use a supported image format: ${Messages.SUPPORTED_FORMATS}"
            "QR_CODE_NOT_DETECTED" -> "Try enhancing the image quality or ensuring the QR code is clearly visible."
            "IMAGE_PROCESSING_FAILED" -> "The image may be corrupted. Try using a different image."
            "PROCESSING_TIMEOUT" -> "Try using a smaller image or increasing the processing timeout."
            "OUT_OF_MEMORY" -> "Try using a smaller image or closing other applications to free memory."
            "IO_ERROR" -> "Check file permissions and ensure the file is not being used by another application."
            else -> "Please try again or contact support if the problem persists."
        }
    }
}
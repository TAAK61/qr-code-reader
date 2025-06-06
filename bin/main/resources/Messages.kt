package resources

/**
 * Centralized message constants for the QR Code Reader application.
 * All user-facing strings should be defined here for consistency and maintainability.
 */
object Messages {
    // Application Messages
    const val APP_NAME = "QR Code Reader"
    const val APP_VERSION = "1.0.0"
    const val APP_DESCRIPTION = "A QR code reader application that processes images to detect and decode QR codes"
    
    // Success Messages
    const val QR_CODE_DETECTED = "QR code successfully detected and decoded"
    const val IMAGE_PROCESSED = "Image processed successfully"
    const val FILE_LOADED = "File loaded successfully"
    
    // Error Messages
    const val ERROR_FILE_NOT_FOUND = "Error: The specified file could not be found"
    const val ERROR_INVALID_IMAGE = "Error: The file is not a valid image format"
    const val ERROR_NO_QR_CODE = "Error: No QR code detected in the image"
    const val ERROR_PROCESSING_FAILED = "Error: Image processing failed"
    const val ERROR_INVALID_INPUT = "Error: Invalid input provided"
    const val ERROR_UNSUPPORTED_FORMAT = "Error: Unsupported image format"
    
    // User Prompts
    const val PROMPT_ENTER_FILE_PATH = "Please enter the path to the image file:"
    const val PROMPT_CONTINUE = "Press Enter to continue or 'q' to quit:"
    const val PROMPT_SELECT_FILE = "Select an image file to process"
    
    // Processing Messages
    const val PROCESSING_IMAGE = "Processing image..."
    const val ENHANCING_CONTRAST = "Enhancing image contrast..."
    const val REDUCING_NOISE = "Reducing image noise..."
    const val DETECTING_QR_CODE = "Detecting QR code..."
    const val RESIZING_IMAGE = "Resizing image for better detection..."
    
    // Result Messages
    const val QR_CODE_CONTENT = "QR Code Content:"
    const val NO_CONTENT_FOUND = "No readable content found"
    const val PROCESSING_COMPLETE = "Processing completed successfully"
    
    // File Operations
    const val SUPPORTED_FORMATS = "Supported formats: PNG, JPG, JPEG, BMP, GIF"
    const val FILE_SIZE_INFO = "File size: %s bytes"
    const val IMAGE_DIMENSIONS = "Image dimensions: %dx%d pixels"
}
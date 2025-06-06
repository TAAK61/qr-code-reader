import qr.QRCodeReader
import qr.exceptions.QRCodeException
import resources.Messages
import utils.Logger
import java.io.File
import java.util.*
import javax.swing.SwingUtilities
import javax.swing.UIManager
import ui.QRCodeReaderUI

/**
 * Main entry point for the QR Code Reader application.
 * 
 * This application provides a command-line interface for reading QR codes from image files.
 * It supports various image formats and includes image preprocessing for better detection.
 */
fun main(args: Array<String>) {
    Logger.setLevel(Logger.Level.INFO)
    Logger.info("MAIN", "Starting ${Messages.APP_NAME} v${Messages.APP_VERSION}")
    
    val scanner = Scanner(System.`in`)
    val qrCodeReader = QRCodeReader()
    
    println("=".repeat(50))
    println(Messages.APP_NAME)
    println(Messages.APP_DESCRIPTION)
    println("=".repeat(50))
    println()
    
    // Check if file path was provided as command line argument
    if (args.isNotEmpty()) {
        processFile(qrCodeReader, args[0])
    } else {
        // Interactive mode
        runInteractiveMode(scanner, qrCodeReader)
    }
    
    Logger.info("MAIN", "Application terminated")
}

/**
 * Runs the application in interactive mode, prompting user for file paths.
 */
private fun runInteractiveMode(scanner: Scanner, reader: QRCodeReader) {
    var continueProcessing = true
    
    while (continueProcessing) {
        try {
            println(Messages.PROMPT_ENTER_FILE_PATH)
            print("> ")
            val filePath = scanner.nextLine().trim()
            
            if (filePath.isEmpty()) {
                println("Please enter a valid file path.")
                continue
            }
            
            if (filePath.lowercase() == "quit" || filePath.lowercase() == "q") {
                break
            }
            
            processFile(reader, filePath)
            
        } catch (e: Exception) {
            Logger.error("MAIN", "Unexpected error in interactive mode", e)
            println("An unexpected error occurred. Please try again.")
        }
        
        println()
        println(Messages.PROMPT_CONTINUE)
        print("> ")
        val response = scanner.nextLine().trim().lowercase()
        
        if (response == "q" || response == "quit") {
            continueProcessing = false
        }
        
        println()
    }
}

/**
 * Processes a single file and displays the results.
 */
private fun processFile(reader: QRCodeReader, filePath: String) {
    try {
        Logger.info("MAIN", "Processing file: $filePath")
        
        // Validate file exists and is readable
        val file = File(filePath)
        if (!file.exists()) {
            println("❌ ${Messages.ERROR_FILE_NOT_FOUND}: $filePath")
            return
        }
        
        if (!file.canRead()) {
            println("❌ Error: Cannot read file: $filePath")
            return
        }
        
        // Display file information
        displayFileInfo(file)
        
        println("\n${Messages.PROCESSING_IMAGE}")
        val startTime = System.currentTimeMillis()
        
        // Process the QR code
        val result = reader.readQRCode(filePath)
        val processingTime = System.currentTimeMillis() - startTime
        
        // Display results
        displayResults(result, processingTime)
        
    } catch (e: QRCodeException) {
        Logger.error("MAIN", "QR Code processing error", e)
        println("❌ ${e.message}")
    } catch (e: Exception) {
        Logger.error("MAIN", "Unexpected error processing file", e)
        println("❌ ${Messages.ERROR_PROCESSING_FAILED}: ${e.message}")
    }
}

/**
 * Displays information about the file being processed.
 */
private fun displayFileInfo(file: File) {
    println("\n📁 File Information:")
    println("   Name: ${file.name}")
    println("   Size: ${formatFileSize(file.length())}")
    println("   Path: ${file.absolutePath}")
    
    val extension = file.extension.lowercase()
    val supportedFormats = listOf("png", "jpg", "jpeg", "bmp", "gif")
    
    if (extension in supportedFormats) {
        println("   Format: ✅ Supported ($extension)")
    } else {
        println("   Format: ⚠️  Unknown ($extension)")
        println("   ${Messages.SUPPORTED_FORMATS}")
    }
}

/**
 * Displays the QR code reading results.
 */
private fun displayResults(result: qr.QRCodeResult, processingTime: Long) {
    println("\n✅ ${Messages.QR_CODE_DETECTED}")
    println("━".repeat(50))
    println("${Messages.QR_CODE_CONTENT}")
    println("${result.content}")
    println("━".repeat(50))
    println()
    println("📊 Processing Details:")
    println("   Format: ${result.format}")
    println("   Confidence: ${String.format("%.1f%%", result.confidence * 100)}")
    println("   Processing Time: ${processingTime}ms")
    println("   Content Length: ${result.content.length} characters")
    
    // Detect content type
    val contentType = detectContentType(result.content)
    if (contentType.isNotEmpty()) {
        println("   Content Type: $contentType")
    }
}

/**
 * Attempts to detect the type of content in the QR code.
 */
private fun detectContentType(content: String): String {
    return when {
        content.startsWith("http://") || content.startsWith("https://") -> "🌐 URL"
        content.startsWith("mailto:") -> "📧 Email"
        content.startsWith("tel:") -> "📞 Phone Number"
        content.startsWith("WIFI:") -> "📶 WiFi Configuration"
        content.startsWith("BEGIN:VCARD") -> "👤 Contact (vCard)"
        content.contains("@") && content.contains(".") -> "📧 Email Address"
        content.matches(Regex("\\+?[0-9\\s\\-\\(\\)]{10,}")) -> "📞 Phone Number"
        else -> ""
    }
}

/**
 * Formats file size in human-readable format.
 */
private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return if (unitIndex == 0) {
        "${size.toInt()} ${units[unitIndex]}"
    } else {
        String.format("%.1f %s", size, units[unitIndex])
    }
}

fun oldMain() {
    try {
        // Utiliser le look and feel du système
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Lancer l'interface utilisateur dans le thread Swing
    SwingUtilities.invokeLater {
        QRCodeReaderUI()
    }
}

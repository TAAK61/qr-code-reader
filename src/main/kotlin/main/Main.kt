package main

import javafx.application.Application
import qr.QRCodeReader
import ui.QRCodeReaderGUI
import java.io.File

/**
 * Main entry point for QR Code Reader application
 * Supports both GUI and CLI modes
 */
fun main(args: Array<String>) {
    println("QR Code Reader v2.0-dev (Modern GUI Edition)")
    println("=============================================")
    
    // Check for CLI mode flags
    val cliMode = args.contains("--cli") || args.contains("-c")
    val helpRequested = args.contains("--help") || args.contains("-h")
    
    if (helpRequested) {
        printUsage()
        return
    }
    
    // GUI Mode (default)
    if (!cliMode && (args.isEmpty() || !isImageFile(args[0]))) {
        println("Launching GUI mode...")
        println("Features available:")
        println("  ✅ Modern graphical interface")
        println("  ✅ Drag-and-drop support")
        println("  ✅ Image preview")
        println("  ✅ Batch processing")
        println("  ✅ Processing history")
        println("  ✅ Keyboard shortcuts")
        println("")
        
        try {
            Application.launch(QRCodeReaderGUI::class.java, *args)
        } catch (e: Exception) {
            println("❌ Failed to launch GUI: ${e.message}")
            println("Falling back to CLI mode...")
            runCliMode(args)
        }
        return
    }
    
    // CLI Mode
    runCliMode(args.filter { !it.startsWith("--") && !it.startsWith("-") }.toTypedArray())
}

private fun runCliMode(args: Array<String>) {
    println("Running in CLI mode...")
    
    if (args.isEmpty()) {
        println("❌ Error: No image file specified for CLI mode")
        printUsage()
        return
    }
    
    val imagePath = args[0]
    
    if (!File(imagePath).exists()) {
        println("❌ Error: Image file not found: $imagePath")
        return
    }
    
    if (!isImageFile(imagePath)) {
        println("❌ Error: File is not a supported image format: $imagePath")
        println("Supported formats: PNG, JPG, JPEG, GIF, BMP")
        return
    }
    
    try {
        println("📄 Processing: $imagePath")
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        
        println("✅ QR Code successfully decoded!")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Content:")
        println(result)
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        
    } catch (e: Exception) {
        println("❌ Failed to decode QR code")
        println("Error: ${e.message}")
        println("Debug info: ${e.javaClass.simpleName}")
        
        // Suggest trying GUI mode for better error handling
        println("")
        println("💡 Tip: Try GUI mode for advanced image processing options:")
        println("   java -jar qr-code-reader.jar")
    }
}

private fun isImageFile(filePath: String): Boolean {
    val extension = File(filePath).extension.lowercase()
    return extension in listOf("png", "jpg", "jpeg", "gif", "bmp")
}

private fun printUsage() {
    println("""
QR Code Reader - Usage:

GUI Mode (default):
  java -jar qr-code-reader.jar
  
CLI Mode:
  java -jar qr-code-reader.jar --cli <image-path>
  java -jar qr-code-reader.jar -c <image-path>
  
Options:
  --help, -h     Show this help message
  --cli, -c      Force CLI mode
  
Supported formats: PNG, JPG, JPEG, GIF, BMP

Examples:
  java -jar qr-code-reader.jar                    # Launch GUI
  java -jar qr-code-reader.jar --cli image.png    # CLI mode
  java -jar qr-code-reader.jar image.png          # CLI mode (auto-detected)
    """.trimIndent())
}

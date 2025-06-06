package main

import qr.QRCodeReader
import java.io.File

fun main(args: Array<String>) {
    println("QR Code Reader v2.0-dev (Development Branch)")
    println("============================================")
    
    if (args.isEmpty()) {
        println("Usage: java -jar qr-code-reader.jar <image-path>")
        println("")
        println("Development features:")
        println("  • Enhanced error handling")
        println("  • Future: Batch processing")  
        println("  • Future: Multiple formats")
        println("  • Future: Advanced filtering")
        return
    }
    
    val imagePath = args[0]
    
    try {
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        println("QR Code trouvé: $result")
        println("Status: ✅ Lecture réussie (dev mode)")
    } catch (e: Exception) {
        println("❌ Erreur: ${e.message}")
        println("Debug info: ${e.javaClass.simpleName}")
    }
}

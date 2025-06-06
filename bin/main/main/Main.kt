package main

import qr.QRCodeReader
import java.io.File

fun main(args: Array<String>) {
    println("QR Code Reader v1.0")
    
    if (args.isEmpty()) {
        println("Usage: java -jar qr-code-reader.jar <image-path>")
        return
    }
    
    val imagePath = args[0]
    
    try {
        val reader = QRCodeReader()
        val result = reader.readQRCode(imagePath)
        println("QR Code trouvé: $result")
    } catch (e: Exception) {
        println("Erreur: ${e.message}")
    }
}

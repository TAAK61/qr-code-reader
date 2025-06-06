import qr.QRCodeReader
import java.io.File

fun main() {
    println("=== QR Code Reader ===")
    println("Entrez le chemin vers l'image contenant le QR code:")
    
    val imagePath = readLine()
    
    if (imagePath.isNullOrBlank()) {
        println("Erreur: Aucun chemin d'image fourni.")
        return
    }
    
    val imageFile = File(imagePath)
    if (!imageFile.exists()) {
        println("Erreur: Le fichier image n'existe pas: $imagePath")
        return
    }
    
    try {
        val qrReader = QRCodeReader()
        val result = qrReader.readQRCode(imageFile)
        
        if (result != null) {
            println("QR Code détecté avec succès!")
            println("Contenu: $result")
        } else {
            println("Aucun QR code trouvé dans l'image.")
        }
    } catch (e: Exception) {
        println("Erreur lors de la lecture du QR code: ${e.message}")
    }
}
fun main() {
    println("Welcome to the QR Code Reader!")
    println("Please enter the path to the image file containing the QR code:")

    val imagePath = readLine()
    if (imagePath != null) {
        val qrCodeReader = QRCodeReader()
        val image = ImageUtils.loadImage(imagePath)

        if (image != null) {
            val result = qrCodeReader.readQRCode(image)
            if (result != null) {
                println("Decoded QR Code: $result")
            } else {
                println("No QR code found in the image.")
            }
        } else {
            println("Failed to load image. Please check the file path.")
        }
    } else {
        println("No input provided.")
    }
}
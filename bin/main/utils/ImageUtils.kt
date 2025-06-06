object ImageUtils {
    fun loadImage(filePath: String): BufferedImage {
        return ImageIO.read(File(filePath))
    }

    fun saveImage(image: BufferedImage, filePath: String) {
        ImageIO.write(image, "png", File(filePath))
    }
}
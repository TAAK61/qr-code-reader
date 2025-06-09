package barcode

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Unified barcode reader that uses the plugin system.
 * 
 * This class provides a simplified interface for barcode reading
 * that automatically detects and uses the appropriate plugin for
 * different barcode formats.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class UnifiedBarcodeReader {
    
    private val pluginManager = BarcodePluginManager.getInstance()
    
    init {
        // Initialize with default plugins if not already initialized
        if (pluginManager.getRegisteredPlugins().isEmpty()) {
            initializeDefaultPlugins()
        }
    }
    
    /**
     * Read any supported barcode from an image file
     * 
     * @param imagePath Path to the image file
     * @param preferredFormats Optional list of preferred formats to try first
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no barcode is found
     */
    fun readBarcode(imagePath: String, vararg preferredFormats: BarcodeFormat): BarcodeResult {
        return pluginManager.decode(imagePath, preferredFormats.toList())
    }
    
    /**
     * Read any supported barcode from a BufferedImage
     * 
     * @param image The image to decode
     * @param preferredFormats Optional list of preferred formats to try first
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no barcode is found
     */
    fun readBarcode(image: BufferedImage, vararg preferredFormats: BarcodeFormat): BarcodeResult {
        return pluginManager.decode(image, preferredFormats.toList())
    }
    
    /**
     * Read specifically a QR code (for backward compatibility)
     * 
     * @param imagePath Path to the image file
     * @return The decoded QR code content as a string
     * @throws RuntimeException if no QR code is found
     */
    fun readQRCode(imagePath: String): String {
        return try {
            val result = readBarcode(imagePath, BarcodeFormat.QR_CODE)
            result.content
        } catch (e: BarcodeNotFoundException) {
            throw RuntimeException("Aucun QR code trouvé")
        } catch (e: Exception) {
            throw RuntimeException("Erreur lecture QR: ${e.message}")
        }
    }
    
    /**
     * Read specifically a QR code from a BufferedImage (for backward compatibility)
     * 
     * @param image The image to decode
     * @return The decoded QR code content as a string
     * @throws RuntimeException if no QR code is found
     */
    fun readQRCode(image: BufferedImage): String {
        return try {
            val result = readBarcode(image, BarcodeFormat.QR_CODE)
            result.content
        } catch (e: BarcodeNotFoundException) {
            throw RuntimeException("Aucun QR code trouvé")
        } catch (e: Exception) {
            throw RuntimeException("Erreur lecture QR: ${e.message}")
        }
    }
    
    /**
     * Detect possible barcode formats in an image without decoding
     * 
     * @param imagePath Path to the image file
     * @return List of possible barcode formats
     */
    fun detectFormats(imagePath: String): List<BarcodeFormat> {
        val file = File(imagePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $imagePath")
        }
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Invalid image file: $imagePath")
        return pluginManager.detectPossibleFormats(image)
    }
    
    /**
     * Get all supported barcode formats
     * 
     * @return Set of all supported barcode formats
     */
    fun getSupportedFormats(): Set<BarcodeFormat> {
        return pluginManager.getSupportedFormats()
    }
    
    /**
     * Get information about all registered plugins
     * 
     * @return Map containing plugin statistics and details
     */
    fun getPluginInfo(): Map<String, Any> {
        return pluginManager.getPluginStatistics()
    }
    
    /**
     * Try to read multiple barcodes from an image
     * (Future enhancement - currently reads the first found barcode)
     * 
     * @param imagePath Path to the image file
     * @return List of decoded barcode results
     */
    fun readMultipleBarcodes(imagePath: String): List<BarcodeResult> {
        // For now, return single result in a list
        // Future implementations could support multiple barcode detection
        return try {
            listOf(readBarcode(imagePath))
        } catch (e: BarcodeNotFoundException) {
            emptyList()
        }
    }
      /**
     * Initialize default plugins
     */
    private fun initializeDefaultPlugins() {
        try {
            // Register core plugins
            pluginManager.registerPlugin(QRCodePlugin())
            pluginManager.registerPlugin(DataMatrixPlugin())
            pluginManager.registerPlugin(Code128Plugin())
            
            // Register additional barcode format plugins (Task 71)
            pluginManager.registerPlugin(Code39Plugin())
            pluginManager.registerPlugin(UPCAPlugin())
            pluginManager.registerPlugin(EAN13Plugin())
            
            println("Initialized unified barcode reader with ${pluginManager.getRegisteredPlugins().size} plugins")
        } catch (e: Exception) {
            println("Warning: Failed to initialize some plugins: ${e.message}")
        }
    }
    
    /**
     * Register a new plugin
     * 
     * @param plugin The plugin to register
     */
    fun registerPlugin(plugin: BarcodePlugin) {
        pluginManager.registerPlugin(plugin)
    }
    
    /**
     * Get detailed results with metadata
     * 
     * @param imagePath Path to the image file
     * @param includeMetadata Whether to include detailed metadata
     * @return Enhanced barcode result with additional information
     */
    fun readBarcodeDetailed(imagePath: String, includeMetadata: Boolean = true): BarcodeResult {
        val result = readBarcode(imagePath)
        
        if (!includeMetadata) {
            return result
        }
        
        // Add additional metadata
        val file = File(imagePath)
        val enhancedMetadata = result.metadata.toMutableMap()
        enhancedMetadata.putAll(mapOf(
            "fileName" to file.name,
            "fileSize" to file.length(),
            "absolutePath" to file.absolutePath,
            "supportedFormats" to getSupportedFormats().map { it.displayName }
        ))
        
        return result.copy(metadata = enhancedMetadata)
    }
    
    /**
     * Shutdown the barcode reader and all plugins
     */
    fun shutdown() {
        pluginManager.shutdown()
    }
}

package barcode

import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import kotlin.reflect.KClass

/**
 * Plugin Manager for the barcode reading system.
 * 
 * This class manages the registration, discovery, and coordination of barcode plugins.
 * It provides a unified interface for barcode reading while delegating to appropriate plugins.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class BarcodePluginManager private constructor() {
    
    private val plugins = CopyOnWriteArrayList<BarcodePlugin>()
    private val formatToPlugins = ConcurrentHashMap<BarcodeFormat, MutableList<BarcodePlugin>>()
    private val pluginCache = ConcurrentHashMap<String, BarcodePlugin>()
      companion object {
        @Volatile
        private var INSTANCE: BarcodePluginManager? = null
        
        fun getInstance(): BarcodePluginManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BarcodePluginManager().also { INSTANCE = it }
            }
        }
        
        // For testing purposes - allow resetting the singleton
        internal fun resetInstance() {
            synchronized(this) {
                INSTANCE?.shutdown()
                INSTANCE = null
            }
        }
    }
    
    /**
     * Register a new barcode plugin
     * 
     * @param plugin The plugin to register
     * @throws IllegalArgumentException if plugin is already registered
     */
    fun registerPlugin(plugin: BarcodePlugin) {
        synchronized(this) {
            // Check if plugin is already registered
            if (plugins.any { it.name == plugin.name && it.version == plugin.version }) {
                throw IllegalArgumentException("Plugin ${plugin.name} v${plugin.version} is already registered")
            }
            
            plugin.initialize()
            plugins.add(plugin)
            
            // Index by supported formats
            plugin.supportedFormats.forEach { format ->
                formatToPlugins.computeIfAbsent(format) { mutableListOf() }.add(plugin)
                // Sort by priority (highest first)
                formatToPlugins[format]?.sortByDescending { it.priority }
            }
            
            println("Registered barcode plugin: ${plugin.name} v${plugin.version}")
        }
    }
    
    /**
     * Unregister a plugin
     * 
     * @param pluginName Name of the plugin to unregister
     * @param pluginVersion Version of the plugin to unregister
     */
    fun unregisterPlugin(pluginName: String, pluginVersion: String) {
        synchronized(this) {
            val plugin = plugins.find { it.name == pluginName && it.version == pluginVersion }
            plugin?.let {
                it.shutdown()
                plugins.remove(it)
                
                // Remove from format mappings
                formatToPlugins.values.forEach { pluginList ->
                    pluginList.remove(it)
                }
                
                // Clear cache entries for this plugin
                pluginCache.entries.removeIf { entry -> entry.value == it }
                
                println("Unregistered barcode plugin: $pluginName v$pluginVersion")
            }
        }
    }
    
    /**
     * Get all registered plugins
     */
    fun getRegisteredPlugins(): List<BarcodePlugin> = plugins.toList()
    
    /**
     * Get plugins that support a specific format
     */
    fun getPluginsForFormat(format: BarcodeFormat): List<BarcodePlugin> {
        return formatToPlugins[format]?.toList() ?: emptyList()
    }
    
    /**
     * Get all supported formats across all plugins
     */
    fun getSupportedFormats(): Set<BarcodeFormat> = formatToPlugins.keys.toSet()
    
    /**
     * Attempt to decode a barcode from an image using all available plugins
     * 
     * @param image The image to decode
     * @param preferredFormats Optional list of preferred formats to try first
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no barcode is found
     */
    fun decode(image: BufferedImage, preferredFormats: List<BarcodeFormat> = emptyList()): BarcodeResult {
        val startTime = System.currentTimeMillis()
        
        // First, try plugins for preferred formats
        for (format in preferredFormats) {
            val formatPlugins = formatToPlugins[format] ?: continue
            for (plugin in formatPlugins) {
                if (plugin.canHandle(image)) {
                    try {
                        val result = plugin.decode(image)
                        val processingTime = System.currentTimeMillis() - startTime
                        return result.copy(processingTimeMs = processingTime)
                    } catch (e: BarcodeNotFoundException) {
                        // Continue to next plugin
                        continue
                    } catch (e: Exception) {
                        // Log error but continue
                        println("Plugin ${plugin.name} failed to decode: ${e.message}")
                        continue
                    }
                }
            }
        }
        
        // Then try all other plugins in priority order
        val allPlugins = plugins.sortedByDescending { it.priority }
        val triedFormats = preferredFormats.toSet()
        
        for (plugin in allPlugins) {
            // Skip plugins for formats we already tried
            if (plugin.supportedFormats.any { it in triedFormats }) continue
            
            if (plugin.canHandle(image)) {
                try {
                    val result = plugin.decode(image)
                    val processingTime = System.currentTimeMillis() - startTime
                    return result.copy(processingTimeMs = processingTime)
                } catch (e: BarcodeNotFoundException) {
                    // Continue to next plugin
                    continue
                } catch (e: Exception) {
                    // Log error but continue
                    println("Plugin ${plugin.name} failed to decode: ${e.message}")
                    continue
                }
            }
        }
        
        throw BarcodeNotFoundException("No barcode found in image with any registered plugin")
    }
    
    /**
     * Attempt to decode a barcode from an image file
     * 
     * @param imagePath Path to the image file
     * @param preferredFormats Optional list of preferred formats to try first
     * @return The decoded barcode result
     * @throws BarcodeNotFoundException if no barcode is found
     */
    fun decode(imagePath: String, preferredFormats: List<BarcodeFormat> = emptyList()): BarcodeResult {
        val file = File(imagePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $imagePath")
        }
        
        val image = ImageIO.read(file) ?: throw IllegalArgumentException("Invalid image file: $imagePath")
        return decode(image, preferredFormats)
    }
    
    /**
     * Try to detect the most likely barcode format in an image
     * 
     * @param image The image to analyze
     * @return List of possible formats ordered by likelihood
     */
    fun detectPossibleFormats(image: BufferedImage): List<BarcodeFormat> {
        val possibleFormats = mutableListOf<BarcodeFormat>()
        
        for (plugin in plugins.sortedByDescending { it.priority }) {
            if (plugin.canHandle(image)) {
                possibleFormats.addAll(plugin.supportedFormats)
            }
        }
        
        return possibleFormats.distinct()
    }
    
    /**
     * Get plugin statistics
     */
    fun getPluginStatistics(): Map<String, Any> {
        return mapOf(
            "totalPlugins" to plugins.size,
            "supportedFormats" to formatToPlugins.keys.size,
            "plugins" to plugins.map { plugin ->
                mapOf(
                    "name" to plugin.name,
                    "version" to plugin.version,
                    "priority" to plugin.priority,
                    "supportedFormats" to plugin.supportedFormats.map { it.displayName },
                    "configuration" to plugin.getConfiguration()
                )
            }
        )
    }
    
    /**
     * Initialize the plugin manager with default plugins
     */
    fun initializeWithDefaults() {
        // Register the QR Code plugin (our existing implementation)
        registerPlugin(QRCodePlugin())
        
        // Register other plugins as they become available
        // registerPlugin(DataMatrixPlugin())
        // registerPlugin(Code128Plugin())
        // etc.
    }    /**
     * Shutdown all plugins and clean up resources
     */
    fun shutdown() {
        synchronized(this) {
            plugins.forEach { it.shutdown() }
            plugins.clear()
            formatToPlugins.clear()
            pluginCache.clear()
        }
    }
    
    /**
     * Load plugins from a directory (for future extensibility)
     * This method can be implemented to load plugins from JAR files
     */
    fun loadPluginsFromDirectory(pluginDirectory: String) {
        // Future implementation for dynamic plugin loading
        // This would scan for plugin JAR files and load them using reflection
        println("Plugin directory loading not yet implemented: $pluginDirectory")
    }
}

/**
 * Extension function to simplify plugin usage
 */
fun BarcodePluginManager.decodeAny(imagePath: String): BarcodeResult {
    return this.decode(imagePath)
}

/**
 * Extension function to decode with format preference
 */
fun BarcodePluginManager.decodeWithPreference(imagePath: String, vararg formats: BarcodeFormat): BarcodeResult {
    return this.decode(imagePath, formats.toList())
}

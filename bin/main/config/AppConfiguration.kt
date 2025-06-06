package config

import qr.interfaces.ConfigurationInterface
import utils.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * Configuration manager for the QR Code Reader application.
 * 
 * This class handles loading, saving, and accessing application configuration
 * from a properties file with default fallback values.
 */
class AppConfiguration private constructor() : ConfigurationInterface {
    
    companion object {
        private const val TAG = "AppConfiguration"
        private const val CONFIG_FILENAME = "qr-reader.properties"
        private const val DEFAULT_CONFIG_FILENAME = "default-config.properties"
        
        @Volatile
        private var INSTANCE: AppConfiguration? = null
        
        /**
         * Gets the singleton instance of AppConfiguration.
         */
        fun getInstance(): AppConfiguration {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppConfiguration().also { INSTANCE = it }
            }
        }
    }
    
    private val properties = Properties()
    private val defaultProperties = Properties()
    private var configFile: File? = null
    
    init {
        loadDefaultConfiguration()
        loadUserConfiguration()
    }
    
    /**
     * Configuration keys used throughout the application.
     */
    object Keys {
        // Image processing settings
        const val CONTRAST_FACTOR = "image.contrast.factor"
        const val ENABLE_NOISE_REDUCTION = "image.noise.reduction.enabled"
        const val MAX_IMAGE_WIDTH = "image.max.width"
        const val MAX_IMAGE_HEIGHT = "image.max.height"
        const val GRAYSCALE_ALGORITHM = "image.grayscale.algorithm"
        
        // QR code detection settings
        const val DETECTION_TIMEOUT_MS = "qr.detection.timeout"
        const val ENABLE_MULTIPLE_DETECTION = "qr.detection.multiple.enabled"
        const val MIN_QR_CODE_SIZE = "qr.detection.min.size"
        
        // Performance settings
        const val ENABLE_CACHING = "performance.caching.enabled"
        const val CACHE_SIZE_MB = "performance.cache.size.mb"
        const val ENABLE_PARALLEL_PROCESSING = "performance.parallel.enabled"
        const val PROCESSING_THREADS = "performance.threads.count"
        
        // Logging settings
        const val LOG_LEVEL = "logging.level"
        const val LOG_TO_FILE = "logging.file.enabled"
        const val LOG_FILE_PATH = "logging.file.path"
        
        // UI settings
        const val WINDOW_WIDTH = "ui.window.width"
        const val WINDOW_HEIGHT = "ui.window.height"
        const val THEME = "ui.theme"
        const val LANGUAGE = "ui.language"
        
        // Advanced settings
        const val ENABLE_DEBUG_MODE = "debug.enabled"
        const val SAVE_PROCESSED_IMAGES = "debug.save.processed.images"
        const val DEBUG_OUTPUT_DIR = "debug.output.directory"
    }
    
    /**
     * Loads default configuration values.
     */
    private fun loadDefaultConfiguration() {
        defaultProperties.apply {
            // Image processing defaults
            setProperty(Keys.CONTRAST_FACTOR, "1.5")
            setProperty(Keys.ENABLE_NOISE_REDUCTION, "true")
            setProperty(Keys.MAX_IMAGE_WIDTH, "4096")
            setProperty(Keys.MAX_IMAGE_HEIGHT, "4096")
            setProperty(Keys.GRAYSCALE_ALGORITHM, "LUMINANCE")
            
            // QR code detection defaults
            setProperty(Keys.DETECTION_TIMEOUT_MS, "30000")
            setProperty(Keys.ENABLE_MULTIPLE_DETECTION, "false")
            setProperty(Keys.MIN_QR_CODE_SIZE, "21")
            
            // Performance defaults
            setProperty(Keys.ENABLE_CACHING, "true")
            setProperty(Keys.CACHE_SIZE_MB, "100")
            setProperty(Keys.ENABLE_PARALLEL_PROCESSING, "true")
            setProperty(Keys.PROCESSING_THREADS, "${Runtime.getRuntime().availableProcessors()}")
            
            // Logging defaults
            setProperty(Keys.LOG_LEVEL, "INFO")
            setProperty(Keys.LOG_TO_FILE, "false")
            setProperty(Keys.LOG_FILE_PATH, "qr-reader.log")
            
            // UI defaults
            setProperty(Keys.WINDOW_WIDTH, "800")
            setProperty(Keys.WINDOW_HEIGHT, "600")
            setProperty(Keys.THEME, "SYSTEM")
            setProperty(Keys.LANGUAGE, "en")
            
            // Advanced defaults
            setProperty(Keys.ENABLE_DEBUG_MODE, "false")
            setProperty(Keys.SAVE_PROCESSED_IMAGES, "false")
            setProperty(Keys.DEBUG_OUTPUT_DIR, "debug_output")
        }
        
        Logger.debug(TAG, "Default configuration loaded")
    }
    
    /**
     * Loads user configuration from file.
     */
    private fun loadUserConfiguration() {
        val userConfigDir = File(System.getProperty("user.home"), ".qr-reader")
        if (!userConfigDir.exists()) {
            userConfigDir.mkdirs()
        }
        
        configFile = File(userConfigDir, CONFIG_FILENAME)
        
        if (configFile!!.exists()) {
            try {
                FileInputStream(configFile).use { input ->
                    properties.load(input)
                }
                Logger.info(TAG, "User configuration loaded from: ${configFile!!.absolutePath}")
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to load user configuration", e)
                // Fall back to defaults
                properties.clear()
                properties.putAll(defaultProperties)
            }
        } else {
            // Create default config file
            properties.putAll(defaultProperties)
            saveConfiguration()
            Logger.info(TAG, "Created default configuration file: ${configFile!!.absolutePath}")
        }
    }
    
    /**
     * Saves current configuration to file.
     */
    fun saveConfiguration() {
        configFile?.let { file ->
            try {
                FileOutputStream(file).use { output ->
                    properties.store(output, "QR Code Reader Configuration")
                }
                Logger.info(TAG, "Configuration saved to: ${file.absolutePath}")
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to save configuration", e)
            }
        }
    }
    
    override fun getString(key: String, defaultValue: String): String {
        val value = properties.getProperty(key) ?: defaultProperties.getProperty(key) ?: defaultValue
        Logger.debug(TAG, "Config[$key] = $value")
        return value
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return try {
            getString(key, defaultValue.toString()).toInt()
        } catch (e: NumberFormatException) {
            Logger.warn(TAG, "Invalid integer value for key '$key', using default: $defaultValue")
            defaultValue
        }
    }
    
    override fun getDouble(key: String, defaultValue: Double): Double {
        return try {
            getString(key, defaultValue.toString()).toDouble()
        } catch (e: NumberFormatException) {
            Logger.warn(TAG, "Invalid double value for key '$key', using default: $defaultValue")
            defaultValue
        }
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = getString(key, defaultValue.toString()).lowercase()
        return when (value) {
            "true", "yes", "1", "on", "enabled" -> true
            "false", "no", "0", "off", "disabled" -> false
            else -> {
                Logger.warn(TAG, "Invalid boolean value '$value' for key '$key', using default: $defaultValue")
                defaultValue
            }
        }
    }
    
    override fun setValue(key: String, value: Any) {
        val oldValue = properties.getProperty(key)
        properties.setProperty(key, value.toString())
        
        Logger.debug(TAG, "Config[$key] changed from '$oldValue' to '$value'")
        
        // Auto-save on change
        saveConfiguration()
    }
    
    /**
     * Resets all configuration to default values.
     */
    fun resetToDefaults() {
        Logger.info(TAG, "Resetting configuration to defaults")
        properties.clear()
        properties.putAll(defaultProperties)
        saveConfiguration()
    }
    
    /**
     * Gets all configuration keys and values.
     */
    fun getAllSettings(): Map<String, String> {
        return properties.stringPropertyNames().associateWith { key ->
            properties.getProperty(key)
        }
    }
    
    /**
     * Validates the current configuration and logs any issues.
     */
    fun validateConfiguration(): List<String> {
        val issues = mutableListOf<String>()
        
        // Validate image settings
        val maxWidth = getInt(Keys.MAX_IMAGE_WIDTH, 4096)
        val maxHeight = getInt(Keys.MAX_IMAGE_HEIGHT, 4096)
        if (maxWidth <= 0 || maxHeight <= 0) {
            issues.add("Invalid image dimensions: ${maxWidth}x${maxHeight}")
        }
        
        // Validate contrast factor
        val contrastFactor = getDouble(Keys.CONTRAST_FACTOR, 1.5)
        if (contrastFactor < 0.1 || contrastFactor > 10.0) {
            issues.add("Invalid contrast factor: $contrastFactor (should be 0.1-10.0)")
        }
        
        // Validate timeout
        val timeout = getInt(Keys.DETECTION_TIMEOUT_MS, 30000)
        if (timeout <= 0) {
            issues.add("Invalid detection timeout: $timeout (should be positive)")
        }
        
        // Validate cache size
        val cacheSize = getInt(Keys.CACHE_SIZE_MB, 100)
        if (cacheSize < 0) {
            issues.add("Invalid cache size: $cacheSize (should be non-negative)")
        }
        
        // Validate thread count
        val threadCount = getInt(Keys.PROCESSING_THREADS, 4)
        if (threadCount <= 0 || threadCount > 32) {
            issues.add("Invalid thread count: $threadCount (should be 1-32)")
        }
        
        if (issues.isNotEmpty()) {
            Logger.warn(TAG, "Configuration validation found ${issues.size} issue(s)")
            issues.forEach { issue ->
                Logger.warn(TAG, "  - $issue")
            }
        } else {
            Logger.info(TAG, "Configuration validation passed")
        }
        
        return issues
    }
    
    /**
     * Exports configuration to a specified file.
     */
    fun exportConfiguration(file: File): Boolean {
        return try {
            FileOutputStream(file).use { output ->
                properties.store(output, "QR Code Reader Configuration Export")
            }
            Logger.info(TAG, "Configuration exported to: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to export configuration", e)
            false
        }
    }
    
    /**
     * Imports configuration from a specified file.
     */
    fun importConfiguration(file: File): Boolean {
        return try {
            val tempProperties = Properties()
            FileInputStream(file).use { input ->
                tempProperties.load(input)
            }
            
            // Validate imported properties before applying
            properties.clear()
            properties.putAll(tempProperties)
            
            val issues = validateConfiguration()
            if (issues.isNotEmpty()) {
                Logger.warn(TAG, "Imported configuration has validation issues")
                return false
            }
            
            saveConfiguration()
            Logger.info(TAG, "Configuration imported from: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Logger.error(TAG, "Failed to import configuration", e)
            false
        }
    }
}
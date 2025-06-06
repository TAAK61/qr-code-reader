package qr.interfaces

import qr.QRCodeResult
import java.awt.image.BufferedImage

/**
 * Interface for QR code reading implementations.
 * 
 * This interface defines the contract for classes that can read and decode QR codes
 * from various sources like files or BufferedImage objects.
 */
interface QRCodeReaderInterface {
    
    /**
     * Reads a QR code from the specified file path.
     * 
     * @param filePath The path to the image file
     * @return The decoded QR code result
     * @throws Exception if reading fails
     */
    fun readQRCode(filePath: String): QRCodeResult
    
    /**
     * Reads a QR code from a BufferedImage.
     * 
     * @param image The image containing the QR code
     * @return The decoded QR code result
     * @throws Exception if reading fails
     */
    fun readQRCode(image: BufferedImage): QRCodeResult
}

/**
 * Interface for image processing implementations.
 * 
 * This interface defines methods for enhancing images to improve QR code detection.
 */
interface ImageProcessorInterface {
    
    /**
     * Processes an image to optimize it for QR code detection.
     * 
     * @param image The original image
     * @return The processed image
     */
    fun processImage(image: BufferedImage): BufferedImage
    
    /**
     * Attempts to decode a QR code from the processed image.
     * 
     * @param image The processed image
     * @return The decoded QR code result
     * @throws Exception if no QR code is found
     */
    fun decodeQRCode(image: BufferedImage): QRCodeResult
}

/**
 * Interface for image utility operations.
 * 
 * This interface defines common image manipulation operations.
 */
interface ImageUtilsInterface {
    
    /**
     * Converts an image to grayscale.
     * 
     * @param image The original image
     * @return The grayscale image
     */
    fun convertToGrayscale(image: BufferedImage): BufferedImage
    
    /**
     * Enhances the contrast of an image.
     * 
     * @param image The original image
     * @param factor The contrast enhancement factor
     * @return The contrast-enhanced image
     */
    fun enhanceContrast(image: BufferedImage, factor: Double = 1.5): BufferedImage
    
    /**
     * Reduces noise in an image.
     * 
     * @param image The original image
     * @return The noise-reduced image
     */
    fun reduceNoise(image: BufferedImage): BufferedImage
    
    /**
     * Resizes an image while maintaining aspect ratio.
     * 
     * @param image The original image
     * @param maxWidth Maximum width for the resized image
     * @param maxHeight Maximum height for the resized image
     * @return The resized image
     */
    fun resizeImage(image: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage
}

/**
 * Interface for configuration management.
 * 
 * This interface defines methods for managing application configuration.
 */
interface ConfigurationInterface {
    
    /**
     * Gets a string configuration value.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if key is not found
     * @return The configuration value
     */
    fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Gets an integer configuration value.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if key is not found
     * @return The configuration value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Gets a double configuration value.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if key is not found
     * @return The configuration value
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double
    
    /**
     * Gets a boolean configuration value.
     * 
     * @param key The configuration key
     * @param defaultValue The default value if key is not found
     * @return The configuration value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Sets a configuration value.
     * 
     * @param key The configuration key
     * @param value The value to set
     */
    fun setValue(key: String, value: Any)
}

/**
 * Interface for caching implementations.
 * 
 * This interface defines methods for caching processed images and results.
 */
interface CacheInterface<K, V> {
    
    /**
     * Stores a value in the cache.
     * 
     * @param key The cache key
     * @param value The value to cache
     */
    fun put(key: K, value: V)
    
    /**
     * Retrieves a value from the cache.
     * 
     * @param key The cache key
     * @return The cached value or null if not found
     */
    fun get(key: K): V?
    
    /**
     * Checks if a key exists in the cache.
     * 
     * @param key The cache key
     * @return True if the key exists, false otherwise
     */
    fun contains(key: K): Boolean
    
    /**
     * Removes a value from the cache.
     * 
     * @param key The cache key
     */
    fun remove(key: K)
    
    /**
     * Clears all cached values.
     */
    fun clear()
    
    /**
     * Gets the current cache size.
     * 
     * @return The number of cached items
     */
    fun size(): Int
}
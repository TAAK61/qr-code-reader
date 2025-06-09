package i18n

import java.text.MessageFormat
import java.util.*

/**
 * Internationalization manager for QR Code Reader
 * Implements Task 49: Add internationalization support for UI elements
 */
object I18nManager {
    
    private var currentLocale: Locale = Locale.getDefault()
    private var resourceBundle: ResourceBundle = loadBundle()
    
    /**
     * Available locales
     */
    val availableLocales = listOf(
        Locale.ENGLISH,
        Locale.FRENCH
    )
    
    /**
     * Get current locale
     */
    fun getCurrentLocale(): Locale = currentLocale
    
    /**
     * Set locale and reload bundle
     */
    fun setLocale(locale: Locale) {
        currentLocale = locale
        resourceBundle = loadBundle()
    }
    
    /**
     * Get localized message by key
     */
    fun getMessage(key: String, vararg args: Any): String {
        return try {
            val message = resourceBundle.getString(key)
            if (args.isNotEmpty()) {
                MessageFormat.format(message, *args)
            } else {
                message
            }
        } catch (e: MissingResourceException) {
            "!$key!" // Return key with markers if not found
        }
    }
    
    /**
     * Get localized message by key (shorter alias)
     */
    fun t(key: String, vararg args: Any): String = getMessage(key, *args)
    
    /**
     * Load resource bundle for current locale
     */
    private fun loadBundle(): ResourceBundle {
        return try {
            ResourceBundle.getBundle("i18n.messages", currentLocale)
        } catch (e: MissingResourceException) {
            // Fallback to English if locale not found
            ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH)
        }
    }
    
    /**
     * Get locale display name for UI
     */
    fun getLocaleDisplayName(locale: Locale): String {
        return when (locale.language) {
            "en" -> "English"
            "fr" -> "Français"
            else -> locale.displayName
        }
    }
    
    /**
     * Initialize with system locale or fallback to English
     */
    fun initialize() {
        val systemLocale = Locale.getDefault()
        val supportedLocale = availableLocales.find { 
            it.language == systemLocale.language 
        } ?: Locale.ENGLISH
        
        setLocale(supportedLocale)
    }
}

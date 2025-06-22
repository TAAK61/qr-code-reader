package qr

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Custom data format processor for QR codes
 * Task 79: Add support for custom data formats in QR codes
 */
object CustomDataProcessor {
    
    private val gson = Gson()
    
    data class CustomDataResult(
        val format: DataFormat,
        val isValid: Boolean,
        val data: Any?,
        val formattedDisplay: String,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    enum class DataFormat {
        JSON, XML, CSV, CALENDAR_EVENT, CONTACT_INFO, GEOLOCATION, 
        PRODUCT_INFO, PAYMENT_INFO, SOCIAL_MEDIA, PLAIN_TEXT, UNKNOWN
    }
    
    /**
     * Detect and parse custom data format
     */
    fun processCustomData(content: String): CustomDataResult {
        val trimmedContent = content.trim()
        
        return when {
            isJSON(trimmedContent) -> processJSON(trimmedContent)
            isXML(trimmedContent) -> processXML(trimmedContent)
            isCSV(trimmedContent) -> processCSV(trimmedContent)
            isCalendarEvent(trimmedContent) -> processCalendarEvent(trimmedContent)
            isContactInfo(trimmedContent) -> processContactInfo(trimmedContent)
            isGeoLocation(trimmedContent) -> processGeoLocation(trimmedContent)
            isProductInfo(trimmedContent) -> processProductInfo(trimmedContent)
            isPaymentInfo(trimmedContent) -> processPaymentInfo(trimmedContent)
            isSocialMedia(trimmedContent) -> processSocialMedia(trimmedContent)
            else -> processPlainText(trimmedContent)
        }
    }
    
    /**
     * JSON format detection and processing
     */
    private fun isJSON(content: String): Boolean {
        return content.startsWith("{") && content.endsWith("}") ||
               content.startsWith("[") && content.endsWith("]")
    }
    
    private fun processJSON(content: String): CustomDataResult {
        return try {
            val data = gson.fromJson(content, Map::class.java)
            CustomDataResult(
                format = DataFormat.JSON,
                isValid = true,
                data = data,
                formattedDisplay = formatJSONForDisplay(data),
                metadata = mapOf("jsonType" to detectJSONType(data as Map<String, Any>))
            )
        } catch (e: JsonSyntaxException) {            CustomDataResult(
                format = DataFormat.JSON,
                isValid = false,
                data = null,
                formattedDisplay = "Invalid JSON format: ${e.message}",
                metadata = mapOf("error" to (e.message ?: "Unknown JSON error"))
            )
        }
    }
    
    /**
     * XML format detection and processing
     */
    private fun isXML(content: String): Boolean {
        return content.trim().startsWith("<") && content.trim().endsWith(">") &&
               content.contains("</")
    }
    
    private fun processXML(content: String): CustomDataResult {
        return try {
            val rootElement = extractXMLRootElement(content)
            CustomDataResult(
                format = DataFormat.XML,
                isValid = true,
                data = content,
                formattedDisplay = formatXMLForDisplay(content),
                metadata = mapOf("rootElement" to (rootElement ?: "unknown"))
            )
        } catch (e: Exception) {            CustomDataResult(
                format = DataFormat.XML,
                isValid = false,
                data = null,
                formattedDisplay = "Invalid XML format: ${e.message}",
                metadata = mapOf("error" to (e.message ?: "Unknown XML error"))
            )
        }
    }
    
    /**
     * CSV format detection and processing
     */
    private fun isCSV(content: String): Boolean {
        val lines = content.lines()
        return lines.size > 1 && lines.any { it.contains(",") }
    }
    
    private fun processCSV(content: String): CustomDataResult {
        val lines = content.lines().filter { it.isNotEmpty() }
        val headers = lines.firstOrNull()?.split(",")?.map { it.trim() } ?: emptyList()
        val dataRows = lines.drop(1).map { it.split(",").map { cell -> cell.trim() } }
        
        return CustomDataResult(
            format = DataFormat.CSV,
            isValid = headers.isNotEmpty() && dataRows.isNotEmpty(),
            data = mapOf("headers" to headers, "rows" to dataRows),
            formattedDisplay = formatCSVForDisplay(headers, dataRows),
            metadata = mapOf("columns" to headers.size, "rows" to dataRows.size)
        )
    }
    
    /**
     * Calendar event format detection and processing
     */
    private fun isCalendarEvent(content: String): Boolean {
        return content.contains("BEGIN:VEVENT", ignoreCase = true) &&
               content.contains("END:VEVENT", ignoreCase = true)
    }
    
    private fun processCalendarEvent(content: String): CustomDataResult {
        val event = parseVEvent(content)
        return CustomDataResult(
            format = DataFormat.CALENDAR_EVENT,
            isValid = event.isNotEmpty(),
            data = event,
            formattedDisplay = formatCalendarEventForDisplay(event),
            metadata = mapOf("eventType" to "vEvent")
        )
    }
    
    /**
     * Contact info format detection and processing (non-vCard)
     */
    private fun isContactInfo(content: String): Boolean {
        val contactKeywords = listOf("name:", "phone:", "email:", "address:", "contact:")
        return contactKeywords.any { content.contains(it, ignoreCase = true) }
    }
    
    private fun processContactInfo(content: String): CustomDataResult {
        val contactData = parseCustomContactInfo(content)
        return CustomDataResult(
            format = DataFormat.CONTACT_INFO,
            isValid = contactData.isNotEmpty(),
            data = contactData,
            formattedDisplay = formatContactInfoForDisplay(contactData),
            metadata = mapOf("contactFields" to contactData.keys.size)
        )
    }
    
    /**
     * Geolocation format detection and processing
     */
    private fun isGeoLocation(content: String): Boolean {
        val geoPattern = Pattern.compile("^(?:geo:)?(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)")
        return geoPattern.matcher(content.trim()).find()
    }
    
    private fun processGeoLocation(content: String): CustomDataResult {
        val geoData = parseGeoLocation(content)
        return CustomDataResult(
            format = DataFormat.GEOLOCATION,
            isValid = geoData != null,
            data = geoData,
            formattedDisplay = formatGeoLocationForDisplay(geoData),
            metadata = if (geoData != null) mapOf("hasAltitude" to geoData.containsKey("altitude")) else emptyMap()
        )
    }
    
    /**
     * Product info format detection and processing
     */
    private fun isProductInfo(content: String): Boolean {
        val productKeywords = listOf("product:", "sku:", "barcode:", "price:", "item:")
        return productKeywords.any { content.contains(it, ignoreCase = true) }
    }
    
    private fun processProductInfo(content: String): CustomDataResult {
        val productData = parseProductInfo(content)
        return CustomDataResult(
            format = DataFormat.PRODUCT_INFO,
            isValid = productData.isNotEmpty(),
            data = productData,
            formattedDisplay = formatProductInfoForDisplay(productData),
            metadata = mapOf("productFields" to productData.keys.size)
        )
    }
    
    /**
     * Payment info format detection and processing
     */
    private fun isPaymentInfo(content: String): Boolean {
        return content.startsWith("bitcoin:", ignoreCase = true) ||
               content.startsWith("ethereum:", ignoreCase = true) ||
               content.contains("amount:", ignoreCase = true) ||
               content.contains("payment:", ignoreCase = true)
    }
    
    private fun processPaymentInfo(content: String): CustomDataResult {
        val paymentData = parsePaymentInfo(content)
        return CustomDataResult(
            format = DataFormat.PAYMENT_INFO,
            isValid = paymentData.isNotEmpty(),
            data = paymentData,
            formattedDisplay = formatPaymentInfoForDisplay(paymentData),
            metadata = mapOf("paymentType" to (paymentData["type"] ?: "unknown"))
        )
    }
    
    /**
     * Social media format detection and processing
     */
    private fun isSocialMedia(content: String): Boolean {
        val socialPlatforms = listOf("twitter.com", "facebook.com", "instagram.com", "linkedin.com", "tiktok.com")
        return socialPlatforms.any { content.contains(it, ignoreCase = true) } ||
               content.startsWith("@") && content.length > 1
    }
    
    private fun processSocialMedia(content: String): CustomDataResult {
        val socialData = parseSocialMediaInfo(content)
        return CustomDataResult(
            format = DataFormat.SOCIAL_MEDIA,
            isValid = socialData.isNotEmpty(),
            data = socialData,
            formattedDisplay = formatSocialMediaForDisplay(socialData),
            metadata = mapOf("platform" to (socialData["platform"] ?: "unknown"))
        )
    }
    
    /**
     * Plain text processing
     */
    private fun processPlainText(content: String): CustomDataResult {
        return CustomDataResult(
            format = DataFormat.PLAIN_TEXT,
            isValid = true,
            data = content,
            formattedDisplay = formatPlainTextForDisplay(content),
            metadata = mapOf(
                "length" to content.length,
                "lines" to content.lines().size,
                "words" to content.split("\\s+".toRegex()).size
            )
        )
    }
    
    // Helper methods for parsing different formats
    
    private fun detectJSONType(data: Map<*, *>): String {
        return when {
            data.containsKey("type") -> data["type"].toString()
            data.containsKey("event") -> "event"
            data.containsKey("contact") -> "contact"
            data.containsKey("product") -> "product"
            else -> "generic"
        }
    }
    
    private fun extractXMLRootElement(content: String): String {
        val startTag = content.indexOf('<')
        val endTag = content.indexOf('>', startTag)
        return if (startTag != -1 && endTag != -1) {
            content.substring(startTag + 1, endTag).split(" ")[0]
        } else "unknown"
    }
    
    private fun parseVEvent(content: String): Map<String, String> {
        val event = mutableMapOf<String, String>()
        val lines = content.lines()
        
        for (line in lines) {
            when {
                line.startsWith("SUMMARY:", ignoreCase = true) -> 
                    event["summary"] = line.substring(8)
                line.startsWith("DTSTART:", ignoreCase = true) -> 
                    event["startDate"] = line.substring(8)
                line.startsWith("DTEND:", ignoreCase = true) -> 
                    event["endDate"] = line.substring(6)
                line.startsWith("LOCATION:", ignoreCase = true) -> 
                    event["location"] = line.substring(9)
                line.startsWith("DESCRIPTION:", ignoreCase = true) -> 
                    event["description"] = line.substring(12)
            }
        }
        
        return event
    }
    
    private fun parseCustomContactInfo(content: String): Map<String, String> {
        val contact = mutableMapOf<String, String>()
        val lines = content.lines()
        
        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex != -1) {
                val key = line.substring(0, colonIndex).trim().lowercase()
                val value = line.substring(colonIndex + 1).trim()
                contact[key] = value
            }
        }
        
        return contact
    }
    
    private fun parseGeoLocation(content: String): Map<String, Any>? {
        val geoPattern = Pattern.compile("^(?:geo:)?(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)(?:,(-?\\d+\\.\\d+))?")
        val matcher = geoPattern.matcher(content.trim())
        
        return if (matcher.find()) {
            val result = mutableMapOf<String, Any>()
            result["latitude"] = matcher.group(1).toDouble()
            result["longitude"] = matcher.group(2).toDouble()
            matcher.group(3)?.let { result["altitude"] = it.toDouble() }
            result
        } else null
    }
    
    private fun parseProductInfo(content: String): Map<String, String> {
        val product = mutableMapOf<String, String>()
        val lines = content.lines()
        
        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex != -1) {
                val key = line.substring(0, colonIndex).trim().lowercase()
                val value = line.substring(colonIndex + 1).trim()
                product[key] = value
            }
        }
        
        return product
    }
    
    private fun parsePaymentInfo(content: String): Map<String, String> {
        val payment = mutableMapOf<String, String>()
        
        when {
            content.startsWith("bitcoin:", ignoreCase = true) -> {
                payment["type"] = "Bitcoin"
                payment["address"] = content.substring(8).split("?")[0]
                // Parse query parameters
                if (content.contains("?")) {
                    val params = content.substring(content.indexOf("?") + 1).split("&")
                    for (param in params) {
                        val parts = param.split("=")
                        if (parts.size == 2) {
                            payment[parts[0]] = parts[1]
                        }
                    }
                }
            }
            content.startsWith("ethereum:", ignoreCase = true) -> {
                payment["type"] = "Ethereum"
                payment["address"] = content.substring(9)
            }
            else -> {
                payment["type"] = "Generic"
                val lines = content.lines()
                for (line in lines) {
                    val colonIndex = line.indexOf(':')
                    if (colonIndex != -1) {
                        val key = line.substring(0, colonIndex).trim().lowercase()
                        val value = line.substring(colonIndex + 1).trim()
                        payment[key] = value
                    }
                }
            }
        }
        
        return payment
    }
    
    private fun parseSocialMediaInfo(content: String): Map<String, String> {
        val social = mutableMapOf<String, String>()
        
        when {
            content.contains("twitter.com") -> {
                social["platform"] = "Twitter"
                social["url"] = content
                val username = content.substringAfterLast("/")
                if (username.isNotEmpty()) social["username"] = username
            }
            content.contains("facebook.com") -> {
                social["platform"] = "Facebook"
                social["url"] = content
            }
            content.contains("instagram.com") -> {
                social["platform"] = "Instagram"
                social["url"] = content
                val username = content.substringAfterLast("/")
                if (username.isNotEmpty()) social["username"] = username
            }
            content.contains("linkedin.com") -> {
                social["platform"] = "LinkedIn"
                social["url"] = content
            }
            content.startsWith("@") -> {
                social["platform"] = "Generic"
                social["username"] = content.substring(1)
            }
        }
        
        return social
    }
    
    // Display formatting methods
      private fun formatJSONForDisplay(data: Map<*, *>): String {
        val builder = StringBuilder()
        builder.appendLine("📋 JSON Data")
        builder.appendLine("=".repeat(20))
        
        data.forEach { (key, value) ->
            builder.appendLine("$key: $value")
        }
        
        return builder.toString()
    }
    
    private fun formatXMLForDisplay(content: String): String {        return "📄 XML Document\n" +
               "=".repeat(20) + "\n" +
               content.take(500) + if (content.length > 500) "..." else ""
    }
      private fun formatCSVForDisplay(headers: List<String>, rows: List<List<String>>): String {
        val builder = StringBuilder()
        builder.appendLine("📊 CSV Data")
        builder.appendLine("=".repeat(20))
        builder.appendLine("Headers: ${headers.joinToString(", ")}")
        builder.appendLine("Rows: ${rows.size}")
        builder.appendLine()
        
        rows.take(5).forEach { row ->
            builder.appendLine(row.joinToString(" | "))
        }
        
        if (rows.size > 5) {
            builder.appendLine("... (${rows.size - 5} more rows)")
        }
        
        return builder.toString()
    }
      private fun formatCalendarEventForDisplay(event: Map<String, String>): String {
        val builder = StringBuilder()
        builder.appendLine("📅 Calendar Event")
        builder.appendLine("=".repeat(20))
        
        event["summary"]?.let { builder.appendLine("📌 Title: $it") }
        event["startDate"]?.let { builder.appendLine("🕐 Start: $it") }
        event["endDate"]?.let { builder.appendLine("🕑 End: $it") }
        event["location"]?.let { builder.appendLine("📍 Location: $it") }
        event["description"]?.let { builder.appendLine("📝 Description: $it") }
        
        return builder.toString()
    }
    
    private fun formatContactInfoForDisplay(contact: Map<String, String>): String {        val builder = StringBuilder()
        builder.appendLine("👤 Contact Information")
        builder.appendLine("=".repeat(25))
        
        contact.forEach { (key, value) ->
            val icon = when (key.lowercase()) {
                "name" -> "👤"
                "phone" -> "📞"
                "email" -> "✉️"
                "address" -> "🏠"
                else -> "📝"
            }
            builder.appendLine("$icon ${key.capitalize()}: $value")
        }
        
        return builder.toString()
    }
    
    private fun formatGeoLocationForDisplay(geoData: Map<String, Any>?): String {
        if (geoData == null) return "Invalid geolocation data"
          val builder = StringBuilder()
        builder.appendLine("🌍 Geolocation")
        builder.appendLine("=".repeat(15))
        builder.appendLine("📍 Latitude: ${geoData["latitude"]}")
        builder.appendLine("📍 Longitude: ${geoData["longitude"]}")
        geoData["altitude"]?.let { builder.appendLine("⛰️ Altitude: $it") }
        
        // Generate maps links
        val lat = geoData["latitude"]
        val lon = geoData["longitude"]
        builder.appendLine("\n🗺️ View on Maps:")
        builder.appendLine("Google Maps: https://maps.google.com/?q=$lat,$lon")
        builder.appendLine("OpenStreetMap: https://www.openstreetmap.org/?mlat=$lat&mlon=$lon")
        
        return builder.toString()
    }
      private fun formatProductInfoForDisplay(product: Map<String, String>): String {
        val builder = StringBuilder()
        builder.appendLine("🛍️ Product Information")
        builder.appendLine("=".repeat(25))
        
        product.forEach { (key, value) ->
            val icon = when (key.lowercase()) {
                "product", "name" -> "📦"
                "price" -> "💰"
                "sku" -> "🔢"
                "barcode" -> "📊"
                else -> "📝"
            }
            builder.appendLine("$icon ${key.capitalize()}: $value")
        }
        
        return builder.toString()
    }
      private fun formatPaymentInfoForDisplay(payment: Map<String, String>): String {
        val builder = StringBuilder()
        builder.appendLine("💳 Payment Information")
        builder.appendLine("=".repeat(25))
        
        payment.forEach { (key, value) ->
            val icon = when (key.lowercase()) {
                "type" -> "💳"
                "amount" -> "💰"
                "address" -> "🏦"
                "currency" -> "💱"
                else -> "📝"
            }
            builder.appendLine("$icon ${key.capitalize()}: $value")
        }
        
        return builder.toString()
    }
      private fun formatSocialMediaForDisplay(social: Map<String, String>): String {
        val builder = StringBuilder()
        builder.appendLine("📱 Social Media")
        builder.appendLine("=".repeat(15))
        
        social["platform"]?.let { 
            val icon = when (it.lowercase()) {
                "twitter" -> "🐦"
                "facebook" -> "📘"
                "instagram" -> "📷"
                "linkedin" -> "💼"
                else -> "📱"
            }
            builder.appendLine("$icon Platform: $it")
        }
        social["username"]?.let { builder.appendLine("👤 Username: $it") }
        social["url"]?.let { builder.appendLine("🔗 URL: $it") }
        
        return builder.toString()
    }
      private fun formatPlainTextForDisplay(content: String): String {
        val builder = StringBuilder()
        builder.appendLine("📄 Plain Text")
        builder.appendLine("=".repeat(15))
        builder.appendLine("Length: ${content.length} characters")
        builder.appendLine("Lines: ${content.lines().size}")
        builder.appendLine("Words: ${content.split("\\s+".toRegex()).size}")
        builder.appendLine()
        builder.appendLine("Content:")
        builder.appendLine(content.take(200) + if (content.length > 200) "..." else "")
        
        return builder.toString()
    }
}

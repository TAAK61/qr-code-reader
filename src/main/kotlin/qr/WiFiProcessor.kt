package qr

/**
 * Wi-Fi configuration processor for QR codes
 * Task 76: Implement Wi-Fi configuration from QR codes
 */
object WiFiProcessor {
    
    data class WiFiConfig(
        val ssid: String,
        val password: String? = null,
        val security: SecurityType = SecurityType.WPA,
        val hidden: Boolean = false
    ) {
        enum class SecurityType {
            NONE, WEP, WPA, WPA2, WPA3
        }
    }
    
    /**
     * Check if content is a Wi-Fi configuration
     */
    fun isWiFiConfig(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("WIFI:", ignoreCase = true)
    }
    
    /**
     * Parse Wi-Fi configuration from QR code content
     * Format: WIFI:T:WPA;S:MyNetworkName;P:MyPassword;H:false;;
     */
    fun parseWiFiConfig(content: String): WiFiConfig? {
        if (!isWiFiConfig(content)) return null
        
        val wifiData = content.removePrefix("WIFI:").removeSuffix(";;")
        val parameters = parseWiFiParameters(wifiData)
        
        val ssid = parameters["S"] ?: return null
        val password = parameters["P"]
        val securityStr = parameters["T"]?.uppercase() ?: "WPA"
        val hidden = parameters["H"]?.toBoolean() ?: false
          val security = when (securityStr) {
            "NONE", "NOPASS" -> WiFiConfig.SecurityType.NONE
            "WEP" -> WiFiConfig.SecurityType.WEP
            "WPA" -> WiFiConfig.SecurityType.WPA
            "WPA2" -> WiFiConfig.SecurityType.WPA2
            "WPA3" -> WiFiConfig.SecurityType.WPA3
            else -> WiFiConfig.SecurityType.WPA
        }
        
        return WiFiConfig(
            ssid = ssid,
            password = password,
            security = security,
            hidden = hidden
        )
    }
    
    /**
     * Generate Wi-Fi QR code content
     */
    fun generateWiFiQRContent(config: WiFiConfig): String {
        val securityStr = when (config.security) {
            WiFiConfig.SecurityType.NONE -> "nopass"
            WiFiConfig.SecurityType.WEP -> "WEP"
            WiFiConfig.SecurityType.WPA -> "WPA"
            WiFiConfig.SecurityType.WPA2 -> "WPA2"
            WiFiConfig.SecurityType.WPA3 -> "WPA3"
        }
        
        val escapedSSID = escapeWiFiValue(config.ssid)
        val escapedPassword = config.password?.let { escapeWiFiValue(it) } ?: ""
        
        return "WIFI:T:$securityStr;S:$escapedSSID;P:$escapedPassword;H:${config.hidden};;"
    }
    
    /**
     * Parse Wi-Fi parameters from the content
     */
    private fun parseWiFiParameters(content: String): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        val parts = content.split(";")
        
        for (part in parts) {
            if (part.contains(":")) {
                val keyValue = part.split(":", limit = 2)
                if (keyValue.size == 2) {
                    val key = keyValue[0].trim()
                    val value = unescapeWiFiValue(keyValue[1])
                    parameters[key] = value
                }
            }
        }
        
        return parameters
    }
    
    /**
     * Escape special characters in Wi-Fi values
     */
    private fun escapeWiFiValue(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace(":", "\\:")
    }
    
    /**
     * Unescape special characters in Wi-Fi values
     */
    private fun unescapeWiFiValue(value: String): String {
        return value
            .replace("\\\\", "\\")
            .replace("\\\"", "\"")
            .replace("\\,", ",")
            .replace("\\;", ";")
            .replace("\\:", ":")
    }
    
    /**
     * Format Wi-Fi configuration for human-readable display
     */
    fun formatWiFiForDisplay(config: WiFiConfig): String {
        val builder = StringBuilder()
          builder.appendLine("📶 Wi-Fi Configuration")
        builder.appendLine("=".repeat(25))
        builder.appendLine("🏷️  Network Name (SSID): ${config.ssid}")
        
        if (config.password != null && config.security != WiFiConfig.SecurityType.NONE) {
            builder.appendLine("🔐 Password: ${"*".repeat(config.password.length)}")
        }
        
        val securityIcon = when (config.security) {
            WiFiConfig.SecurityType.NONE -> "🔓"
            WiFiConfig.SecurityType.WEP -> "🔒"
            WiFiConfig.SecurityType.WPA -> "🔐"
            WiFiConfig.SecurityType.WPA2 -> "🛡️"
            WiFiConfig.SecurityType.WPA3 -> "🔰"
        }
        builder.appendLine("$securityIcon Security: ${config.security.name}")
        
        if (config.hidden) {
            builder.appendLine("👁️‍🗨️ Hidden Network: Yes")
        }
        
        builder.appendLine("\n📋 Instructions:")
        builder.appendLine("1. Go to Wi-Fi settings on your device")
        builder.appendLine("2. Connect to network: ${config.ssid}")
        if (config.password != null && config.security != WiFiConfig.SecurityType.NONE) {
            builder.appendLine("3. Enter the password when prompted")
        }
        if (config.hidden) {
            builder.appendLine("4. You may need to manually add this hidden network")
        }
        
        return builder.toString()
    }
    
    /**
     * Generate connection instructions for different platforms
     */
    fun generateConnectionInstructions(config: WiFiConfig): Map<String, String> {
        val instructions = mutableMapOf<String, String>()
        
        // Windows instructions
        instructions["Windows"] = buildString {
            appendLine("Windows Wi-Fi Connection:")
            appendLine("1. Click the Wi-Fi icon in the system tray")
            appendLine("2. Find and click '${config.ssid}'")
            if (config.password != null) {
                appendLine("3. Enter password and click 'Connect'")
            } else {
                appendLine("3. Click 'Connect'")
            }
            if (config.hidden) {
                appendLine("Note: For hidden networks, use 'Add a hidden network' option")
            }
        }
        
        // macOS instructions
        instructions["macOS"] = buildString {
            appendLine("macOS Wi-Fi Connection:")
            appendLine("1. Click the Wi-Fi icon in the menu bar")
            appendLine("2. Select '${config.ssid}' from the list")
            if (config.password != null) {
                appendLine("3. Enter password and click 'Join'")
            } else {
                appendLine("3. Click 'Join'")
            }
            if (config.hidden) {
                appendLine("Note: Use 'Join Other Network...' for hidden networks")
            }
        }
        
        // Android instructions
        instructions["Android"] = buildString {
            appendLine("Android Wi-Fi Connection:")
            appendLine("1. Go to Settings > Wi-Fi")
            appendLine("2. Tap '${config.ssid}' from available networks")
            if (config.password != null) {
                appendLine("3. Enter password and tap 'Connect'")
            } else {
                appendLine("3. Tap 'Connect'")
            }
            if (config.hidden) {
                appendLine("Note: Use 'Add network' for hidden networks")
            }
        }
        
        // iOS instructions
        instructions["iOS"] = buildString {
            appendLine("iOS Wi-Fi Connection:")
            appendLine("1. Go to Settings > Wi-Fi")
            appendLine("2. Tap '${config.ssid}' from the list")
            if (config.password != null) {
                appendLine("3. Enter password and tap 'Join'")
            } else {
                appendLine("3. Tap 'Join'")
            }
            if (config.hidden) {
                appendLine("Note: Use 'Other...' for hidden networks")
            }
        }
        
        return instructions
    }
    
    /**
     * Validate Wi-Fi configuration
     */
    fun validateWiFiConfig(config: WiFiConfig): List<String> {
        val issues = mutableListOf<String>()
        
        if (config.ssid.isEmpty()) {
            issues.add("SSID cannot be empty")
        }
        
        if (config.ssid.length > 32) {
            issues.add("SSID cannot be longer than 32 characters")
        }
        
        if (config.security != WiFiConfig.SecurityType.NONE && config.password.isNullOrEmpty()) {
            issues.add("Password is required for secured networks")
        }
        
        config.password?.let { password ->
            when (config.security) {
                WiFiConfig.SecurityType.WEP -> {
                    if (password.length != 5 && password.length != 13 && 
                        password.length != 10 && password.length != 26) {
                        issues.add("WEP password must be 5, 13, 10, or 26 characters")
                    }
                }
                WiFiConfig.SecurityType.WPA, WiFiConfig.SecurityType.WPA2, WiFiConfig.SecurityType.WPA3 -> {
                    if (password.length < 8 || password.length > 63) {
                        issues.add("WPA password must be between 8 and 63 characters")
                    }
                }
                WiFiConfig.SecurityType.NONE -> {
                    if (password.isNotEmpty()) {
                        issues.add("Open networks should not have a password")
                    }
                }
            }
        }
        
        return issues
    }
}

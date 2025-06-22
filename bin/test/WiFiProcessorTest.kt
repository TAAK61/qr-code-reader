package qr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Tests for WiFi configuration processing functionality
 * Task 76: Implement Wi-Fi configuration from QR codes
 */
class WiFiProcessorTest {
    
    private lateinit var processor: WiFiProcessor
    
    @BeforeEach
    fun setUp() {
        processor = WiFiProcessor
    }
    
    @Test
    fun `should detect WiFi content correctly`() {
        val wifiContent = "WIFI:T:WPA;S:MyNetwork;P:MyPassword;H:false;;"
        
        assertTrue(processor.isWiFiConfig(wifiContent))
        assertFalse(processor.isWiFiConfig("Not a WiFi config"))
        assertFalse(processor.isWiFiConfig("HTTP://example.com"))
    }
    
    @Test
    fun `should parse WPA WiFi config correctly`() {
        val wifiContent = "WIFI:T:WPA;S:MyNetwork;P:MyPassword123;H:false;;"
        
        val config = processor.parseWiFiConfig(wifiContent)
        
        assertNotNull(config)
        assertEquals("MyNetwork", config!!.ssid)
        assertEquals("MyPassword123", config.password)
        assertEquals(WiFiProcessor.WiFiConfig.SecurityType.WPA, config.security)
        assertFalse(config.hidden)
    }
    
    @Test
    fun `should parse open WiFi config correctly`() {
        val wifiContent = "WIFI:T:nopass;S:OpenNetwork;P:;H:false;;"
        
        val config = processor.parseWiFiConfig(wifiContent)
        
        assertNotNull(config)
        assertEquals("OpenNetwork", config!!.ssid)
        assertEquals("", config.password)
        assertEquals(WiFiProcessor.WiFiConfig.SecurityType.NONE, config.security)
        assertFalse(config.hidden)
    }
    
    @Test
    fun `should parse hidden network correctly`() {
        val wifiContent = "WIFI:T:WPA2;S:HiddenNet;P:SecretPass;H:true;;"
        
        val config = processor.parseWiFiConfig(wifiContent)
        
        assertNotNull(config)
        assertEquals("HiddenNet", config!!.ssid)
        assertEquals("SecretPass", config.password)
        assertEquals(WiFiProcessor.WiFiConfig.SecurityType.WPA2, config.security)
        assertTrue(config.hidden)
    }
    
    @Test
    fun `should generate WiFi QR content correctly`() {
        val config = WiFiProcessor.WiFiConfig(
            ssid = "TestNetwork",
            password = "TestPass123",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA2,
            hidden = false
        )
        
        val generated = processor.generateWiFiQRContent(config)
        
        assertEquals("WIFI:T:WPA2;S:TestNetwork;P:TestPass123;H:false;;", generated)
    }
    
    @Test
    fun `should handle special characters in SSID and password`() {
        val config = WiFiProcessor.WiFiConfig(
            ssid = "Test;Network,With:Special\"Chars\\",
            password = "Pass;Word,With:Special\"Chars\\",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA,
            hidden = false
        )
        
        val generated = processor.generateWiFiQRContent(config)
        assertTrue(generated.contains("\\;"))
        assertTrue(generated.contains("\\,"))
        assertTrue(generated.contains("\\:"))
        assertTrue(generated.contains("\\\""))
        assertTrue(generated.contains("\\\\"))
    }
    
    @Test
    fun `should format WiFi for display correctly`() {
        val config = WiFiProcessor.WiFiConfig(
            ssid = "TestNetwork",
            password = "TestPassword",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA2,
            hidden = true
        )
        
        val formatted = processor.formatWiFiForDisplay(config)
        
        assertTrue(formatted.contains("Wi-Fi Configuration"))
        assertTrue(formatted.contains("TestNetwork"))
        assertTrue(formatted.contains("WPA2"))
        assertTrue(formatted.contains("Hidden Network: Yes"))
        assertTrue(formatted.contains("Instructions:"))
    }
    
    @Test
    fun `should validate WiFi config correctly`() {
        // Valid config
        val validConfig = WiFiProcessor.WiFiConfig(
            ssid = "ValidNetwork",
            password = "ValidPassword123",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA2
        )
        assertTrue(processor.validateWiFiConfig(validConfig).isEmpty())
        
        // Empty SSID
        val emptySSID = WiFiProcessor.WiFiConfig(
            ssid = "",
            password = "password",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA
        )
        val issues = processor.validateWiFiConfig(emptySSID)
        assertTrue(issues.any { it.contains("SSID cannot be empty") })
        
        // Missing password for secured network
        val missingPassword = WiFiProcessor.WiFiConfig(
            ssid = "Network",
            password = null,
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA
        )
        val passwordIssues = processor.validateWiFiConfig(missingPassword)
        assertTrue(passwordIssues.any { it.contains("Password is required") })
    }
    
    @Test
    fun `should generate connection instructions for different platforms`() {
        val config = WiFiProcessor.WiFiConfig(
            ssid = "TestNetwork",
            password = "TestPassword",
            security = WiFiProcessor.WiFiConfig.SecurityType.WPA2
        )
        
        val instructions = processor.generateConnectionInstructions(config)
        
        assertTrue(instructions.containsKey("Windows"))
        assertTrue(instructions.containsKey("macOS"))
        assertTrue(instructions.containsKey("Android"))
        assertTrue(instructions.containsKey("iOS"))
        
        assertTrue(instructions["Windows"]!!.contains("TestNetwork"))
        assertTrue(instructions["Android"]!!.contains("Settings > Wi-Fi"))
    }
}

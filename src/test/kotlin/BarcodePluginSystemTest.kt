package barcode

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Unit tests for the barcode plugin system.
 * 
 * Tests the plugin manager, individual plugins, and the unified barcode reader.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BarcodePluginSystemTest {
    
    private lateinit var pluginManager: BarcodePluginManager
    private lateinit var unifiedReader: UnifiedBarcodeReader
      @BeforeAll
    fun setup() {
        // Reset singleton for clean test state
        BarcodePluginManager.resetInstance()
        
        pluginManager = BarcodePluginManager.getInstance()
        
        // Initialize with test plugins
        pluginManager.registerPlugin(QRCodePlugin())
        pluginManager.registerPlugin(DataMatrixPlugin())
        pluginManager.registerPlugin(Code128Plugin())
        
        unifiedReader = UnifiedBarcodeReader()
    }
    
    @AfterAll
    fun cleanup() {
        BarcodePluginManager.resetInstance()
    }
    
    @Nested
    @DisplayName("Plugin Manager Tests")
    inner class PluginManagerTests {
        
        @Test
        @DisplayName("Should register plugins successfully")
        fun shouldRegisterPlugins() {
            val registeredPlugins = pluginManager.getRegisteredPlugins()
            assertTrue(registeredPlugins.size >= 3)
            
            val pluginNames = registeredPlugins.map { it.name }
            assertTrue(pluginNames.contains("QR Code Reader"))
            assertTrue(pluginNames.contains("Data Matrix Reader"))
            assertTrue(pluginNames.contains("Code 128 Reader"))
        }
        
        @Test
        @DisplayName("Should get supported formats")
        fun shouldGetSupportedFormats() {
            val supportedFormats = pluginManager.getSupportedFormats()
            assertTrue(supportedFormats.contains(BarcodeFormat.QR_CODE))
            assertTrue(supportedFormats.contains(BarcodeFormat.DATA_MATRIX))
            assertTrue(supportedFormats.contains(BarcodeFormat.CODE_128))
        }
        
        @Test
        @DisplayName("Should get plugins for specific format")
        fun shouldGetPluginsForFormat() {
            val qrPlugins = pluginManager.getPluginsForFormat(BarcodeFormat.QR_CODE)
            assertEquals(1, qrPlugins.size)
            assertEquals("QR Code Reader", qrPlugins[0].name)
        }
        
        @Test
        @DisplayName("Should prevent duplicate plugin registration")
        fun shouldPreventDuplicateRegistration() {
            assertThrows<IllegalArgumentException> {
                pluginManager.registerPlugin(QRCodePlugin())
            }
        }
        
        @Test
        @DisplayName("Should provide plugin statistics")
        fun shouldProvideStatistics() {
            val stats = pluginManager.getPluginStatistics()
            assertTrue(stats.containsKey("totalPlugins"))
            assertTrue(stats.containsKey("supportedFormats"))
            assertTrue(stats.containsKey("plugins"))
            
            val totalPlugins = stats["totalPlugins"] as Int
            assertTrue(totalPlugins >= 3)
        }
    }
    
    @Nested
    @DisplayName("QR Code Plugin Tests")
    inner class QRCodePluginTests {
        
        private lateinit var qrPlugin: QRCodePlugin
        
        @BeforeEach
        fun setupPlugin() {
            qrPlugin = QRCodePlugin()
            qrPlugin.initialize()
        }
        
        @AfterEach
        fun cleanupPlugin() {
            qrPlugin.shutdown()
        }
        
        @Test
        @DisplayName("Should initialize successfully")
        fun shouldInitialize() {
            val config = qrPlugin.getConfiguration()
            assertTrue(config["initialized"] as Boolean)
            assertEquals("QR Code Reader", config["name"])
            assertEquals("1.0.0", config["version"])
        }
        
        @Test
        @DisplayName("Should handle valid QR code image")
        fun shouldHandleValidQRCode() {
            // Create a simple test image (this would need an actual QR code image in real tests)
            val testImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
            
            // Note: This test would need actual QR code images to be meaningful
            // For now, we just test that the method doesn't crash
            assertDoesNotThrow {
                qrPlugin.canHandle(testImage)
            }
        }
        
        @Test
        @DisplayName("Should throw exception when not initialized")
        fun shouldThrowWhenNotInitialized() {
            val uninitializedPlugin = QRCodePlugin()
            val testImage = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
            
            assertThrows<BarcodeDecodingException> {
                uninitializedPlugin.decode(testImage)
            }
        }
        
        @Test
        @DisplayName("Should have correct supported formats")
        fun shouldHaveCorrectSupportedFormats() {
            assertEquals(listOf(BarcodeFormat.QR_CODE), qrPlugin.supportedFormats)
        }
        
        @Test
        @DisplayName("Should have correct priority")
        fun shouldHaveCorrectPriority() {
            assertEquals(100, qrPlugin.priority)
        }
    }
    
    @Nested
    @DisplayName("Unified Barcode Reader Tests")
    inner class UnifiedBarcodeReaderTests {
        
        @Test
        @DisplayName("Should return supported formats")
        fun shouldReturnSupportedFormats() {
            val formats = unifiedReader.getSupportedFormats()
            assertTrue(formats.isNotEmpty())
            assertTrue(formats.contains(BarcodeFormat.QR_CODE))
        }
        
        @Test
        @DisplayName("Should provide plugin information")
        fun shouldProvidePluginInfo() {
            val info = unifiedReader.getPluginInfo()
            assertTrue(info.containsKey("totalPlugins"))
            assertTrue(info.containsKey("supportedFormats"))
            
            val totalPlugins = info["totalPlugins"] as Int
            assertTrue(totalPlugins > 0)
        }
        
        @Test
        @DisplayName("Should handle non-existent file")
        fun shouldHandleNonExistentFile() {
            assertThrows<IllegalArgumentException> {
                unifiedReader.readBarcode("non-existent-file.png")
            }
        }
        
        @Test
        @DisplayName("Should detect formats without throwing")
        fun shouldDetectFormatsWithoutThrowing() {
            // Test with existing test images if available
            val testImagesDir = File("test-images")
            if (testImagesDir.exists()) {
                val imageFiles = testImagesDir.listFiles { file -> 
                    file.extension.lowercase() in listOf("png", "jpg", "jpeg")
                }
                
                imageFiles?.firstOrNull()?.let { imageFile ->
                    assertDoesNotThrow {
                        unifiedReader.detectFormats(imageFile.absolutePath)
                    }
                }
            }
        }
    }
    
    @Nested
    @DisplayName("Barcode Format Tests")
    inner class BarcodeFormatTests {
        
        @Test
        @DisplayName("Should create barcode formats correctly")
        fun shouldCreateFormatsCorrectly() {
            assertEquals("QR Code", BarcodeFormat.QR_CODE.displayName)
            assertEquals("Data Matrix", BarcodeFormat.DATA_MATRIX.displayName)
            assertEquals("Code 128", BarcodeFormat.CODE_128.displayName)
        }
        
        @Test
        @DisplayName("Should parse format from string")
        fun shouldParseFormatFromString() {
            assertEquals(BarcodeFormat.QR_CODE, BarcodeFormat.fromString("QR_CODE"))
            assertEquals(BarcodeFormat.DATA_MATRIX, BarcodeFormat.fromString("DATA_MATRIX"))
            assertNull(BarcodeFormat.fromString("INVALID_FORMAT"))
        }
    }
    
    @Nested
    @DisplayName("Barcode Result Tests")
    inner class BarcodeResultTests {
        
        @Test
        @DisplayName("Should create barcode result correctly")
        fun shouldCreateResultCorrectly() {
            val result = BarcodeResult(
                content = "Test content",
                format = BarcodeFormat.QR_CODE,
                confidence = 0.95,
                metadata = mapOf("test" to "value"),
                processingTimeMs = 100
            )
            
            assertEquals("Test content", result.content)
            assertEquals(BarcodeFormat.QR_CODE, result.format)
            assertEquals(0.95, result.confidence)
            assertEquals(100, result.processingTimeMs)
            assertTrue(result.metadata.containsKey("test"))
        }
        
        @Test
        @DisplayName("Should handle equality correctly")
        fun shouldHandleEqualityCorrectly() {
            val result1 = BarcodeResult("test", BarcodeFormat.QR_CODE)
            val result2 = BarcodeResult("test", BarcodeFormat.QR_CODE)
            val result3 = BarcodeResult("different", BarcodeFormat.QR_CODE)
            
            assertEquals(result1, result2)
            assertNotEquals(result1, result3)
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {
          @Test
        @DisplayName("Should integrate with existing QRCodeReader")
        fun shouldIntegrateWithExistingQRCodeReader() {
            val qrReader = qr.QRCodeReader()
            
            // Enable plugin system integration
            qrReader.enablePluginSystem()
            
            // Test that the enhanced methods are available
            assertDoesNotThrow {
                qrReader.getSupportedFormats()
                qrReader.getPluginInfo()
            }
            
            val supportedFormats = qrReader.getSupportedFormats()
            assertTrue(supportedFormats.isNotEmpty())
        }
        
        @Test
        @DisplayName("Should maintain backward compatibility")
        fun shouldMaintainBackwardCompatibility() {
            val qrReader = qr.QRCodeReader()
            
            // Test that existing methods still work (with test image if available)
            val testImagesDir = File("test-images")
            if (testImagesDir.exists()) {
                val qrImages = testImagesDir.listFiles { file ->
                    file.name.contains("qr") && file.extension.lowercase() in listOf("png", "jpg", "jpeg")
                }
                
                qrImages?.firstOrNull()?.let { qrImage ->
                    assertDoesNotThrow {
                        qrReader.readQRCode(qrImage.absolutePath)
                    }
                }
            }
        }
    }
}

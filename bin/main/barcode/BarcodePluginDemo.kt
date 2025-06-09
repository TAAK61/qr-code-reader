package barcode

import java.io.File

/**
 * Demo class to showcase the barcode plugin system.
 * 
 * This class demonstrates how to use the plugin system to read different
 * types of barcodes and how to extend the system with new plugins.
 * 
 * Task 25: Implement a plugin system for supporting different barcode formats
 */
class BarcodePluginDemo {
    
    private val unifiedReader = UnifiedBarcodeReader()
    
    fun runDemo() {
        println("=".repeat(60))
        println("         BARCODE PLUGIN SYSTEM DEMO")
        println("=".repeat(60))
        println()
        
        // Show plugin information
        showPluginInfo()
        
        // Show supported formats
        showSupportedFormats()
        
        // Demonstrate barcode reading
        demonstrateBarcodeReading()
        
        // Show usage examples
        showUsageExamples()
        
        println("\n" + "=".repeat(60))
        println("         DEMO COMPLETED")
        println("=".repeat(60))
    }
    
    private fun showPluginInfo() {
        println("📋 REGISTERED PLUGINS")
        println("-".repeat(40))
        
        val pluginInfo = unifiedReader.getPluginInfo()
        val totalPlugins = pluginInfo["totalPlugins"] as Int
        val supportedFormats = pluginInfo["supportedFormats"] as Int
        
        println("Total Plugins: $totalPlugins")
        println("Supported Formats: $supportedFormats")
        println()
        
        @Suppress("UNCHECKED_CAST")
        val plugins = pluginInfo["plugins"] as List<Map<String, Any>>
        
        plugins.forEach { plugin ->
            val name = plugin["name"] as String
            val version = plugin["version"] as String
            val priority = plugin["priority"] as Int
            @Suppress("UNCHECKED_CAST")
            val formats = plugin["supportedFormats"] as List<String>
            
            println("🔌 $name v$version")
            println("   Priority: $priority")
            println("   Formats: ${formats.joinToString(", ")}")
            println()
        }
    }
    
    private fun showSupportedFormats() {
        println("📊 SUPPORTED BARCODE FORMATS")
        println("-".repeat(40))
        
        val formats = unifiedReader.getSupportedFormats()
        formats.forEach { format ->
            println("✅ ${format.displayName} - ${format.description}")
        }
        println()
    }
    
    private fun demonstrateBarcodeReading() {
        println("🔍 BARCODE READING DEMONSTRATION")
        println("-".repeat(40))
        
        val testImagesDir = File("test-images")
        if (!testImagesDir.exists()) {
            println("❌ Test images directory not found: test-images/")
            println("   Please create test QR code images to see the demo in action.")
            return
        }
        
        val imageFiles = testImagesDir.listFiles { file ->
            file.extension.lowercase() in listOf("png", "jpg", "jpeg", "gif", "bmp")
        }
        
        if (imageFiles.isNullOrEmpty()) {
            println("❌ No image files found in test-images/")
            return
        }
        
        println("Found ${imageFiles.size} test images:")
        println()
        
        imageFiles.take(5).forEach { imageFile ->
            println("📄 Processing: ${imageFile.name}")
            
            try {
                // Detect possible formats first
                val possibleFormats = unifiedReader.detectFormats(imageFile.absolutePath)
                if (possibleFormats.isNotEmpty()) {
                    println("   Detected formats: ${possibleFormats.map { it.displayName }.joinToString(", ")}")
                }
                
                // Try to read the barcode
                val result = unifiedReader.readBarcodeDetailed(imageFile.absolutePath)
                
                println("   ✅ SUCCESS!")
                println("   Format: ${result.format.displayName}")
                println("   Content: ${result.content.take(100)}${if (result.content.length > 100) "..." else ""}")
                println("   Processing Time: ${result.processingTimeMs}ms")
                println("   Confidence: ${(result.confidence * 100).toInt()}%")
                
            } catch (e: BarcodeNotFoundException) {
                println("   ⚠️  No barcode found")
            } catch (e: Exception) {
                println("   ❌ Error: ${e.message}")
            }
            
            println()
        }
    }
    
    private fun showUsageExamples() {
        println("💡 USAGE EXAMPLES")
        println("-".repeat(40))
        
        println("1. Reading any barcode:")
        println("   val reader = UnifiedBarcodeReader()")
        println("   val result = reader.readBarcode(\"image.png\")")
        println("   println(\"Content: \${result.content}\")")
        println()
        
        println("2. Reading with format preference:")
        println("   val result = reader.readBarcode(\"image.png\", BarcodeFormat.QR_CODE)")
        println()
        
        println("3. Backward compatibility with QRCodeReader:")
        println("   val qrReader = QRCodeReader()")
        println("   val content = qrReader.readQRCode(\"qr-image.png\")")
        println()
        
        println("4. Detecting barcode formats:")
        println("   val formats = reader.detectFormats(\"image.png\")")
        println("   println(\"Possible formats: \$formats\")")
        println()
        
        println("5. Getting detailed results:")
        println("   val detailed = reader.readBarcodeDetailed(\"image.png\")")
        println("   println(\"Processing time: \${detailed.processingTimeMs}ms\")")
        println()
        
        println("6. Adding custom plugins:")
        println("   class MyBarcodePlugin : BarcodePlugin { ... }")
        println("   reader.registerPlugin(MyBarcodePlugin())")
        println()
    }
    
    /**
     * Test the plugin system with a specific image
     */
    fun testWithImage(imagePath: String) {
        println("🧪 TESTING WITH IMAGE: $imagePath")
        println("-".repeat(50))
        
        if (!File(imagePath).exists()) {
            println("❌ File not found: $imagePath")
            return
        }
        
        try {
            // Detect formats
            val formats = unifiedReader.detectFormats(imagePath)
            println("Detected formats: ${formats.map { it.displayName }}")
            
            // Read barcode
            val result = unifiedReader.readBarcodeDetailed(imagePath)
            
            println("✅ Successfully decoded!")
            println("Format: ${result.format.displayName}")
            println("Content: ${result.content}")
            println("Confidence: ${(result.confidence * 100).toInt()}%")
            println("Processing time: ${result.processingTimeMs}ms")
            
            if (result.metadata.isNotEmpty()) {
                println("Metadata:")
                result.metadata.forEach { (key, value) ->
                    println("  $key: $value")
                }
            }
            
        } catch (e: BarcodeNotFoundException) {
            println("⚠️  No barcode found in image")
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    
    /**
     * Performance test with multiple images
     */
    fun performanceTest() {
        println("⚡ PERFORMANCE TEST")
        println("-".repeat(40))
        
        val testImagesDir = File("test-images")
        if (!testImagesDir.exists()) {
            println("❌ Test images directory not found")
            return
        }
        
        val imageFiles = testImagesDir.listFiles { file ->
            file.extension.lowercase() in listOf("png", "jpg", "jpeg")
        }
        
        if (imageFiles.isNullOrEmpty()) {
            println("❌ No test images found")
            return
        }
        
        val totalImages = imageFiles.size
        var successCount = 0
        var totalTime = 0L
        
        println("Testing with $totalImages images...")
        
        imageFiles.forEach { imageFile ->
            try {
                val result = unifiedReader.readBarcode(imageFile.absolutePath)
                successCount++
                totalTime += result.processingTimeMs
            } catch (e: Exception) {
                // Count failures but continue
            }
        }
        
        println("Results:")
        println("  Total images: $totalImages")
        println("  Successful reads: $successCount")
        println("  Success rate: ${(successCount * 100.0 / totalImages).toInt()}%")
        println("  Average processing time: ${totalTime / maxOf(successCount, 1)}ms")
        println()
    }
}

/**
 * Main function to run the demo
 */
fun main() {
    val demo = BarcodePluginDemo()
    demo.runDemo()
    
    // Run performance test if requested
    if (System.getProperty("performance.test") == "true") {
        demo.performanceTest()
    }
    
    // Test with specific image if provided
    System.getProperty("test.image")?.let { imagePath ->
        demo.testWithImage(imagePath)
    }
}

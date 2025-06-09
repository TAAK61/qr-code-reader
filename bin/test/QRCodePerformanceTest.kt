package qr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis
import kotlin.system.measureNanoTime
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("QR Code Reader Performance Tests")
class QRCodePerformanceTest {

    private lateinit var reader: QRCodeReader
    private val testImagesDir = File("test-images")
    private val performanceResults = mutableMapOf<String, MutableList<Long>>()

    @BeforeEach
    fun setUp() {
        reader = QRCodeReader()
        // Clear previous results for this test run
        performanceResults.clear()
    }

    @AfterEach
    fun tearDown() {
        // Print performance summary after each test method
        printPerformanceSummary()
    }

    @Nested
    @DisplayName("Image Loading Performance")
    inner class ImageLoadingPerformance {

        @Test
        @DisplayName("Measure image loading time for different file sizes")
        fun testImageLoadingTime() {
            val imageFiles = testImagesDir.listFiles { file -> 
                file.extension.lowercase() in listOf("png", "jpg", "jpeg", "gif")
            } ?: emptyArray()

            imageFiles.forEach { imageFile ->
                val fileSize = imageFile.length()
                val loadTime = measureTimeMillis {
                    val image = ImageIO.read(imageFile)
                    assertNotNull(image, "Image should be loaded successfully: ${imageFile.name}")
                }

                recordPerformanceMetric("image_loading_${fileSize}bytes", loadTime)
                println("Loaded ${imageFile.name} (${fileSize} bytes) in ${loadTime}ms")

                // Performance assertion: images should load within reasonable time
                assertTrue(loadTime < 1000, "Image loading should complete within 1 second for ${imageFile.name}")
            }
        }

        @ParameterizedTest
        @ValueSource(strings = ["qr-hello-world.png", "qr-long-text.png", "qr-url.png"])
        @DisplayName("Compare loading times across different QR code images")
        fun testLoadingTimeComparison(fileName: String) {
            val imageFile = File(testImagesDir, fileName)
            if (!imageFile.exists()) {
                println("Skipping test for missing file: $fileName")
                return
            }

            val loadTimes = mutableListOf<Long>()
            repeat(10) {
                val loadTime = measureTimeMillis {
                    ImageIO.read(imageFile)
                }
                loadTimes.add(loadTime)
            }

            val avgLoadTime = loadTimes.average()
            val minLoadTime = loadTimes.minOrNull() ?: 0L
            val maxLoadTime = loadTimes.maxOrNull() ?: 0L

            recordPerformanceMetric("avg_loading_$fileName", avgLoadTime.toLong())
            
            println("$fileName - Avg: ${avgLoadTime}ms, Min: ${minLoadTime}ms, Max: ${maxLoadTime}ms")
            assertTrue(avgLoadTime < 500, "Average loading time should be under 500ms for $fileName")
        }
    }

    @Nested
    @DisplayName("QR Code Processing Performance")
    inner class QRCodeProcessingPerformance {

        @Test
        @DisplayName("Measure QR code detection and decoding time")
        fun testQRCodeProcessingTime() {
            val imageFiles = testImagesDir.listFiles { file -> 
                file.name.startsWith("qr-") && file.extension.lowercase() in listOf("png", "jpg", "jpeg")
            } ?: emptyArray()

            imageFiles.forEach { imageFile ->
                val processingTime = measureTimeMillis {
                    val result = reader.readQRCode(imageFile.absolutePath)
                    assertNotNull(result, "QR code should be detected in ${imageFile.name}")
                }

                recordPerformanceMetric("qr_processing_${imageFile.name}", processingTime)
                println("Processed ${imageFile.name} in ${processingTime}ms")

                // Performance assertion: QR code processing should be fast
                assertTrue(processingTime < 2000, "QR code processing should complete within 2 seconds for ${imageFile.name}")
            }
        }

        @RepeatedTest(5)
        @DisplayName("Consistency test for QR code processing time")
        fun testProcessingTimeConsistency() {
            val testFile = File(testImagesDir, "qr-hello-world.png")
            if (!testFile.exists()) {
                println("Skipping consistency test - test file not found")
                return
            }

            val processingTime = measureTimeMillis {
                reader.readQRCode(testFile.absolutePath)
            }

            recordPerformanceMetric("consistency_test", processingTime)
            assertTrue(processingTime < 1500, "Processing time should be consistent and under 1.5 seconds")
        }
    }

    @Nested
    @DisplayName("Memory Usage Performance")
    inner class MemoryUsagePerformance {

        @Test
        @DisplayName("Monitor memory usage during image processing")
        fun testMemoryUsage() {
            val runtime = Runtime.getRuntime()
            
            // Force garbage collection and get baseline
            System.gc()
            Thread.sleep(100)
            val baselineMemory = runtime.totalMemory() - runtime.freeMemory()

            val imageFiles = testImagesDir.listFiles { file -> 
                file.name.startsWith("qr-") && file.extension.lowercase() in listOf("png", "jpg", "jpeg")
            } ?: emptyArray()

            var maxMemoryUsage = baselineMemory
            
            imageFiles.forEach { imageFile ->
                val beforeMemory = runtime.totalMemory() - runtime.freeMemory()
                
                reader.readQRCode(imageFile.absolutePath)
                  val afterMemory = runtime.totalMemory() - runtime.freeMemory()
                val memoryUsed = afterMemory - beforeMemory
                maxMemoryUsage = maxOf(maxMemoryUsage, afterMemory)

                recordPerformanceMetric("memory_usage_${imageFile.name}", memoryUsed)
                println("Memory used for ${imageFile.name}: ${memoryUsed / 1024}KB")
            }
            
            val totalMemoryIncrease = maxMemoryUsage - baselineMemory
            println("Total memory increase: ${totalMemoryIncrease / 1024}KB")

            // Memory usage should be reasonable (less than 100MB for QR code processing with fallback strategies)
            assertTrue(totalMemoryIncrease < 100 * 1024 * 1024, "Memory usage should be reasonable, was ${totalMemoryIncrease / 1024 / 1024}MB")
        }
    }

    @Nested
    @DisplayName("Throughput Performance")
    inner class ThroughputPerformance {

        @Test
        @DisplayName("Measure QR code processing throughput")
        fun testProcessingThroughput() {
            val testFile = File(testImagesDir, "qr-hello-world.png")
            if (!testFile.exists()) {
                println("Skipping throughput test - test file not found")
                return
            }

            val iterations = 50
            val totalTime = measureTimeMillis {
                repeat(iterations) {
                    reader.readQRCode(testFile.absolutePath)
                }
            }

            val throughput = (iterations * 1000.0) / totalTime // operations per second
            recordPerformanceMetric("throughput_ops_per_second", throughput.toLong())
            
            println("Processed $iterations QR codes in ${totalTime}ms")
            println("Throughput: ${String.format("%.2f", throughput)} operations/second")

            // Should be able to process at least 10 QR codes per second
            assertTrue(throughput >= 10.0, "Throughput should be at least 10 operations per second")
        }

        @Test
        @DisplayName("Measure batch processing performance")
        fun testBatchProcessingPerformance() {
            val imageFiles = testImagesDir.listFiles { file -> 
                file.name.startsWith("qr-") && file.extension.lowercase() in listOf("png", "jpg", "jpeg")
            } ?: emptyArray()

            if (imageFiles.isEmpty()) {
                println("Skipping batch processing test - no test files found")
                return
            }

            val batchTime = measureTimeMillis {
                imageFiles.forEach { imageFile ->
                    reader.readQRCode(imageFile.absolutePath)
                }
            }

            val averageTimePerImage = batchTime.toDouble() / imageFiles.size
            recordPerformanceMetric("batch_avg_per_image", averageTimePerImage.toLong())
            
            println("Batch processed ${imageFiles.size} images in ${batchTime}ms")
            println("Average time per image: ${String.format("%.2f", averageTimePerImage)}ms")

            assertTrue(averageTimePerImage < 1000, "Average processing time per image should be under 1 second")
        }
    }

    @Nested
    @DisplayName("Micro-benchmarks")
    inner class MicroBenchmarks {

        @Test
        @DisplayName("Nano-precision timing for critical operations")
        fun testMicroBenchmarks() {
            val testFile = File(testImagesDir, "qr-hello-world.png")
            if (!testFile.exists()) {
                println("Skipping micro-benchmark - test file not found")
                return
            }

            // Image loading micro-benchmark
            val imageLoadTime = measureNanoTime {
                ImageIO.read(testFile)
            }
            recordPerformanceMetric("micro_image_load_nanos", imageLoadTime / 1000) // Convert to microseconds

            // QR code detection micro-benchmark
            val qrDetectionTime = measureNanoTime {
                reader.readQRCode(testFile.absolutePath)
            }
            recordPerformanceMetric("micro_qr_detection_nanos", qrDetectionTime / 1000) // Convert to microseconds

            println("Image loading: ${imageLoadTime / 1000}μs")
            println("QR detection: ${qrDetectionTime / 1000}μs")

            // Micro-benchmarks should complete in reasonable time
            assertTrue(imageLoadTime < 100_000_000, "Image loading should complete within 100ms") // 100ms in nanoseconds
            assertTrue(qrDetectionTime < 1_000_000_000, "QR detection should complete within 1 second") // 1s in nanoseconds
        }
    }

    @Test
    @DisplayName("Performance regression test")
    fun testPerformanceRegression() {
        val testFile = File(testImagesDir, "qr-hello-world.png")
        if (!testFile.exists()) {
            println("Skipping regression test - test file not found")
            return
        }

        // Baseline measurements (simulate expected performance)
        val expectedMaxProcessingTime = 1000L // 1 second
        val expectedMinThroughput = 5.0 // 5 operations per second

        val processingTime = measureTimeMillis {
            reader.readQRCode(testFile.absolutePath)
        }

        // Simple throughput test
        val iterations = 10
        val throughputTime = measureTimeMillis {
            repeat(iterations) {
                reader.readQRCode(testFile.absolutePath)
            }
        }
        val throughput = (iterations * 1000.0) / throughputTime

        recordPerformanceMetric("regression_processing_time", processingTime)
        recordPerformanceMetric("regression_throughput", throughput.toLong())

        println("Regression test - Processing time: ${processingTime}ms, Throughput: ${String.format("%.2f", throughput)} ops/sec")

        // Assertions to catch performance regressions
        assertTrue(processingTime <= expectedMaxProcessingTime, 
            "Performance regression detected: processing time ${processingTime}ms exceeds expected ${expectedMaxProcessingTime}ms")
        assertTrue(throughput >= expectedMinThroughput, 
            "Performance regression detected: throughput ${String.format("%.2f", throughput)} ops/sec below expected ${expectedMinThroughput}")
    }

    private fun recordPerformanceMetric(key: String, value: Long) {
        performanceResults.computeIfAbsent(key) { mutableListOf() }.add(value)
    }

    private fun printPerformanceSummary() {
        if (performanceResults.isNotEmpty()) {
            println("\n=== Performance Summary ===")
            performanceResults.forEach { (metric, values) ->
                val avg = values.average()
                val min = values.minOrNull() ?: 0L
                val max = values.maxOrNull() ?: 0L
                println("$metric: avg=${String.format("%.2f", avg)}, min=$min, max=$max")
            }
            println("============================\n")
        }
    }
}

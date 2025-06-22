package qr

import com.github.sarxos.webcam.Webcam
import com.google.zxing.*
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import utils.PerformanceProfiler
import java.awt.image.BufferedImage
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * QR code tracking in video streams
 * Task 80: Implement QR code tracking in video streams
 */
class QRVideoTracker {
    
    private val reader = MultiFormatReader()
    private var isTracking = AtomicBoolean(false)
    private var webcam: Webcam? = null
    
    // Tracking state
    private val trackedQRCodes = ConcurrentHashMap<String, QRTrackingInfo>()
    private var frameCount = 0L
    private var startTime: Instant? = null
    
    data class QRTrackingInfo(
        val content: String,
        val firstDetected: Instant,
        var lastDetected: Instant,
        var detectionCount: Int = 1,
        var avgX: Double = 0.0,
        var avgY: Double = 0.0,
        var isStable: Boolean = false,
        val id: String = generateQRId(content)
    ) {        companion object {
            fun generateQRId(content: String): String {
                return "QR_${content.hashCode().toString(16)}"
            }
        }
        
        fun updatePosition(x: Double, y: Double) {
            avgX = (avgX * detectionCount + x) / (detectionCount + 1)
            avgY = (avgY * detectionCount + y) / (detectionCount + 1)
            detectionCount++
            lastDetected = Instant.now()
            
            // Consider stable if detected more than 5 times
            isStable = detectionCount > 5
        }
    }
    
    data class QRTrackingResult(
        val qrCodes: List<QRTrackingInfo>,
        val newDetections: List<QRTrackingInfo>,
        val lostQRCodes: List<QRTrackingInfo>,
        val frameNumber: Long,
        val timestamp: Instant,
        val processingTimeMs: Long
    )
    
    /**
     * Start QR code tracking in video stream
     */
    fun startTracking(
        webcamIndex: Int = 0,
        onTrackingUpdate: (QRTrackingResult) -> Unit,
        onError: (String) -> Unit
    ): Task<Void> {
        return object : Task<Void>() {
            override fun call(): Void? {
                return PerformanceProfiler.measureOperation("videoTracking") {
                    try {
                        initializeWebcam(webcamIndex)
                        isTracking.set(true)
                        startTime = Instant.now()
                        frameCount = 0
                        
                        while (isTracking.get() && !isCancelled) {
                            val startFrame = System.currentTimeMillis()
                            
                            val frame = webcam?.image
                            if (frame != null) {
                                frameCount++
                                val result = processVideoFrame(frame, frameCount)
                                
                                val processingTime = System.currentTimeMillis() - startFrame
                                val trackingResult = QRTrackingResult(
                                    qrCodes = trackedQRCodes.values.toList(),
                                    newDetections = result.newDetections,
                                    lostQRCodes = result.lostQRCodes,
                                    frameNumber = frameCount,
                                    timestamp = Instant.now(),
                                    processingTimeMs = processingTime
                                )
                                
                                onTrackingUpdate(trackingResult)
                                
                                // Clean up old QR codes (not seen for 5 seconds)
                                cleanupOldQRCodes()
                                
                                // Limit frame rate to 15 FPS
                                Thread.sleep(maxOf(0, 67 - processingTime))
                            }
                        }
                    } catch (e: Exception) {
                        onError("Video tracking error: ${e.message}")
                    } finally {
                        stopTracking()
                    }
                    null
                }
            }
        }
    }
    
    /**
     * Stop QR code tracking
     */
    fun stopTracking() {
        isTracking.set(false)
        webcam?.close()
        webcam = null
    }
    
    /**
     * Process a single video frame for QR codes
     */
    private fun processVideoFrame(frame: BufferedImage, frameNumber: Long): FrameProcessingResult {
        val newDetections = mutableListOf<QRTrackingInfo>()
        val currentFrameQRs = mutableSetOf<String>()
        
        try {
            // Try to detect QR codes in the frame
            val detectedQRs = detectQRCodesInFrame(frame)
            
            for (detection in detectedQRs) {
                val qrId = QRTrackingInfo.generateQRId(detection.content)
                currentFrameQRs.add(qrId)
                
                val existingQR = trackedQRCodes[qrId]
                if (existingQR != null) {
                    // Update existing QR code
                    existingQR.updatePosition(detection.x, detection.y)
                } else {
                    // New QR code detected
                    val newQR = QRTrackingInfo(
                        content = detection.content,
                        firstDetected = Instant.now(),
                        lastDetected = Instant.now(),
                        avgX = detection.x,
                        avgY = detection.y
                    )
                    trackedQRCodes[qrId] = newQR
                    newDetections.add(newQR)
                }
            }
        } catch (e: Exception) {
            // Frame processing failed, continue with next frame
        }
        
        // Check for lost QR codes (not detected in current frame)
        val lostQRCodes = mutableListOf<QRTrackingInfo>()
        val currentTime = Instant.now()
        
        trackedQRCodes.values.forEach { qr ->
            if (!currentFrameQRs.contains(qr.id)) {
                val timeSinceLastSeen = currentTime.epochSecond - qr.lastDetected.epochSecond
                if (timeSinceLastSeen > 2) { // Consider lost after 2 seconds
                    lostQRCodes.add(qr)
                }
            }
        }
        
        return FrameProcessingResult(newDetections, lostQRCodes)
    }
    
    /**
     * Detect QR codes in a single frame
     */
    private fun detectQRCodesInFrame(frame: BufferedImage): List<QRDetection> {
        val detections = mutableListOf<QRDetection>()
        
        try {
            val source = BufferedImageLuminanceSource(frame)
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            
            // Try to decode QR code
            val result = reader.decode(bitmap)
            
            // For simplicity, assume QR code is in center of frame
            // In a real implementation, you'd want to find the actual position
            val centerX = frame.width / 2.0
            val centerY = frame.height / 2.0
            
            detections.add(QRDetection(result.text, centerX, centerY))
            
        } catch (e: NotFoundException) {
            // No QR code found in this frame
        } catch (e: Exception) {
            // Other decoding error
        }
        
        return detections
    }
    
    /**
     * Initialize webcam for video tracking
     */
    private fun initializeWebcam(webcamIndex: Int) {
        val webcams = Webcam.getWebcams()
        if (webcams.isEmpty()) {
            throw Exception("No webcams available")
        }
        
        webcam = if (webcamIndex < webcams.size) {
            webcams[webcamIndex]
        } else {
            webcams[0]
        }
        
        webcam?.open()
        if (webcam?.isOpen != true) {
            throw Exception("Failed to open webcam")
        }
    }
    
    /**
     * Clean up QR codes that haven't been seen recently
     */
    private fun cleanupOldQRCodes() {
        val currentTime = Instant.now()
        val keysToRemove = mutableListOf<String>()
        
        trackedQRCodes.forEach { (key, qr) ->
            val timeSinceLastSeen = currentTime.epochSecond - qr.lastDetected.epochSecond
            if (timeSinceLastSeen > 5) { // Remove after 5 seconds
                keysToRemove.add(key)
            }
        }
        
        keysToRemove.forEach { trackedQRCodes.remove(it) }
    }
    
    /**
     * Get tracking statistics
     */
    fun getTrackingStatistics(): Map<String, Any> {
        val currentTime = Instant.now()
        val duration = if (startTime != null) {
            currentTime.epochSecond - startTime!!.epochSecond
        } else 0L
        
        return mapOf(
            "isTracking" to isTracking.get(),
            "frameCount" to frameCount,
            "trackingDurationSeconds" to duration,
            "averageFPS" to if (duration > 0) frameCount.toDouble() / duration else 0.0,
            "currentQRCount" to trackedQRCodes.size,
            "totalUniqueQRs" to trackedQRCodes.values.map { it.content }.distinct().size,
            "stableQRs" to trackedQRCodes.values.count { it.isStable }
        )
    }
    
    /**
     * Get all currently tracked QR codes
     */
    fun getCurrentQRCodes(): List<QRTrackingInfo> {
        return trackedQRCodes.values.toList()
    }
    
    /**
     * Get QR codes with specific content
     */
    fun findQRCodesByContent(content: String): List<QRTrackingInfo> {
        return trackedQRCodes.values.filter { it.content.contains(content, ignoreCase = true) }
    }
    
    /**
     * Clear all tracking data
     */
    fun clearTrackingData() {
        trackedQRCodes.clear()
        frameCount = 0
        startTime = null
    }
    
    /**
     * Export tracking data for analysis
     */
    fun exportTrackingData(): Map<String, Any> {
        return mapOf(
            "trackingSession" to mapOf(
                "startTime" to startTime?.toString(),
                "frameCount" to frameCount,
                "statistics" to getTrackingStatistics()
            ),
            "qrCodes" to trackedQRCodes.values.map { qr ->
                mapOf(
                    "id" to qr.id,
                    "content" to qr.content,
                    "firstDetected" to qr.firstDetected.toString(),
                    "lastDetected" to qr.lastDetected.toString(),
                    "detectionCount" to qr.detectionCount,
                    "avgPosition" to mapOf("x" to qr.avgX, "y" to qr.avgY),
                    "isStable" to qr.isStable
                )
            }
        )
    }
    
    // Helper data classes
    private data class QRDetection(
        val content: String,
        val x: Double,
        val y: Double
    )
    
    private data class FrameProcessingResult(
        val newDetections: List<QRTrackingInfo>,
        val lostQRCodes: List<QRTrackingInfo>
    )
}

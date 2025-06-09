package webcam

import com.github.sarxos.webcam.Webcam
import com.github.sarxos.webcam.WebcamResolution
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import qr.QRCodeReader
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture

/**
 * Webcam manager for real-time QR code scanning
 * Implements Task 47: Add support for reading QR codes from a webcam
 */
class WebcamManager {
    
    private var webcam: Webcam? = null
    private var isScanning = false
    private val qrReader = QRCodeReader()
    
    /**
     * Check if webcam is available
     */
    fun isWebcamAvailable(): Boolean {
        return Webcam.getWebcams().isNotEmpty()
    }
    
    /**
     * Get available webcams
     */
    fun getAvailableWebcams(): List<Webcam> {
        return Webcam.getWebcams()
    }
    
    /**
     * Initialize webcam
     */
    fun initializeWebcam(webcamIndex: Int = 0): Boolean {
        return try {
            val webcams = Webcam.getWebcams()
            if (webcams.isEmpty() || webcamIndex >= webcams.size) {
                false
            } else {
                webcam = webcams[webcamIndex]
                webcam?.viewSize = WebcamResolution.VGA.size
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Start webcam
     */
    fun startWebcam(): Boolean {
        return try {
            webcam?.open()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Stop webcam
     */
    fun stopWebcam() {
        try {
            isScanning = false
            webcam?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Capture single frame from webcam
     */
    fun captureFrame(): BufferedImage? {
        return try {
            webcam?.image
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Start continuous QR scanning
     */
    fun startQRScanning(
        onFrameUpdate: (Image) -> Unit,
        onQRDetected: (String) -> Unit,
        onError: (String) -> Unit
    ): Task<Void> {
        
        val task = object : Task<Void>() {
            override fun call(): Void? {
                isScanning = true
                
                while (isScanning && !isCancelled) {
                    try {
                        val bufferedImage = captureFrame()
                        
                        if (bufferedImage != null) {
                            // Convert to JavaFX Image for display
                            val fxImage = SwingFXUtils.toFXImage(bufferedImage, null)
                            Platform.runLater {
                                onFrameUpdate(fxImage)
                            }
                            
                            // Try to read QR code
                            try {
                                // Create a temporary file for QR reading
                                val tempFile = java.io.File.createTempFile("webcam_frame", ".png")
                                javax.imageio.ImageIO.write(bufferedImage, "png", tempFile)
                                
                                val qrContent = qrReader.readQRCode(tempFile.absolutePath)
                                tempFile.delete()
                                
                                if (qrContent.isNotEmpty()) {
                                    Platform.runLater {
                                        onQRDetected(qrContent)
                                    }
                                    // Stop scanning after detecting QR code
                                    isScanning = false
                                }
                            } catch (qrException: Exception) {
                                // QR not detected in this frame, continue scanning
                            }
                        }
                        
                        // Small delay to prevent excessive CPU usage
                        Thread.sleep(100)
                        
                    } catch (e: Exception) {
                        Platform.runLater {
                            onError("Webcam error: ${e.message}")
                        }
                        isScanning = false
                    }
                }
                
                return null
            }
        }
        
        return task
    }
    
    /**
     * Stop QR scanning
     */
    fun stopQRScanning() {
        isScanning = false
    }
    
    /**
     * Check if currently scanning
     */
    fun isScanning(): Boolean = isScanning
    
    /**
     * Get webcam info
     */
    fun getWebcamInfo(): String? {
        return webcam?.name
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopQRScanning()
        stopWebcam()
    }
}

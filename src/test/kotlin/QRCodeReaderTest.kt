package qr

import org.junit.Test
import org.junit.Assert.*

class QRCodeReaderTest {
    
    @Test
    fun testQRCodeReaderExists() {
        val reader = QRCodeReader()
        assertNotNull(reader)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testFileNotFound() {
        val reader = QRCodeReader()
        reader.readQRCode("nonexistent.png")
    }
}

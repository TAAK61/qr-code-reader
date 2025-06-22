package qr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * Tests for batch export functionality
 * Task 78: Implement batch export of QR code contents
 */
class BatchExporterTest {
    
    private lateinit var exporter: BatchExporter
    
    @TempDir
    lateinit var tempDir: Path
    
    @BeforeEach
    fun setUp() {
        exporter = BatchExporter()
        exporter.clearData()
    }
    
    @Test
    fun `should add QR data correctly`() {
        exporter.addQRData(
            fileName = "test.png",
            filePath = "/path/to/test.png",
            content = "Hello World",
            processingTimeMs = 100L
        )
        
        val stats = exporter.getStatistics()
        assertEquals(1, stats["totalItems"])
    }
    
    @Test
    fun `should detect content types correctly`() {
        // Add vCard
        exporter.addQRData(
            fileName = "vcard.png",
            filePath = "/path/vcard.png",
            content = "BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nEND:VCARD"
        )
        
        // Add WiFi
        exporter.addQRData(
            fileName = "wifi.png", 
            filePath = "/path/wifi.png",
            content = "WIFI:T:WPA;S:MyNetwork;P:password;;"
        )
        
        // Add URL
        exporter.addQRData(
            fileName = "url.png",
            filePath = "/path/url.png", 
            content = "https://example.com"
        )
        
        val stats = exporter.getStatistics()
        assertEquals(3, stats["totalItems"])
        assertEquals(1, stats["vCardCount"])
        assertEquals(1, stats["wifiCount"])
        assertEquals(1, stats["urlCount"])
    }
    
    @Test
    fun `should export to CSV correctly`() {
        exporter.addQRData(
            fileName = "test1.png",
            filePath = "/path/test1.png",
            content = "Test Content 1"
        )
        exporter.addQRData(
            fileName = "test2.png",
            filePath = "/path/test2.png", 
            content = "Test Content 2"
        )
        
        val csvFile = tempDir.resolve("export.csv").toFile()
        val success = exporter.exportToFile(csvFile.absolutePath, BatchExporter.ExportFormat.CSV)
        
        assertTrue(success)
        assertTrue(csvFile.exists())
        
        val content = csvFile.readText()
        assertTrue(content.contains("FileName,FilePath,Content"))
        assertTrue(content.contains("test1.png"))
        assertTrue(content.contains("test2.png"))
        assertTrue(content.contains("Test Content 1"))
        assertTrue(content.contains("Test Content 2"))
    }
    
    @Test
    fun `should export to JSON correctly`() {
        exporter.addQRData(
            fileName = "test.png",
            filePath = "/path/test.png",
            content = "JSON Test Content"
        )
        
        val jsonFile = tempDir.resolve("export.json").toFile()
        val success = exporter.exportToFile(jsonFile.absolutePath, BatchExporter.ExportFormat.JSON)
        
        assertTrue(success)
        assertTrue(jsonFile.exists())
        
        val content = jsonFile.readText()
        assertTrue(content.contains("exportTimestamp"))
        assertTrue(content.contains("totalItems"))
        assertTrue(content.contains("qrCodes"))
        assertTrue(content.contains("JSON Test Content"))
    }
    
    @Test
    fun `should export to XML correctly`() {
        exporter.addQRData(
            fileName = "test.png",
            filePath = "/path/test.png",
            content = "XML Test Content"
        )
        
        val xmlFile = tempDir.resolve("export.xml").toFile()
        val success = exporter.exportToFile(xmlFile.absolutePath, BatchExporter.ExportFormat.XML)
        
        assertTrue(success)
        assertTrue(xmlFile.exists())
        
        val content = xmlFile.readText()
        assertTrue(content.contains("<?xml version=\"1.0\""))
        assertTrue(content.contains("<qrCodeExport>"))
        assertTrue(content.contains("<fileName><![CDATA[test.png]]></fileName>"))
        assertTrue(content.contains("<content><![CDATA[XML Test Content]]></content>"))
        assertTrue(content.contains("</qrCodeExport>"))
    }
    
    @Test
    fun `should export to HTML correctly`() {
        exporter.addQRData(
            fileName = "test.png",
            filePath = "/path/test.png",
            content = "HTML Test Content"
        )
        
        val htmlFile = tempDir.resolve("export.html").toFile()
        val success = exporter.exportToFile(htmlFile.absolutePath, BatchExporter.ExportFormat.HTML)
        
        assertTrue(success)
        assertTrue(htmlFile.exists())
        
        val content = htmlFile.readText()
        assertTrue(content.contains("<!DOCTYPE html>"))
        assertTrue(content.contains("<title>QR Code Export Report</title>"))
        assertTrue(content.contains("HTML Test Content"))
        assertTrue(content.contains("test.png"))
    }
    
    @Test
    fun `should export to TXT correctly`() {
        exporter.addQRData(
            fileName = "test.png",
            filePath = "/path/test.png",
            content = "TXT Test Content"
        )
        
        val txtFile = tempDir.resolve("export.txt").toFile()
        val success = exporter.exportToFile(txtFile.absolutePath, BatchExporter.ExportFormat.TXT)
        
        assertTrue(success)
        assertTrue(txtFile.exists())
        
        val content = txtFile.readText()
        assertTrue(content.contains("QR Code Export Report"))
        assertTrue(content.contains("test.png"))
        assertTrue(content.contains("TXT Test Content"))
    }
    
    @Test
    fun `should calculate statistics correctly`() {
        // Add mixed content types
        exporter.addQRData("vcard.png", "/path/vcard.png", "BEGIN:VCARD\nFN:Test\nEND:VCARD", 50L)
        exporter.addQRData("wifi.png", "/path/wifi.png", "WIFI:T:WPA;S:Test;;", 75L)
        exporter.addQRData("url.png", "/path/url.png", "https://test.com", 100L)
        exporter.addQRData("plain.png", "/path/plain.png", "Plain text", 25L)
        
        val stats = exporter.getStatistics()
        
        assertEquals(4, stats["totalItems"])
        assertEquals(1, stats["vCardCount"])
        assertEquals(1, stats["wifiCount"])
        assertEquals(1, stats["urlCount"])
        assertEquals(0, stats["encryptedCount"])
        assertEquals(62.5, stats["averageProcessingTime"])
        assertEquals(100L, stats["maxProcessingTime"])
        assertEquals(25L, stats["minProcessingTime"])
    }
    
    @Test
    fun `should handle CSV special characters correctly`() {
        exporter.addQRData(
            fileName = "special.png",
            filePath = "/path/special.png",
            content = "Content with \"quotes\", commas, and\nnewlines"
        )
        
        val csvFile = tempDir.resolve("special.csv").toFile()
        val success = exporter.exportToFile(csvFile.absolutePath, BatchExporter.ExportFormat.CSV)
        
        assertTrue(success)
        val content = csvFile.readText()
        assertTrue(content.contains("\"Content with \"\"quotes\"\", commas, and\nnewlines\""))
    }
    
    @Test
    fun `should clear data correctly`() {
        exporter.addQRData("test.png", "/path/test.png", "Test")
        assertEquals(1, exporter.getStatistics()["totalItems"])
        
        exporter.clearData()
        assertEquals(0, exporter.getStatistics()["totalItems"])
    }
}

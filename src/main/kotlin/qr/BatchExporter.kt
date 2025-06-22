package qr

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.JsonSerializationContext
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import utils.PerformanceProfiler

/**
 * Batch export functionality for QR code contents
 * Task 78: Implement batch export of QR code contents
 */
class BatchExporter {
    
    data class QRExportData(
        val fileName: String,
        val filePath: String,
        val content: String,
        val timestamp: LocalDateTime,
        val processingTimeMs: Long? = null,
        val isVCard: Boolean = false,
        val isWiFi: Boolean = false,
        val isUrl: Boolean = false,
        val urlType: String? = null,
        val isEncrypted: Boolean = false,
        val metadata: Map<String, Any> = emptyMap()
    )
    
    enum class ExportFormat {
        CSV, JSON, XML, TXT, HTML
    }
    
    private val exportData = ConcurrentHashMap<String, QRExportData>()
    
    /**
     * Add QR code data for export
     */
    fun addQRData(
        fileName: String,
        filePath: String,
        content: String,
        processingTimeMs: Long? = null,
        metadata: Map<String, Any> = emptyMap()
    ) {
        val qrData = QRExportData(
            fileName = fileName,
            filePath = filePath,
            content = content,
            timestamp = LocalDateTime.now(),
            processingTimeMs = processingTimeMs,
            isVCard = VCardProcessor.isVCard(content),
            isWiFi = WiFiProcessor.isWiFiConfig(content),
            isUrl = detectUrl(content),
            urlType = detectUrlType(content),
            isEncrypted = detectEncryption(content),
            metadata = metadata
        )
        
        exportData[fileName] = qrData
    }
    
    /**
     * Export all collected QR data to specified format
     */
    fun exportToFile(outputPath: String, format: ExportFormat): Boolean {
        return PerformanceProfiler.measureOperation("batchExport") {
            try {
                when (format) {
                    ExportFormat.CSV -> exportToCSV(outputPath)
                    ExportFormat.JSON -> exportToJSON(outputPath)
                    ExportFormat.XML -> exportToXML(outputPath)
                    ExportFormat.TXT -> exportToTXT(outputPath)
                    ExportFormat.HTML -> exportToHTML(outputPath)
                }
                true
            } catch (e: Exception) {
                println("Export failed: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Export to CSV format
     */
    private fun exportToCSV(outputPath: String) {
        FileWriter(outputPath).use { writer ->
            val csv = PrintWriter(writer)
            
            // Header
            csv.println("FileName,FilePath,Content,Timestamp,ProcessingTime(ms),IsVCard,IsWiFi,IsUrl,UrlType,IsEncrypted")
            
            // Data rows
            exportData.values.sortedBy { it.timestamp }.forEach { data ->
                val escapedContent = escapeCSV(data.content)
                csv.println("${data.fileName},${data.filePath},$escapedContent,${data.timestamp},${data.processingTimeMs ?: ""},${data.isVCard},${data.isWiFi},${data.isUrl},${data.urlType ?: ""},${data.isEncrypted}")
            }
        }
    }    /**
     * Export to JSON format
     */
    private fun exportToJSON(outputPath: String) {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ ->
                JsonPrimitive(src.toString())
            })
            .create()
        
        val jsonObject = JsonObject()
        jsonObject.addProperty("exportTimestamp", LocalDateTime.now().toString())
        jsonObject.addProperty("totalItems", exportData.size)
        
        val dataArray = gson.toJsonTree(exportData.values.sortedBy { it.timestamp })
        jsonObject.add("qrCodes", dataArray)
        
        FileWriter(outputPath).use { writer ->
            gson.toJson(jsonObject, writer)
        }
    }
    
    /**
     * Export to XML format
     */
    private fun exportToXML(outputPath: String) {
        FileWriter(outputPath).use { writer ->
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<qrCodeExport>\n")
            writer.write("  <exportInfo>\n")
            writer.write("    <timestamp>${LocalDateTime.now()}</timestamp>\n")
            writer.write("    <totalItems>${exportData.size}</totalItems>\n")
            writer.write("  </exportInfo>\n")
            writer.write("  <qrCodes>\n")
            
            exportData.values.sortedBy { it.timestamp }.forEach { data ->
                writer.write("    <qrCode>\n")
                writer.write("      <fileName><![CDATA[${data.fileName}]]></fileName>\n")
                writer.write("      <filePath><![CDATA[${data.filePath}]]></filePath>\n")
                writer.write("      <content><![CDATA[${data.content}]]></content>\n")
                writer.write("      <timestamp>${data.timestamp}</timestamp>\n")
                writer.write("      <processingTimeMs>${data.processingTimeMs ?: ""}</processingTimeMs>\n")
                writer.write("      <isVCard>${data.isVCard}</isVCard>\n")
                writer.write("      <isWiFi>${data.isWiFi}</isWiFi>\n")
                writer.write("      <isUrl>${data.isUrl}</isUrl>\n")
                writer.write("      <urlType>${data.urlType ?: ""}</urlType>\n")
                writer.write("      <isEncrypted>${data.isEncrypted}</isEncrypted>\n")
                writer.write("    </qrCode>\n")
            }
            
            writer.write("  </qrCodes>\n")
            writer.write("</qrCodeExport>\n")
        }
    }
    
    /**
     * Export to plain text format
     */
    private fun exportToTXT(outputPath: String) {
        FileWriter(outputPath).use { writer ->
            writer.write("QR Code Export Report\n")
            writer.write("=" + "=".repeat(50) + "\n")
            writer.write("Export Date: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\n")
            writer.write("Total Items: ${exportData.size}\n\n")
            
            exportData.values.sortedBy { it.timestamp }.forEachIndexed { index, data ->
                writer.write("${index + 1}. ${data.fileName}\n")
                writer.write("   File: ${data.filePath}\n")
                writer.write("   Processed: ${data.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\n")
                data.processingTimeMs?.let { writer.write("   Processing Time: ${it}ms\n") }
                
                val types = mutableListOf<String>()
                if (data.isVCard) types.add("vCard")
                if (data.isWiFi) types.add("Wi-Fi")
                if (data.isUrl) types.add("URL(${data.urlType})")
                if (data.isEncrypted) types.add("Encrypted")
                if (types.isNotEmpty()) {
                    writer.write("   Type: ${types.joinToString(", ")}\n")
                }
                
                writer.write("   Content:\n")
                val contentLines = data.content.lines()
                contentLines.take(5).forEach { line ->
                    writer.write("     $line\n")
                }
                if (contentLines.size > 5) {
                    writer.write("     ... (${contentLines.size - 5} more lines)\n")
                }
                writer.write("\n")
            }
        }
    }
    
    /**
     * Export to HTML format
     */
    private fun exportToHTML(outputPath: String) {
        FileWriter(outputPath).use { writer ->
            writer.write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>QR Code Export Report</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .header { background: #f0f0f0; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
                        .qr-item { border: 1px solid #ccc; margin: 10px 0; padding: 15px; border-radius: 5px; }
                        .qr-header { font-weight: bold; color: #333; margin-bottom: 10px; }
                        .qr-meta { color: #666; font-size: 0.9em; margin: 5px 0; }
                        .qr-content { background: #f9f9f9; padding: 10px; border-radius: 3px; font-family: monospace; white-space: pre-wrap; max-height: 200px; overflow-y: auto; }
                        .badge { background: #007bff; color: white; padding: 2px 6px; border-radius: 3px; font-size: 0.8em; margin-right: 5px; }
                        .badge-vcard { background: #28a745; }
                        .badge-wifi { background: #17a2b8; }
                        .badge-url { background: #ffc107; color: black; }
                        .badge-encrypted { background: #dc3545; }
                        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>QR Code Export Report</h1>
                        <p>Export Date: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}</p>
                        <p>Total Items: ${exportData.size}</p>
                    </div>
            """.trimIndent())
            
            // Summary table
            writer.write("<h2>Summary</h2>\n")
            writer.write("<table>\n")
            writer.write("<tr><th>Type</th><th>Count</th></tr>\n")
            
            val vCardCount = exportData.values.count { it.isVCard }
            val wifiCount = exportData.values.count { it.isWiFi }
            val urlCount = exportData.values.count { it.isUrl }
            val encryptedCount = exportData.values.count { it.isEncrypted }
            
            writer.write("<tr><td>vCard Contacts</td><td>$vCardCount</td></tr>\n")
            writer.write("<tr><td>Wi-Fi Configurations</td><td>$wifiCount</td></tr>\n")
            writer.write("<tr><td>URLs</td><td>$urlCount</td></tr>\n")
            writer.write("<tr><td>Encrypted</td><td>$encryptedCount</td></tr>\n")
            writer.write("</table>\n")
            
            writer.write("<h2>Detailed Results</h2>\n")
            
            exportData.values.sortedBy { it.timestamp }.forEach { data ->
                writer.write("<div class=\"qr-item\">\n")
                writer.write("<div class=\"qr-header\">${escapeHtml(data.fileName)}</div>\n")
                
                // Badges
                if (data.isVCard) writer.write("<span class=\"badge badge-vcard\">vCard</span>\n")
                if (data.isWiFi) writer.write("<span class=\"badge badge-wifi\">Wi-Fi</span>\n")
                if (data.isUrl) writer.write("<span class=\"badge badge-url\">URL</span>\n")
                if (data.isEncrypted) writer.write("<span class=\"badge badge-encrypted\">Encrypted</span>\n")
                writer.write("<br><br>\n")
                
                writer.write("<div class=\"qr-meta\">File: ${escapeHtml(data.filePath)}</div>\n")
                writer.write("<div class=\"qr-meta\">Processed: ${data.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}</div>\n")
                data.processingTimeMs?.let { 
                    writer.write("<div class=\"qr-meta\">Processing Time: ${it}ms</div>\n") 
                }
                
                writer.write("<div class=\"qr-content\">${escapeHtml(data.content)}</div>\n")
                writer.write("</div>\n")
            }
            
            writer.write("</body></html>\n")
        }
    }
    
    /**
     * Clear all export data
     */
    fun clearData() {
        exportData.clear()
    }
    
    /**
     * Get export statistics
     */
    fun getStatistics(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        stats["totalItems"] = exportData.size
        stats["vCardCount"] = exportData.values.count { it.isVCard }
        stats["wifiCount"] = exportData.values.count { it.isWiFi }
        stats["urlCount"] = exportData.values.count { it.isUrl }
        stats["encryptedCount"] = exportData.values.count { it.isEncrypted }
        
        val processingTimes = exportData.values.mapNotNull { it.processingTimeMs }
        if (processingTimes.isNotEmpty()) {
            stats["averageProcessingTime"] = processingTimes.average()
            stats["maxProcessingTime"] = processingTimes.maxOrNull() ?: 0
            stats["minProcessingTime"] = processingTimes.minOrNull() ?: 0
        }
        
        return stats
    }
    
    // Helper functions
    private fun escapeCSV(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\"" 
        }
        return value
    }
    
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }
    
    private fun detectUrl(content: String): Boolean {
        val trimmed = content.trim().lowercase()
        return trimmed.startsWith("http://") || 
               trimmed.startsWith("https://") ||
               trimmed.startsWith("ftp://") ||
               trimmed.startsWith("mailto:") ||
               trimmed.startsWith("tel:")
    }
    
    private fun detectUrlType(content: String): String? {
        val trimmed = content.trim().lowercase()
        return when {
            trimmed.startsWith("http://") -> "http"
            trimmed.startsWith("https://") -> "https"
            trimmed.startsWith("ftp://") -> "ftp"
            trimmed.startsWith("mailto:") -> "mailto"
            trimmed.startsWith("tel:") -> "tel"
            else -> null
        }
    }
    
    private fun detectEncryption(content: String): Boolean {
        // Simple heuristic - encrypted content is usually base64 or contains encryption markers
        return content.startsWith("ENC:") || 
               (content.length > 50 && content.matches(Regex("^[A-Za-z0-9+/=]+$")))
    }
}

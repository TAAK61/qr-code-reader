package utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Simple logging utility for the QR Code Reader application.
 * 
 * This logger provides different log levels and formats messages consistently.
 * It can be easily extended to support different output destinations.
 */
object Logger {
    
    /**
     * Enumeration of available log levels.
     */
    enum class Level(val priority: Int, val displayName: String) {
        DEBUG(0, "DEBUG"),
        INFO(1, "INFO"),
        WARN(2, "WARN"),
        ERROR(3, "ERROR")
    }
    
    private var currentLevel = Level.INFO
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    
    /**
     * Sets the minimum log level. Messages below this level will be ignored.
     * 
     * @param level The minimum log level to display
     */
    fun setLevel(level: Level) {
        currentLevel = level
    }
    
    /**
     * Logs a debug message.
     * 
     * @param tag The component or class name generating the log
     * @param message The log message
     * @param throwable Optional throwable for stack trace
     */
    fun debug(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.DEBUG, tag, message, throwable)
    }
    
    /**
     * Logs an info message.
     * 
     * @param tag The component or class name generating the log
     * @param message The log message
     * @param throwable Optional throwable for stack trace
     */
    fun info(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.INFO, tag, message, throwable)
    }
    
    /**
     * Logs a warning message.
     * 
     * @param tag The component or class name generating the log
     * @param message The log message
     * @param throwable Optional throwable for stack trace
     */
    fun warn(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.WARN, tag, message, throwable)
    }
    
    /**
     * Logs an error message.
     * 
     * @param tag The component or class name generating the log
     * @param message The log message
     * @param throwable Optional throwable for stack trace
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }
    
    /**
     * Core logging method that formats and outputs the log message.
     * 
     * @param level The log level
     * @param tag The component tag
     * @param message The message to log
     * @param throwable Optional throwable for detailed error information
     */
    private fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (level.priority >= currentLevel.priority) {
            val timestamp = LocalDateTime.now().format(dateFormatter)
            val formattedMessage = "[$timestamp] [${level.displayName}] [$tag] $message"
            
            when (level) {
                Level.ERROR -> System.err.println(formattedMessage)
                else -> println(formattedMessage)
            }
            
            throwable?.let {
                it.printStackTrace()
            }
        }
    }
    
    /**
     * Logs the start of a processing operation.
     * 
     * @param operation The name of the operation starting
     * @param details Additional details about the operation
     */
    fun logOperationStart(operation: String, details: String = "") {
        info("OPERATION", "Starting: $operation${if (details.isNotEmpty()) " - $details" else ""}")
    }
    
    /**
     * Logs the completion of a processing operation.
     * 
     * @param operation The name of the operation that completed
     * @param durationMs The duration of the operation in milliseconds
     */
    fun logOperationComplete(operation: String, durationMs: Long) {
        info("OPERATION", "Completed: $operation (${durationMs}ms)")
    }
    
    /**
     * Logs performance metrics.
     * 
     * @param metric The name of the metric
     * @param value The metric value
     * @param unit The unit of measurement
     */
    fun logMetric(metric: String, value: Any, unit: String = "") {
        debug("METRICS", "$metric: $value${if (unit.isNotEmpty()) " $unit" else ""}")
    }
}
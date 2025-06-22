package utils

import java.util.concurrent.ConcurrentHashMap
import java.time.Instant
import java.time.Duration

/**
 * Simple performance profiler for identifying bottlenecks
 * Task 56: Profile the application to identify performance bottlenecks
 */
object PerformanceProfiler {
    
    private val measurements = ConcurrentHashMap<String, MutableList<PerformanceMeasurement>>()
    private val activeOperations = ConcurrentHashMap<String, Instant>()
    
    data class PerformanceMeasurement(
        val operationName: String,
        val duration: Duration,
        val timestamp: Instant,
        val threadName: String,
        val memoryUsed: Long? = null
    )
    
    data class PerformanceReport(
        val operationName: String,
        val totalExecutions: Int,
        val averageDuration: Duration,
        val minDuration: Duration,
        val maxDuration: Duration,
        val totalDuration: Duration,
        val averageMemoryUsed: Long?
    )
    
    /**
     * Start measuring an operation
     */
    fun startOperation(operationName: String): String {
        val operationId = "${operationName}_${Thread.currentThread().id}_${Instant.now().toEpochMilli()}"
        activeOperations[operationId] = Instant.now()
        return operationId
    }
    
    /**
     * End measuring an operation
     */
    fun endOperation(operationId: String, memoryUsed: Long? = null) {
        val startTime = activeOperations.remove(operationId) ?: return
        val endTime = Instant.now()
        val duration = Duration.between(startTime, endTime)
        
        val operationName = operationId.substringBefore("_")
        val measurement = PerformanceMeasurement(
            operationName = operationName,
            duration = duration,
            timestamp = startTime,
            threadName = Thread.currentThread().name,
            memoryUsed = memoryUsed
        )
        
        measurements.getOrPut(operationName) { mutableListOf() }.add(measurement)
    }
    
    /**
     * Measure an operation with automatic timing
     */
    inline fun <T> measureOperation(operationName: String, operation: () -> T): T {
        val operationId = startOperation(operationName)
        val startMemory = getUsedMemory()
        
        return try {
            operation()
        } finally {
            val endMemory = getUsedMemory()
            val memoryDiff = if (startMemory != null && endMemory != null) {
                endMemory - startMemory
            } else null
            endOperation(operationId, memoryDiff)
        }
    }
    
    /**
     * Get current memory usage
     */
    fun getUsedMemory(): Long? {
        return try {
            val runtime = Runtime.getRuntime()
            runtime.totalMemory() - runtime.freeMemory()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Generate performance report for a specific operation
     */
    fun getPerformanceReport(operationName: String): PerformanceReport? {
        val operationMeasurements = measurements[operationName] ?: return null
        
        if (operationMeasurements.isEmpty()) return null
        
        val durations = operationMeasurements.map { it.duration }
        val memoryUsages = operationMeasurements.mapNotNull { it.memoryUsed }
        
        return PerformanceReport(
            operationName = operationName,
            totalExecutions = operationMeasurements.size,
            averageDuration = Duration.ofNanos(durations.map { it.toNanos() }.average().toLong()),
            minDuration = durations.minOrNull() ?: Duration.ZERO,
            maxDuration = durations.maxOrNull() ?: Duration.ZERO,
            totalDuration = durations.fold(Duration.ZERO) { acc, duration -> acc.plus(duration) },
            averageMemoryUsed = if (memoryUsages.isNotEmpty()) {
                memoryUsages.average().toLong()
            } else null
        )
    }
    
    /**
     * Generate comprehensive performance report
     */
    fun generateFullReport(): Map<String, PerformanceReport> {
        return measurements.keys.mapNotNull { operationName ->
            getPerformanceReport(operationName)?.let { operationName to it }
        }.toMap()
    }
    
    /**
     * Clear all measurements
     */
    fun clearMeasurements() {
        measurements.clear()
        activeOperations.clear()
    }
    
    /**
     * Get bottlenecks (operations taking more than threshold)
     */
    fun getBottlenecks(thresholdMs: Long = 100): List<PerformanceReport> {
        return generateFullReport().values.filter { report ->
            report.averageDuration.toMillis() > thresholdMs ||
            report.maxDuration.toMillis() > thresholdMs * 2
        }.sortedByDescending { it.averageDuration.toMillis() }
    }
    
    /**
     * Print performance summary to console
     */
    fun printPerformanceSummary() {
        val reports = generateFullReport()
        
        println("=== PERFORMANCE PROFILING REPORT ===")
        println("Operations tracked: ${reports.size}")
        
        if (reports.isEmpty()) {
            println("No performance data available.")
            return
        }
        
        println("\nOperation Performance Summary:")
        println("%-30s %8s %10s %10s %10s %15s".format(
            "Operation", "Count", "Avg (ms)", "Min (ms)", "Max (ms)", "Total (ms)"
        ))
        println("-".repeat(85))
        
        reports.values.sortedByDescending { it.totalDuration.toMillis() }.forEach { report ->
            println("%-30s %8d %10.2f %10.2f %10.2f %15.2f".format(
                report.operationName.take(30),
                report.totalExecutions,
                report.averageDuration.toMillis().toDouble(),
                report.minDuration.toMillis().toDouble(),
                report.maxDuration.toMillis().toDouble(),
                report.totalDuration.toMillis().toDouble()
            ))
        }
        
        val bottlenecks = getBottlenecks()
        if (bottlenecks.isNotEmpty()) {
            println("\nPERFORMANCE BOTTLENECKS (>100ms average):")
            bottlenecks.forEach { bottleneck ->
                println("⚠️  ${bottleneck.operationName}: ${bottleneck.averageDuration.toMillis()}ms average")
            }
        }
        
        println("\n=== END PERFORMANCE REPORT ===")
    }
}

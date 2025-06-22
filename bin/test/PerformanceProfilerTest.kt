package utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.Duration

/**
 * Tests for performance profiler functionality
 * Task 56: Profile the application to identify performance bottlenecks
 */
class PerformanceProfilerTest {
    
    @BeforeEach
    fun setUp() {
        PerformanceProfiler.clearMeasurements()
    }
    
    @Test
    fun `should measure operation duration correctly`() {
        val result = PerformanceProfiler.measureOperation("testOperation") {
            Thread.sleep(100) // Sleep for 100ms
            "test result"
        }
        
        assertEquals("test result", result)
        
        val report = PerformanceProfiler.getPerformanceReport("testOperation")
        assertNotNull(report)
        assertEquals(1, report!!.totalExecutions)
        assertTrue(report.averageDuration.toMillis() >= 100)
    }
    
    @Test
    fun `should track multiple operations correctly`() {
        repeat(3) {
            PerformanceProfiler.measureOperation("operation1") {
                Thread.sleep(50)
            }
        }
        
        repeat(2) {
            PerformanceProfiler.measureOperation("operation2") {
                Thread.sleep(75)
            }
        }
        
        val report1 = PerformanceProfiler.getPerformanceReport("operation1")
        val report2 = PerformanceProfiler.getPerformanceReport("operation2")
        
        assertNotNull(report1)
        assertNotNull(report2)
        assertEquals(3, report1!!.totalExecutions)
        assertEquals(2, report2!!.totalExecutions)
    }
    
    @Test
    fun `should identify bottlenecks correctly`() {
        // Fast operation
        PerformanceProfiler.measureOperation("fastOp") {
            Thread.sleep(10)
        }
        
        // Slow operation (bottleneck)
        PerformanceProfiler.measureOperation("slowOp") {
            Thread.sleep(150)
        }
        
        val bottlenecks = PerformanceProfiler.getBottlenecks(100)
        
        assertEquals(1, bottlenecks.size)
        assertEquals("slowOp", bottlenecks[0].operationName)
        assertTrue(bottlenecks[0].averageDuration.toMillis() > 100)
    }
    
    @Test
    fun `should generate full report correctly`() {
        PerformanceProfiler.measureOperation("op1") { Thread.sleep(30) }
        PerformanceProfiler.measureOperation("op2") { Thread.sleep(40) }
        
        val fullReport = PerformanceProfiler.generateFullReport()
        
        assertEquals(2, fullReport.size)
        assertTrue(fullReport.containsKey("op1"))
        assertTrue(fullReport.containsKey("op2"))
    }
    
    @Test
    fun `should handle manual operation timing`() {
        val operationId = PerformanceProfiler.startOperation("manualOp")
        Thread.sleep(60)
        PerformanceProfiler.endOperation(operationId)
        
        val report = PerformanceProfiler.getPerformanceReport("manualOp")
        assertNotNull(report)
        assertEquals(1, report!!.totalExecutions)
        assertTrue(report.averageDuration.toMillis() >= 60)
    }
    
    @Test
    fun `should clear measurements correctly`() {
        PerformanceProfiler.measureOperation("testOp") { Thread.sleep(10) }
        
        assertNotNull(PerformanceProfiler.getPerformanceReport("testOp"))
        
        PerformanceProfiler.clearMeasurements()
        
        assertNull(PerformanceProfiler.getPerformanceReport("testOp"))
    }
    
    @Test
    fun `should calculate statistics correctly`() {
        // Run same operation multiple times with different durations
        repeat(5) { index ->
            PerformanceProfiler.measureOperation("statTest") {
                Thread.sleep((index + 1) * 20L) // 20, 40, 60, 80, 100 ms
            }
        }
        
        val report = PerformanceProfiler.getPerformanceReport("statTest")
        assertNotNull(report)
        assertEquals(5, report!!.totalExecutions)
        assertTrue(report.minDuration.toMillis() >= 20)
        assertTrue(report.maxDuration.toMillis() >= 100)
        assertTrue(report.averageDuration.toMillis() >= 60) // Average should be around 60ms
    }
}

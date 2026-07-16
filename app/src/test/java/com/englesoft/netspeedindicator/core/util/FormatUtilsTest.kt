package com.englesoft.netspeedindicator.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `formatBytes handles zero and negative as zero bytes`() {
        assertEquals("0 B", FormatUtils.formatBytes(0))
        assertEquals("0 B", FormatUtils.formatBytes(-100))
    }

    @Test
    fun `formatBytes formats each unit group with the expected precision`() {
        assertEquals("512 B", FormatUtils.formatBytes(512))
        assertEquals("1 KB", FormatUtils.formatBytes(1024))
        assertEquals("10 KB", FormatUtils.formatBytes(10240))
        assertEquals("1.0 MB", FormatUtils.formatBytes(1024L * 1024))
        assertEquals("1.00 GB", FormatUtils.formatBytes(1024L * 1024 * 1024))
    }

    @Test
    fun `formatSpeed handles zero and negative as zero speed`() {
        assertEquals("0 B/s", FormatUtils.formatSpeed(0))
        assertEquals("0 B/s", FormatUtils.formatSpeed(-1))
    }

    @Test
    fun `formatSpeed formats below and above 1024 bytes per second`() {
        assertEquals("500 B/s", FormatUtils.formatSpeed(500))
        assertEquals("1.5 KB/s", FormatUtils.formatSpeed(1536))
        assertEquals("1.0 MB/s", FormatUtils.formatSpeed(1024L * 1024))
    }

    @Test
    fun `formatSpeedCompact switches units at the documented thresholds`() {
        assertEquals("50" to "KB/s", FormatUtils.formatSpeedCompact(50 * 1024L))
        assertEquals("1.2" to "MB/s", FormatUtils.formatSpeedCompact((1.2 * 1024 * 1024).toLong()))
        assertEquals("2.0" to "GB/s", FormatUtils.formatSpeedCompact(2L * 1024 * 1024 * 1024))
    }

    @Test
    fun `formatSpeedValue and formatSpeedUnit split the same value formatSpeed produces`() {
        assertEquals("1.5", FormatUtils.formatSpeedValue(1536))
        assertEquals("KB/s", FormatUtils.formatSpeedUnit(1536))
        assertEquals("0", FormatUtils.formatSpeedValue(-5))
        assertEquals("B/s", FormatUtils.formatSpeedUnit(-5))
    }

    @Test
    fun `formatBytesValue and formatBytesUnit split the same value formatBytes produces`() {
        assertEquals("0", FormatUtils.formatBytesValue(0))
        assertEquals("B", FormatUtils.formatBytesUnit(0))
        assertEquals("1.0", FormatUtils.formatBytesValue(1024L * 1024))
        assertEquals("MB", FormatUtils.formatBytesUnit(1024L * 1024))
    }

    @Test
    fun `formatDuration pads to HH-MM-SS`() {
        assertEquals("00:00:00", FormatUtils.formatDuration(0))
        assertEquals("00:01:05", FormatUtils.formatDuration(65))
        assertEquals("02:00:00", FormatUtils.formatDuration(7200))
    }
}

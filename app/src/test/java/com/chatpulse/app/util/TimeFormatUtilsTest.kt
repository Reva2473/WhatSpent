package com.chatpulse.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormatUtilsTest {

    @Test
    fun testFormatDurationShort() {
        assertEquals("0m", TimeFormatUtils.formatDurationShort(0L))
        assertEquals("45s", TimeFormatUtils.formatDurationShort(45_000L))
        assertEquals("18m", TimeFormatUtils.formatDurationShort(18 * 60 * 1000L))
        assertEquals("2h 14m", TimeFormatUtils.formatDurationShort((2 * 3600 + 14 * 60) * 1000L))
        assertEquals("2h", TimeFormatUtils.formatDurationShort(2 * 3600 * 1000L))
    }

    @Test
    fun testFormatDurationCompact() {
        assertEquals("0s", TimeFormatUtils.formatDurationCompact(0L))
        assertEquals("45s", TimeFormatUtils.formatDurationCompact(45_000L))
        assertEquals("12m 30s", TimeFormatUtils.formatDurationCompact((12 * 60 + 30) * 1000L))
        assertEquals("1h 05m", TimeFormatUtils.formatDurationCompact((3600 + 5 * 60) * 1000L))
    }

    @Test
    fun testFormatTimerClock() {
        assertEquals("00:00", TimeFormatUtils.formatTimerClock(0L))
        assertEquals("04:12", TimeFormatUtils.formatTimerClock((4 * 60 + 12) * 1000L))
        assertEquals("1:04:12", TimeFormatUtils.formatTimerClock((3600 + 4 * 60 + 12) * 1000L))
    }

    @Test
    fun testCalculatePercentChange() {
        assertNull(TimeFormatUtils.calculatePercentChange(1000L, 0L))
        assertNull(TimeFormatUtils.calculatePercentChange(1000L, -500L))
        assertEquals(15, TimeFormatUtils.calculatePercentChange(1150L, 1000L))
        assertEquals(-20, TimeFormatUtils.calculatePercentChange(800L, 1000L))
        assertEquals(0, TimeFormatUtils.calculatePercentChange(1000L, 1000L))
    }

    @Test
    fun testFormatPercentComparison() {
        assertEquals("No data yesterday", TimeFormatUtils.formatPercentComparison(1000L, 0L))
        assertEquals("+15% vs yesterday", TimeFormatUtils.formatPercentComparison(1150L, 1000L))
        assertEquals("-20% vs yesterday", TimeFormatUtils.formatPercentComparison(800L, 1000L))
        assertEquals("0% vs yesterday", TimeFormatUtils.formatPercentComparison(1000L, 1000L))
    }

    @Test
    fun testIsoDateHelpers() {
        val today = TimeFormatUtils.todayDateString()
        val yesterday = TimeFormatUtils.yesterdayDateString()
        val twoDaysAgo = TimeFormatUtils.dateStringDaysAgo(2)

        assertNotNull(today)
        assertNotNull(yesterday)
        assertNotNull(twoDaysAgo)

        val readableToday = TimeFormatUtils.formatReadableDate(today)
        assert(readableToday.startsWith("Today,"))

        val readableYesterday = TimeFormatUtils.formatReadableDate(yesterday)
        assert(readableYesterday.startsWith("Yesterday,"))
    }
}

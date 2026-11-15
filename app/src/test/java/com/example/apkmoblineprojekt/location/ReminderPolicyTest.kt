package com.example.apkmoblineprojekt.location

import com.example.apkmoblineprojekt.data.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ReminderPolicyTest {

    private val night = AppSettings(quietHoursEnabled = true, quietFrom = LocalTime.of(22, 0), quietTo = LocalTime.of(7, 0))

    @Test
    fun quietHoursAcrossMidnight() {
        assertTrue(ReminderPolicy.inQuietHours(night, LocalTime.of(23, 30)))
        assertTrue(ReminderPolicy.inQuietHours(night, LocalTime.of(22, 0)))
        assertTrue(ReminderPolicy.inQuietHours(night, LocalTime.of(6, 59)))
        assertFalse(ReminderPolicy.inQuietHours(night, LocalTime.of(7, 0)))
        assertFalse(ReminderPolicy.inQuietHours(night, LocalTime.of(12, 0)))
    }

    @Test
    fun quietHoursWithinOneDay() {
        val afternoon = night.copy(quietFrom = LocalTime.of(13, 0), quietTo = LocalTime.of(15, 0))
        assertTrue(ReminderPolicy.inQuietHours(afternoon, LocalTime.of(14, 0)))
        assertFalse(ReminderPolicy.inQuietHours(afternoon, LocalTime.of(15, 0)))
        assertFalse(ReminderPolicy.inQuietHours(afternoon, LocalTime.of(10, 0)))
    }

    @Test
    fun quietHoursDisabled() {
        assertFalse(ReminderPolicy.inQuietHours(night.copy(quietHoursEnabled = false), LocalTime.of(23, 0)))
    }

    @Test
    fun remindsAgainOnlyAfterFourHours() {
        val now = 10 * ReminderPolicy.MIN_INTERVAL_MILLIS
        assertTrue(ReminderPolicy.intervalElapsed(null, now))
        assertFalse(ReminderPolicy.intervalElapsed(now - ReminderPolicy.MIN_INTERVAL_MILLIS + 1, now))
        assertTrue(ReminderPolicy.intervalElapsed(now - ReminderPolicy.MIN_INTERVAL_MILLIS, now))
    }

    @Test
    fun overLimitKeepsNearestDeadlines() {
        val zones = (1..5).map { zone(it, dueAt = it.toLong()) } + zone(6, dueAt = null)
        val selected = ReminderPolicy.selectForRegistration(zones.shuffled(), limit = 3)
        assertEquals(listOf("1", "2", "3"), selected.map { it.taskUid })
        assertEquals(6, ReminderPolicy.selectForRegistration(zones, limit = 100).size)
    }

    private fun zone(n: Int, dueAt: Long?) = GeofenceZone(
        requestId = "task:$n:point",
        taskUid = "$n",
        taskId = n.toLong(),
        taskTitle = "Zadanie $n",
        placeName = "Miejsce $n",
        latitude = 50.06,
        longitude = 19.94,
        radiusMeters = 300,
        dueAt = dueAt,
    )
}

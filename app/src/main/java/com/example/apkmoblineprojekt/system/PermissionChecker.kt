package com.example.apkmoblineprojekt.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class LocationAccess { NONE, WHILE_IN_USE, ALWAYS }

data class PermissionState(
    val notificationsEnabled: Boolean,
    val location: LocationAccess,
    val calendar: Boolean = false,
) {
    /** Geofencing wymaga lokalizacji „Zawsze” (HLD §8). */
    val remindersFullyWorking: Boolean
        get() = notificationsEnabled && location == LocationAccess.ALWAYS
}

interface PermissionChecker {
    fun current(): PermissionState
}

class AndroidPermissionChecker(private val context: Context) : PermissionChecker {
    override fun current(): PermissionState {
        val foreground = isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        val background = foreground && (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            )
        return PermissionState(
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            location = when {
                background -> LocationAccess.ALWAYS
                foreground -> LocationAccess.WHILE_IN_USE
                else -> LocationAccess.NONE
            },
            calendar = isGranted(Manifest.permission.READ_CALENDAR) &&
                isGranted(Manifest.permission.WRITE_CALENDAR),
        )
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

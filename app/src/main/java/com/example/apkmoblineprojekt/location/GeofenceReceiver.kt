package com.example.apkmoblineprojekt.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.apkmoblineprojekt.BliskoApplication
import com.example.apkmoblineprojekt.reminders.Notifications
import com.example.apkmoblineprojekt.reminders.launchAsync
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.flow.first
import java.time.LocalTime

/**
 * Odbiera zdarzenia DWELL (HLD §5 p. 3): pokazuje powiadomienie, jeśli zadanie jest nadal aktywne,
 * ostatnie przypomnienie było ponad 4 h temu i nie trwa cisza nocna.
 */
class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.w(TAG, "Błąd geofencingu: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_DWELL) return
        val requestIds = event.triggeringGeofences.orEmpty().map { it.requestId }
        if (requestIds.isEmpty()) return

        val container = (context.applicationContext as BliskoApplication).container
        launchAsync {
            val store = container.geofenceStore
            val settings = container.settingsRepository.settings.first()
            if (ReminderPolicy.inQuietHours(settings, LocalTime.now())) return@launchAsync
            val tasks = container.taskRepository.observeTasks().first()
            val now = System.currentTimeMillis()

            requestIds.mapNotNull(store::zone)
                .distinctBy { it.taskUid } // jedno powiadomienie na zadanie
                .forEach { zone ->
                    val task = tasks.firstOrNull { it.uid == zone.taskUid }
                    // Zadania nie ma w pamięci (np. proces zrestartowany) – ufamy strefie, która jest
                    // usuwana przy oznaczeniu jako zrobione / usunięciu.
                    if (task?.done == true) {
                        store.removeForTask(zone.taskUid)
                        container.geofenceHelper.sync()
                        return@forEach
                    }
                    if (!ReminderPolicy.intervalElapsed(store.lastNotifiedAt(zone.taskUid), now)) return@forEach
                    if (Notifications.showNearby(context, zone.copy(taskTitle = task?.title ?: zone.taskTitle))) {
                        store.setLastNotifiedAt(zone.taskUid, now)
                    }
                }
        }
    }

    companion object {
        const val ACTION = "com.example.apkmoblineprojekt.GEOFENCE_EVENT"
        private const val TAG = "GeofenceReceiver"
    }
}

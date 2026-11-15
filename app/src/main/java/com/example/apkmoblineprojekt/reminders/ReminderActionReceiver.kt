package com.example.apkmoblineprojekt.reminders

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.apkmoblineprojekt.BliskoApplication
import com.example.apkmoblineprojekt.location.ReminderPolicy
import kotlinx.coroutines.flow.first
import java.time.LocalTime

/**
 * Akcje z powiadomienia „W pobliżu” (F7):
 * - Zrobione – oznacza zadanie jako zrobione i usuwa jego strefy,
 * - Później – chowa powiadomienie i pokazuje je ponownie za godzinę.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: return
        val container = (context.applicationContext as BliskoApplication).container
        val store = container.geofenceStore
        val zone = store.zone(requestId) ?: return

        when (intent.action) {
            ACTION_DONE -> launchAsync {
                Notifications.cancel(context, zone.taskUid)
                val task = container.taskRepository.observeTasks().first().firstOrNull { it.uid == zone.taskUid }
                if (task != null) {
                    container.taskRepository.setDone(task.id, true) // dekorator usuwa strefy
                } else {
                    store.removeForTask(zone.taskUid)
                    container.geofenceHelper.sync()
                }
            }

            ACTION_LATER -> {
                Notifications.cancel(context, zone.taskUid)
                // Niedokładny alarm wystarczy (tolerancja kilku minut) i nie wymaga SCHEDULE_EXACT_ALARM.
                context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + SNOOZE_MILLIS,
                    Notifications.action(context, ACTION_SHOW_AGAIN, zone),
                )
            }

            ACTION_SHOW_AGAIN -> launchAsync {
                val task = container.taskRepository.observeTasks().first().firstOrNull { it.uid == zone.taskUid }
                if (task?.done == true) return@launchAsync
                val settings = container.settingsRepository.settings.first()
                if (ReminderPolicy.inQuietHours(settings, LocalTime.now())) return@launchAsync
                if (Notifications.showNearby(context, zone.copy(taskTitle = task?.title ?: zone.taskTitle))) {
                    store.setLastNotifiedAt(zone.taskUid, System.currentTimeMillis())
                }
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.example.apkmoblineprojekt.REMINDER_DONE"
        const val ACTION_LATER = "com.example.apkmoblineprojekt.REMINDER_LATER"
        const val ACTION_SHOW_AGAIN = "com.example.apkmoblineprojekt.REMINDER_SHOW_AGAIN"
        const val EXTRA_REQUEST_ID = "requestId"
        const val SNOOZE_MILLIS = 60 * 60 * 1000L
    }
}

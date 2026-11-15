package com.example.apkmoblineprojekt.reminders

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.apkmoblineprojekt.MainActivity
import com.example.apkmoblineprojekt.R
import com.example.apkmoblineprojekt.location.GeofenceZone

/** Kanały i powiadomienia z akcjami (HLD §8). */
object Notifications {
    const val CHANNEL_NEARBY = "nearby"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NEARBY,
                context.getString(R.string.notif_channel_nearby),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = context.getString(R.string.notif_channel_nearby_desc) },
        )
    }

    fun notificationId(taskUid: String) = taskUid.hashCode()

    /** „Jesteś blisko: Sklep budowlany – Kupić żarówki” z akcjami Zrobione / Później / Nawiguj. */
    @SuppressLint("MissingPermission") // sprawdzane przez areNotificationsEnabled()
    fun showNearby(context: Context, zone: GeofenceZone): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false

        val id = notificationId(zone.taskUid)
        val open = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val navigate = PendingIntent.getActivity(
            context,
            id,
            Intent(Intent.ACTION_VIEW, navigationUri(zone)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEARBY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_nearby_title, zone.placeName))
            .setContentText(zone.taskTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(zone.taskTitle))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(0, context.getString(R.string.notif_action_done), action(context, ReminderActionReceiver.ACTION_DONE, zone))
            .addAction(0, context.getString(R.string.notif_action_later), action(context, ReminderActionReceiver.ACTION_LATER, zone))
            .addAction(0, context.getString(R.string.notif_action_navigate), navigate)
            .build()

        return try {
            manager.notify(id, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun cancel(context: Context, taskUid: String) {
        NotificationManagerCompat.from(context).cancel(notificationId(taskUid))
    }

    private fun navigationUri(zone: GeofenceZone): Uri =
        Uri.parse("geo:0,0?q=${zone.latitude},${zone.longitude}(${Uri.encode(zone.placeName)})")

    fun action(context: Context, action: String, zone: GeofenceZone): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            // Osobny requestCode na akcję i zadanie, żeby PendingIntenty się nie nadpisywały.
            31 * notificationId(zone.taskUid) + action.hashCode(),
            Intent(context, ReminderActionReceiver::class.java)
                .setAction(action)
                .putExtra(ReminderActionReceiver.EXTRA_REQUEST_ID, zone.requestId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

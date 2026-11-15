package com.example.apkmoblineprojekt.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.apkmoblineprojekt.BliskoApplication

/** Po restarcie telefonu (albo aktualizacji aplikacji) system zapomina strefy – rejestrujemy je ponownie (N5). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        (context.applicationContext as BliskoApplication).container.geofenceHelper.sync()
    }
}

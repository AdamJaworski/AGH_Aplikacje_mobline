package com.example.apkmoblineprojekt.reminders

import android.content.BroadcastReceiver
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Praca w tle z [BroadcastReceiver.goAsync] – receiver ma ok. 10 s na zakończenie. */
fun BroadcastReceiver.launchAsync(block: suspend CoroutineScope.() -> Unit) {
    val pending = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            block()
        } catch (e: Exception) {
            Log.e("ReceiverAsync", "Błąd w ${this@launchAsync::class.simpleName}", e)
        } finally {
            pending.finish()
        }
    }
}

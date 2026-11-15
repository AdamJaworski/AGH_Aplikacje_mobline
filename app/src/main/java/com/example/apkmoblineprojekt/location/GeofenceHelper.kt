package com.example.apkmoblineprojekt.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/**
 * Rejestruje strefy z [GeofenceStore] w systemowym geofencingu (HLD §5 p. 2):
 * typ DWELL, `loiteringDelay = 60 s`, promień z zadania. Aplikacja sama nie odpytuje GPS (N1).
 *
 * [sync] zawsze odtwarza pełny zestaw stref ze [store] – jest idempotentne, więc wołamy je
 * po każdej zmianie zadania, po restarcie telefonu i po nadaniu uprawnień.
 */
class GeofenceHelper(
    context: Context,
    private val store: GeofenceStore,
) {
    private val context = context.applicationContext
    private val client = LocationServices.getGeofencingClient(this.context)

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(this.context, GeofenceReceiver::class.java).setAction(GeofenceReceiver.ACTION)
        // FLAG_MUTABLE jest wymagane – system dopisuje do intencji dane o zdarzeniu.
        PendingIntent.getBroadcast(
            this.context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    @SuppressLint("MissingPermission") // sprawdzane w hasLocationPermission()
    fun sync() {
        if (!hasLocationPermission()) {
            Log.i(TAG, "Brak uprawnień do lokalizacji – strefy zostaną zarejestrowane później")
            return
        }
        val zones = ReminderPolicy.selectForRegistration(store.zones())
        client.removeGeofences(pendingIntent).addOnCompleteListener {
            if (zones.isEmpty()) return@addOnCompleteListener
            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(zones.map { it.toGeofence() })
                .build()
            try {
                client.addGeofences(request, pendingIntent)
                    .addOnSuccessListener { Log.i(TAG, "Zarejestrowano strefy: ${zones.size}") }
                    // Np. GEOFENCE_NOT_AVAILABLE przy wyłączonej lokalizacji w telefonie.
                    .addOnFailureListener { Log.w(TAG, "Nie udało się zarejestrować stref", it) }
            } catch (e: SecurityException) {
                Log.w(TAG, "Uprawnienia cofnięte w trakcie rejestracji", e)
            }
        }
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private fun GeofenceZone.toGeofence(): Geofence = Geofence.Builder()
        .setRequestId(requestId)
        .setCircularRegion(latitude, longitude, radiusMeters.toFloat())
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
        .setLoiteringDelay(LOITERING_DELAY_MS)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .build()

    companion object {
        private const val TAG = "GeofenceHelper"
        /** F6: powiadomienie po 60 s przebywania w strefie. */
        const val LOITERING_DELAY_MS = 60_000
    }
}

package com.example.apkmoblineprojekt.location

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Strefa zarejestrowana dla zadania. Id w formacie `task:<uid zadania>:<placeId | point>` –
 * zamiast `id` (HLD §5) używamy `uid`, bo id z repozytorium w pamięci nie są stałe między
 * uruchomieniami procesu.
 */
data class GeofenceZone(
    val requestId: String,
    val taskUid: String,
    val taskId: Long,
    val taskTitle: String,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int,
    /** Termin zadania (epoch millis) – do wyboru stref przy limicie 100. */
    val dueAt: Long? = null,
)

/**
 * Trwała lista stref i czasu ostatniego przypomnienia (odpowiednik `TaskPlace` i
 * `Task.lastNotifiedAt` z HLD §7). Zadania trzymamy na razie w pamięci, więc gdy system wybudzi
 * aplikację zdarzeniem geofence albo po restarcie telefonu, dane o strefie bierzemy stąd.
 * Po przejściu na Room można to przenieść do bazy.
 */
class GeofenceStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("geofences", Context.MODE_PRIVATE)
    private val lock = Any()

    fun zones(): List<GeofenceZone> = synchronized(lock) { readZones() }

    fun zone(requestId: String): GeofenceZone? = zones().firstOrNull { it.requestId == requestId }

    /** Podmienia wszystkie strefy danego zadania. */
    fun replaceForTask(taskUid: String, zones: List<GeofenceZone>) = synchronized(lock) {
        writeZones(readZones().filterNot { it.taskUid == taskUid } + zones)
    }

    fun removeForTask(taskUid: String) = synchronized(lock) {
        writeZones(readZones().filterNot { it.taskUid == taskUid })
        prefs.edit().remove(lastKey(taskUid)).apply()
    }

    fun lastNotifiedAt(taskUid: String): Long? =
        prefs.getLong(lastKey(taskUid), -1L).takeIf { it >= 0 }

    fun setLastNotifiedAt(taskUid: String, millis: Long) {
        prefs.edit().putLong(lastKey(taskUid), millis).apply()
    }

    private fun lastKey(taskUid: String) = "last:$taskUid"

    private fun readZones(): List<GeofenceZone> {
        val raw = prefs.getString(KEY_ZONES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { array.getJSONObject(it).toZone() }
        }.getOrDefault(emptyList())
    }

    private fun writeZones(zones: List<GeofenceZone>) {
        val array = JSONArray()
        zones.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_ZONES, array.toString()).apply()
    }

    private fun GeofenceZone.toJson() = JSONObject()
        .put("requestId", requestId)
        .put("taskUid", taskUid)
        .put("taskId", taskId)
        .put("taskTitle", taskTitle)
        .put("placeName", placeName)
        .put("lat", latitude)
        .put("lng", longitude)
        .put("radius", radiusMeters)
        .apply { dueAt?.let { put("dueAt", it) } }

    private fun JSONObject.toZone() = GeofenceZone(
        requestId = getString("requestId"),
        taskUid = getString("taskUid"),
        taskId = getLong("taskId"),
        taskTitle = getString("taskTitle"),
        placeName = getString("placeName"),
        latitude = getDouble("lat"),
        longitude = getDouble("lng"),
        radiusMeters = getInt("radius"),
        dueAt = if (has("dueAt")) getLong("dueAt") else null,
    )

    private companion object {
        const val KEY_ZONES = "zones"
    }
}

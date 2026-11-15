package com.example.apkmoblineprojekt.location

import android.content.Context
import android.location.Geocoder
import com.example.apkmoblineprojekt.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class GeocodedPlace(val address: String, val lat: Double, val lng: Double)

class GeocodingRepository(private val context: Context) {

    suspend fun geocode(query: String): GeocodedPlace? {
        val q = query.trim()
        if (q.isEmpty()) return null
        return viaGoogle(q) ?: viaAndroidGeocoder(q)
    }

    private suspend fun viaGoogle(q: String): GeocodedPlace? = withContext(Dispatchers.IO) {
        if (BuildConfig.MAPS_API_KEY.isBlank()) return@withContext null
        try {
            val url = "https://maps.googleapis.com/maps/api/geocode/json" +
                    "?address=${URLEncoder.encode(q, "UTF-8")}" +
                    "&language=pl&region=pl&key=${BuildConfig.MAPS_API_KEY}"

            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 8_000
            }
            val body = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }

            val json = JSONObject(body)
            if (json.getString("status") != "OK") return@withContext null

            val first = json.getJSONArray("results").getJSONObject(0)
            val loc = first.getJSONObject("geometry").getJSONObject("location")
            GeocodedPlace(
                address = first.getString("formatted_address"),
                lat = loc.getDouble("lat"),
                lng = loc.getDouble("lng"),
            )
        } catch (e: Exception) {   // IOException, JSONException
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun viaAndroidGeocoder(q: String): GeocodedPlace? =
        withContext(Dispatchers.IO) {
            try {
                Geocoder(context, Locale("pl", "PL"))
                    .getFromLocationName(q, 1)
                    ?.firstOrNull()
                    ?.let { GeocodedPlace(it.getAddressLine(0) ?: q, it.latitude, it.longitude) }
            } catch (e: IOException) {
                null
            }
        }
}
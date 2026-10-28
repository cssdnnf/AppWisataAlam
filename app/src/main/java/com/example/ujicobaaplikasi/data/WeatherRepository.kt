package com.example.ujicobaaplikasi.data

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.roundToInt

enum class WeatherKind {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    RAIN,
    THUNDERSTORM,
    FOG,
}

data class WeatherNow(
    val temperatureC: Int,
    val feelsLikeC: Int,
    val condition: String,
    val humidityPercent: Int,
    val windKph: Int,
    val uvIndex: Double,
    val isDay: Boolean,
    val kind: WeatherKind,
)

/**
 * Cuaca terkini untuk area Lombok dari Open-Meteo.
 *
 * Open-Meteo dipakai karena menyediakan suhu aktual, suhu terasa, kelembapan,
 * angin, dan indeks UV dalam satu respons tanpa API key. Data diperbarui
 * sekitar setiap 15 menit oleh penyedia.
 */
object WeatherRepository {

    // Titik tengah Mataram, Lombok.
    private const val LATITUDE = -8.5833
    private const val LONGITUDE = 116.1167
    private const val ENDPOINT = "https://api.open-meteo.com/v1/forecast"
    private const val TIMEOUT_MS = 10_000

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetch(callback: (Result<WeatherNow>) -> Unit) {
        executor.execute {
            val result = runCatching { requestWeather() }
            mainHandler.post { callback(result) }
        }
    }

    private fun requestWeather(): WeatherNow {
        val url = URL(
            "$ENDPOINT?latitude=$LATITUDE&longitude=$LONGITUDE" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
                "is_day,weather_code,wind_speed_10m,uv_index" +
                "&wind_speed_unit=kmh&timezone=Asia%2FMakassar",
        )
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.useCaches = false
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "JelajahLombok/1.0 (Android)")

            val status = connection.responseCode
            if (status !in 200..299) {
                val detail = connection.errorStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
                throw IOException("Open-Meteo HTTP $status $detail")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            val weatherCode = current.getInt("weather_code")
            val description = describe(weatherCode)

            return WeatherNow(
                temperatureC = current.getDouble("temperature_2m").roundToInt(),
                feelsLikeC = current.getDouble("apparent_temperature").roundToInt(),
                condition = description.first,
                humidityPercent = current.getInt("relative_humidity_2m"),
                windKph = current.getDouble("wind_speed_10m").roundToInt(),
                uvIndex = current.optDouble("uv_index", 0.0),
                isDay = current.optInt("is_day", 1) == 1,
                kind = description.second,
            )
        } finally {
            connection.disconnect()
        }
    }

    /** WMO weather code dari Open-Meteo ke deskripsi Indonesia + jenis ikon. */
    private fun describe(code: Int): Pair<String, WeatherKind> = when (code) {
        0 -> "Cerah" to WeatherKind.CLEAR
        1 -> "Sebagian besar cerah" to WeatherKind.CLEAR
        2 -> "Cerah berawan" to WeatherKind.PARTLY_CLOUDY
        3 -> "Berawan" to WeatherKind.CLOUDY
        45, 48 -> "Berkabut" to WeatherKind.FOG
        51 -> "Gerimis ringan" to WeatherKind.RAIN
        53 -> "Gerimis" to WeatherKind.RAIN
        55 -> "Gerimis lebat" to WeatherKind.RAIN
        56, 57 -> "Gerimis membeku" to WeatherKind.RAIN
        61 -> "Hujan ringan" to WeatherKind.RAIN
        63 -> "Hujan sedang" to WeatherKind.RAIN
        65 -> "Hujan lebat" to WeatherKind.RAIN
        66, 67 -> "Hujan membeku" to WeatherKind.RAIN
        71 -> "Salju ringan" to WeatherKind.CLOUDY
        73 -> "Salju sedang" to WeatherKind.CLOUDY
        75 -> "Salju lebat" to WeatherKind.CLOUDY
        77 -> "Butir salju" to WeatherKind.CLOUDY
        80 -> "Hujan lokal ringan" to WeatherKind.RAIN
        81 -> "Hujan lokal sedang" to WeatherKind.RAIN
        82 -> "Hujan lokal lebat" to WeatherKind.RAIN
        85, 86 -> "Hujan salju" to WeatherKind.CLOUDY
        95 -> "Badai petir" to WeatherKind.THUNDERSTORM
        96, 99 -> "Badai petir dan hujan es" to WeatherKind.THUNDERSTORM
        else -> "Cuaca tidak diketahui" to WeatherKind.CLOUDY
    }
}

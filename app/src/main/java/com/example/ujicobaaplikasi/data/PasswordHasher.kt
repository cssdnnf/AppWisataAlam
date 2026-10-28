package com.example.ujicobaaplikasi.data

import com.example.ujicobaaplikasi.R
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {

    fun randomSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$password".toByteArray(Charsets.UTF_8))
        return bytes.toHex()
    }

    fun resetCodeHash(code: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(code.toByteArray(Charsets.UTF_8)).toHex()
    }

    fun resetCode(): String = "%06d".format(SecureRandom().nextInt(1_000_000))

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

/** Menjembatani kunci teks dari database dengan resource drawable/warna lokal. */
object Catalog {

    data class Option(val value: String, val label: String)

    private val images = linkedMapOf(
        "foto_rinjani" to R.drawable.foto_rinjani,
        "foto_tanjung_aan" to R.drawable.foto_tanjung_aan,
        "foto_gili_trawangan" to R.drawable.foto_gili_trawangan,
        "foto_pantai_lombok" to R.drawable.foto_pantai_lombok,
        "foto_air_terjun_lombok" to R.drawable.foto_air_terjun_lombok,
        "foto_sade" to R.drawable.foto_sade,
        "foto_bukit_merese" to R.drawable.foto_bukit_merese,
        "kuliner_ayam_taliwang" to R.drawable.kuliner_ayam_taliwang,
        "kuliner_plecing_kangkung" to R.drawable.kuliner_plecing_kangkung,
        "kuliner_sate_bulayak" to R.drawable.kuliner_sate_bulayak,
        "stay_bungalow_gili" to R.drawable.stay_bungalow_gili,
        "stay_lodge_rinjani" to R.drawable.stay_lodge_rinjani,
        "stay_homestay_sasak" to R.drawable.stay_homestay_sasak,
        "stay_resort_tanjung_aan" to R.drawable.stay_resort_tanjung_aan,
        "activity_trekking_rinjani" to R.drawable.activity_trekking_rinjani,
        "activity_snorkeling_gili" to R.drawable.activity_snorkeling_gili,
        "activity_surfing_kuta" to R.drawable.activity_surfing_kuta,
        "activity_tenun_sade" to R.drawable.activity_tenun_sade,
        "activity_sunrise_merese" to R.drawable.activity_sunrise_merese,
        "event_bau_nyale" to R.drawable.event_bau_nyale,
        "event_gendang_beleq" to R.drawable.event_gendang_beleq,
        "event_presean" to R.drawable.event_presean,
        "event_nyongkolan" to R.drawable.event_nyongkolan,
    )

    private val imageLabels = linkedMapOf(
        "foto_rinjani" to "Gunung Rinjani",
        "foto_tanjung_aan" to "Tanjung Aan",
        "foto_gili_trawangan" to "Gili Trawangan",
        "foto_pantai_lombok" to "Pantai Kuta",
        "foto_air_terjun_lombok" to "Air Terjun Senaru",
        "foto_sade" to "Desa Sade",
        "foto_bukit_merese" to "Bukit Merese",
        "kuliner_ayam_taliwang" to "Ayam Taliwang",
        "kuliner_plecing_kangkung" to "Plecing Kangkung",
        "kuliner_sate_bulayak" to "Sate Bulayak",
        "stay_bungalow_gili" to "Bungalow tepi pantai Gili",
        "stay_lodge_rinjani" to "Lodge kaki Rinjani",
        "stay_homestay_sasak" to "Kamar homestay Sasak",
        "stay_resort_tanjung_aan" to "Resort dengan kolam",
        "activity_trekking_rinjani" to "Trekking Rinjani",
        "activity_snorkeling_gili" to "Snorkeling di Gili",
        "activity_surfing_kuta" to "Surfing di Lombok",
        "activity_tenun_sade" to "Workshop tenun ikat",
        "activity_sunrise_merese" to "Sunrise Bukit Merese",
        "event_bau_nyale" to "Festival Bau Nyale",
        "event_gendang_beleq" to "Gendang Beleq",
        "event_presean" to "Presean",
        "event_nyongkolan" to "Nyongkolan",
    )

    private val icons = linkedMapOf(
        "ic_menu_destination" to R.drawable.ic_menu_destination,
        "ic_menu_culinary" to R.drawable.ic_menu_culinary,
        "ic_menu_stay" to R.drawable.ic_menu_stay,
        "ic_menu_activity" to R.drawable.ic_menu_activity,
        "ic_menu_event" to R.drawable.ic_menu_event,
        "ic_menu_trip" to R.drawable.ic_menu_trip,
        "ic_water_drop" to R.drawable.ic_water_drop,
        "ic_weather_sun" to R.drawable.ic_weather_sun,
        "ic_location" to R.drawable.ic_location,
        "ic_notifications" to R.drawable.ic_notifications,
        "ic_star" to R.drawable.ic_star,
        "ic_heart" to R.drawable.ic_heart,
        "ic_calendar" to R.drawable.ic_calendar,
        "ic_clock" to R.drawable.ic_clock,
    )

    private val iconLabels = linkedMapOf(
        "ic_menu_destination" to "Destinasi",
        "ic_menu_culinary" to "Kuliner",
        "ic_menu_stay" to "Penginapan",
        "ic_menu_activity" to "Aktivitas",
        "ic_menu_event" to "Event budaya",
        "ic_menu_trip" to "Rencana trip",
        "ic_water_drop" to "Air / snorkeling",
        "ic_weather_sun" to "Matahari",
        "ic_location" to "Lokasi",
        "ic_notifications" to "Notifikasi",
        "ic_star" to "Bintang",
        "ic_heart" to "Hati",
        "ic_calendar" to "Kalender",
        "ic_clock" to "Jam",
    )

    private val colors = linkedMapOf(
        "hijau_lombok" to R.color.hijau_lombok,
        "hijau_lombok_muda" to R.color.hijau_lombok_muda,
        "hijau_lombok_gelap" to R.color.hijau_lombok_gelap,
        "koral" to R.color.koral,
        "koral_gelap" to R.color.koral_gelap,
        "koral_lembut" to R.color.koral_lembut,
        "lavender" to R.color.lavender,
        "lavender_lembut" to R.color.lavender_lembut,
        "biru" to R.color.biru,
        "biru_lembut" to R.color.biru_lembut,
        "coklat_tua" to R.color.coklat_tua,
        "pasir_kuning" to R.color.pasir_kuning,
        "merah" to R.color.merah,
        "merah_lembut" to R.color.merah_lembut,
        "teks_sekunder" to R.color.teks_sekunder,
    )

    private val colorLabels = linkedMapOf(
        "hijau_lombok" to "Hijau Lombok",
        "hijau_lombok_muda" to "Hijau muda",
        "hijau_lombok_gelap" to "Hijau gelap",
        "koral" to "Koral",
        "koral_gelap" to "Koral gelap",
        "koral_lembut" to "Koral lembut",
        "lavender" to "Lavender",
        "lavender_lembut" to "Lavender lembut",
        "biru" to "Biru",
        "biru_lembut" to "Biru lembut",
        "coklat_tua" to "Cokelat tua",
        "pasir_kuning" to "Pasir kuning",
        "merah" to "Merah",
        "merah_lembut" to "Merah lembut",
        "teks_sekunder" to "Abu hijau",
    )

    val imageOptions: List<Option> = images.keys.map { Option(it, imageLabel(it)) }
    val iconOptions: List<Option> = icons.keys.map { Option(it, iconLabel(it)) }
    val colorOptions: List<Option> = colors.keys.map { Option(it, colorLabel(it)) }

    fun image(key: String?): Int =
        images[key] ?: images.getValue("foto_pantai_lombok")

    fun icon(key: String?): Int =
        icons[key] ?: R.drawable.ic_menu_destination

    fun color(key: String?): Int =
        colors[key] ?: R.color.hijau_lombok

    fun imageLabel(key: String?): String =
        imageLabels[key] ?: key?.replace('_', ' ')?.replaceFirstChar { it.titlecase() }
            ?: "Gambar"

    fun iconLabel(key: String?): String =
        iconLabels[key] ?: key?.replace('_', ' ')?.replaceFirstChar { it.titlecase() }
            ?: "Ikon"

    fun colorLabel(key: String?): String =
        colorLabels[key] ?: key?.replace('_', ' ')?.replaceFirstChar { it.titlecase() }
            ?: "Warna"
}

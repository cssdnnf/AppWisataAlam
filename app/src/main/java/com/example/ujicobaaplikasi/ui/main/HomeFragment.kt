package com.example.ujicobaaplikasi.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ujicobaaplikasi.R
import com.example.ujicobaaplikasi.ui.common.AppNotice
import com.example.ujicobaaplikasi.data.AppData
import com.example.ujicobaaplikasi.data.AuthStore
import com.example.ujicobaaplikasi.data.Culinary
import com.example.ujicobaaplikasi.data.Destination
import com.example.ujicobaaplikasi.data.SavedType
import com.example.ujicobaaplikasi.data.TravelRepository
import com.example.ujicobaaplikasi.data.WeatherKind
import com.example.ujicobaaplikasi.data.WeatherNow
import com.example.ujicobaaplikasi.data.WeatherRepository
import com.example.ujicobaaplikasi.ui.adapters.CulinaryAdapter
import com.example.ujicobaaplikasi.ui.adapters.FeaturedDestinationAdapter
import com.example.ujicobaaplikasi.ui.adapters.MenuAdapter
import com.example.ujicobaaplikasi.ui.auth.LoginActivity
import com.example.ujicobaaplikasi.ui.common.AvatarView
import com.example.ujicobaaplikasi.ui.detail.CulinaryDetailActivity
import com.example.ujicobaaplikasi.ui.detail.DestinationDetailActivity
import com.example.ujicobaaplikasi.ui.detail.MenuDetailActivity
import com.example.ujicobaaplikasi.ui.detail.NotificationsActivity
import com.example.ujicobaaplikasi.ui.trip.TripPlansActivity
import com.example.ujicobaaplikasi.ui.navigation.BottomNavDestination
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var destinationAdapter: FeaturedDestinationAdapter? = null
    private var culinaryAdapter: CulinaryAdapter? = null
    private var avatar: AvatarView? = null
    private var textNama: TextView? = null
    private var textGreeting: TextView? = null
    private var dotNotifikasi: View? = null
    private var weatherCard: View? = null
    private var weatherTemp: TextView? = null
    private var weatherCondition: TextView? = null
    private var weatherFeels: TextView? = null
    private var weatherHumidity: TextView? = null
    private var weatherWind: TextView? = null
    private var weatherUv: TextView? = null
    private var weatherIcon: ImageView? = null
    private var weatherLoading = false
    private var weatherHasData = false
    private var weatherLoadedAt = 0L
    private var weatherRequestToken = 0

    private companion object {
        private const val TAG = "HomeFragment"
        private const val WEATHER_REFRESH_INTERVAL_MS = 10 * 60 * 1000L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        avatar = view.findViewById(R.id.homeAvatar)
        textNama = view.findViewById(R.id.textNama)
        textGreeting = view.findViewById(R.id.textGreeting)
        dotNotifikasi = view.findViewById(R.id.dotNotifikasi)

        view.findViewById<TextView>(R.id.textTanggal).text = formattedToday()
        refreshProfile()
        refreshNotificationDot()

        bindWeatherViews(view)
        bindQuickMenu(view)
        bindDestinations(view)
        bindCulinary(view)
        bindActions(view)
    }

    override fun onResume() {
        super.onResume()
        refreshProfile()
        refreshNotificationDot()
        refreshWeather(force = false)
        destinationAdapter?.updateFavorites(AppData.favoriteIds())
        culinaryAdapter?.updateFavorites(AppData.savedIds(SavedType.CULINARY))
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            refreshProfile()
            refreshNotificationDot()
            refreshWeather(force = false)
            destinationAdapter?.updateFavorites(AppData.favoriteIds())
            culinaryAdapter?.updateFavorites(AppData.savedIds(SavedType.CULINARY))
        }
    }

    private fun bindQuickMenu(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.homeMenuRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.itemAnimator = null
        val adapter = MenuAdapter { menu ->
            if (menu.id == "trip") {
                TripPlansActivity.open(requireContext())
            } else {
                MenuDetailActivity.open(requireContext(), menu.id)
            }
        }
        recycler.adapter = adapter
        adapter.submitList(TravelRepository.menus)
    }

    private fun bindDestinations(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.homeDestinationRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.itemAnimator = null
        val adapter = FeaturedDestinationAdapter(
            onOpen = { destination ->
                DestinationDetailActivity.open(requireContext(), destination.id)
            },
            onFavoriteToggle = ::toggleFavorite,
        )
        recycler.adapter = adapter
        destinationAdapter = adapter
        adapter.submitList(
            TravelRepository.featuredDestinations(),
            AppData.favoriteIds(),
        )
    }

    private fun bindCulinary(view: View) {
        val recycler = view.findViewById<RecyclerView>(R.id.homeCulinaryRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.itemAnimator = null
        val adapter = CulinaryAdapter(
            onOpen = { culinary ->
                CulinaryDetailActivity.open(requireContext(), culinary.id)
            },
            onFavoriteToggle = ::toggleCulinaryFavorite,
        )
        culinaryAdapter = adapter
        recycler.adapter = adapter
        adapter.submitList(
            TravelRepository.culinary,
            AppData.savedIds(SavedType.CULINARY),
        )
    }

    private fun bindActions(view: View) {
        view.findViewById<View>(R.id.homeAvatar).setOnClickListener { openProfileTab() }
        view.findViewById<View>(R.id.textNama).setOnClickListener { openProfileTab() }

        view.findViewById<View>(R.id.tombolNotifikasi).setOnClickListener {
            startActivity(
                Intent(requireContext(), NotificationsActivity::class.java)
                    .putExtra(
                        NotificationsActivity.EXTRA_NAV_DESTINATION,
                        BottomNavDestination.HOME.name,
                    ),
            )
        }
        view.findViewById<View>(R.id.tombolLogoutHeader).setOnClickListener { confirmLogout() }

        view.findViewById<View>(R.id.tombolLihatSemuaDestinasi).setOnClickListener {
            MenuDetailActivity.open(requireContext(), "destination")
        }
        view.findViewById<View>(R.id.tombolLihatSemuaKuliner).setOnClickListener {
            MenuDetailActivity.open(requireContext(), "culinary")
        }
        view.findViewById<View>(R.id.tombolPromoTrip).setOnClickListener {
            TripPlansActivity.open(requireContext())
        }
    }

    private fun bindWeatherViews(view: View) {
        weatherCard = view.findViewById(R.id.weatherCard)
        weatherTemp = view.findViewById(R.id.textWeatherTemp)
        weatherCondition = view.findViewById(R.id.textWeatherCondition)
        weatherFeels = view.findViewById(R.id.textWeatherFeels)
        weatherHumidity = view.findViewById(R.id.textWeatherHumidity)
        weatherWind = view.findViewById(R.id.textWeatherWind)
        weatherUv = view.findViewById(R.id.textWeatherUv)
        weatherIcon = view.findViewById(R.id.imageWeatherIcon)
        weatherCard?.setOnClickListener { refreshWeather(force = true) }
    }

    private fun refreshWeather(force: Boolean) {
        if (weatherLoading) return
        if (!force && weatherHasData &&
            SystemClock.elapsedRealtime() - weatherLoadedAt < WEATHER_REFRESH_INTERVAL_MS
        ) {
            return
        }

        weatherLoading = true
        val token = ++weatherRequestToken
        if (!weatherHasData) showWeatherLoading()

        WeatherRepository.fetch { result ->
            if (token != weatherRequestToken) return@fetch
            weatherLoading = false
            if (view == null) return@fetch

            result.fold(
                onSuccess = { weather ->
                    weatherHasData = true
                    weatherLoadedAt = SystemClock.elapsedRealtime()
                    renderWeather(weather)
                },
                onFailure = { error ->
                    Log.w(TAG, "Gagal memuat cuaca", error)
                    if (!weatherHasData) showWeatherUnavailable()
                },
            )
        }
    }

    private fun showWeatherLoading() {
        weatherTemp?.setText(R.string.weather_temp_placeholder)
        weatherCondition?.setText(R.string.weather_loading)
        weatherFeels?.setText(R.string.weather_value_placeholder)
        weatherHumidity?.setText(R.string.weather_value_placeholder)
        weatherWind?.setText(R.string.weather_value_placeholder)
        weatherUv?.setText(R.string.weather_value_placeholder)
        weatherCard?.contentDescription = getString(R.string.cd_weather_loading)
    }

    private fun showWeatherUnavailable() {
        weatherTemp?.setText(R.string.weather_temp_placeholder)
        weatherCondition?.setText(R.string.weather_unavailable)
        weatherFeels?.setText(R.string.weather_retry)
        weatherHumidity?.setText(R.string.weather_value_placeholder)
        weatherWind?.setText(R.string.weather_value_placeholder)
        weatherUv?.setText(R.string.weather_value_placeholder)
        weatherIcon?.setImageResource(R.drawable.ic_weather_cloud)
        weatherCard?.contentDescription = getString(R.string.cd_weather_unavailable)
    }

    private fun renderWeather(weather: WeatherNow) {
        weatherTemp?.text = getString(R.string.weather_temp_format, weather.temperatureC)
        weatherCondition?.text = weather.condition
        weatherFeels?.text = getString(R.string.weather_feels_format, weather.feelsLikeC)
        weatherHumidity?.text =
            getString(R.string.weather_humidity_format, weather.humidityPercent)
        weatherWind?.text = getString(R.string.weather_wind_format, weather.windKph)
        weatherUv?.text = getString(
            R.string.weather_uv_format,
            getString(uvLabelRes(weather.uvIndex)),
            formattedUv(weather.uvIndex),
        )
        weatherIcon?.setImageResource(weatherIconRes(weather.kind, weather.isDay))
        weatherCard?.contentDescription = getString(
            R.string.cd_weather_summary,
            weather.condition,
            weather.temperatureC,
            weather.feelsLikeC,
            weather.humidityPercent,
            weather.windKph,
            getString(uvLabelRes(weather.uvIndex)),
        )
    }

    private fun formattedUv(value: Double): String {
        val rounded = value.roundToInt()
        return if (abs(value - rounded) < 0.05) {
            rounded.toString()
        } else {
            String.format(Locale.forLanguageTag("id-ID"), "%.1f", value)
        }
    }

    @StringRes
    private fun uvLabelRes(uvIndex: Double): Int = when {
        uvIndex < 3 -> R.string.weather_uv_low
        uvIndex < 6 -> R.string.weather_uv_moderate
        uvIndex < 8 -> R.string.weather_uv_high
        uvIndex < 11 -> R.string.weather_uv_very_high
        else -> R.string.weather_uv_extreme
    }

    @DrawableRes
    private fun weatherIconRes(kind: WeatherKind, isDay: Boolean): Int = when (kind) {
        WeatherKind.CLEAR ->
            if (isDay) R.drawable.ic_weather_sun else R.drawable.ic_weather_moon
        WeatherKind.PARTLY_CLOUDY ->
            if (isDay) R.drawable.ic_weather_partly_cloudy
            else R.drawable.ic_weather_night_cloudy
        WeatherKind.CLOUDY -> R.drawable.ic_weather_cloud
        WeatherKind.RAIN -> R.drawable.ic_weather_rain
        WeatherKind.THUNDERSTORM -> R.drawable.ic_weather_thunderstorm
        WeatherKind.FOG -> R.drawable.ic_weather_fog
    }

    private fun toggleFavorite(destination: Destination) {
        AppData.toggleFavorite(requireContext(), destination.id) { result ->
            result.fold(
                onSuccess = { nowFavorite ->
                    destinationAdapter?.updateFavorites(AppData.favoriteIds())
                    if (nowFavorite) {
                        AppNotice.success(requireActivity(), R.string.favorite_added)
                    } else {
                        AppNotice.info(requireActivity(), R.string.favorite_removed)
                    }
                },
                onFailure = {
                    AppNotice.error(requireActivity(), R.string.error_network)
                },
            )
        }
    }

    private fun toggleCulinaryFavorite(culinary: Culinary) {
        AppData.toggleSaved(requireContext(), SavedType.CULINARY, culinary.id) { result ->
            result.fold(
                onSuccess = { nowSaved ->
                    culinaryAdapter?.updateFavorites(AppData.savedIds(SavedType.CULINARY))
                    if (nowSaved) {
                        AppNotice.success(requireActivity(), R.string.favorite_added)
                    } else {
                        AppNotice.info(requireActivity(), R.string.favorite_removed)
                    }
                },
                onFailure = { AppNotice.error(requireActivity(), R.string.error_network) },
            )
        }
    }

    private fun refreshProfile() {
        val account = AuthStore.currentAccount(requireContext())
        val name = account?.name?.takeIf { it.isNotBlank() } ?: getString(R.string.profile_tagline)
        avatar?.setProfile(name, account?.avatarUri)
        textNama?.text = name
        textGreeting?.setText(greetingForNow())
    }

    private fun refreshNotificationDot() {
        dotNotifikasi?.visibility =
            if (AppData.hasUnreadNotifications()) View.VISIBLE else View.GONE
    }

    private fun greetingForNow(): Int {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..10 -> R.string.home_greeting_morning
            in 11..14 -> R.string.home_greeting_afternoon
            in 15..17 -> R.string.home_greeting_evening
            else -> R.string.home_greeting_night
        }
    }

    private fun formattedToday(): String {
        val locale = Locale("id", "ID")
        val formatted = SimpleDateFormat("EEEE, d MMMM", locale).format(Date())
        return formatted.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(locale) else char.toString()
        }
    }

    private fun openProfileTab() {
        (activity as? MainActivity)?.showTab(BottomNavDestination.PROFILE)
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.logout_confirm) { _, _ ->
                AuthStore.logout(requireContext())
                AppNotice.deferSuccess(requireActivity(), R.string.logout_success)
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                )
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        weatherRequestToken++
        weatherLoading = false
        weatherHasData = false
        weatherLoadedAt = 0L
        destinationAdapter = null
        culinaryAdapter = null
        avatar = null
        textNama = null
        textGreeting = null
        dotNotifikasi = null
        weatherCard = null
        weatherTemp = null
        weatherCondition = null
        weatherFeels = null
        weatherHumidity = null
        weatherWind = null
        weatherUv = null
        weatherIcon = null
    }
}

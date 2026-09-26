package com.liquidglass

import android.content.Context
import com.liquidglass.shared.AppSettings
import com.liquidglass.shared.GestureConfig
import com.liquidglass.shared.MiniPlayerSize
import com.liquidglass.shared.NavigationStyle
import com.liquidglass.shared.NavigationSize
import com.liquidglass.shared.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val defaults = AppSettings()
        val gestures = defaults.gestures
        return AppSettings(
            gestures = GestureConfig(
                miniPlayerSwipe = prefs.getBoolean("mini_player_swipe", gestures.miniPlayerSwipe),
                playerSwipeDown = prefs.getBoolean("player_swipe_down", gestures.playerSwipeDown),
                miniPlayerLongPress = prefs.getBoolean("mini_player_long_press", gestures.miniPlayerLongPress),
                sensitivityDp = prefs.getFloat("sensitivity_dp", gestures.sensitivityDp).coerceIn(40f, 160f),
            ),
            glassIntensity = prefs.getFloat("glass_intensity", defaults.glassIntensity).coerceIn(0f, 1f),
            smartShuffle = prefs.getBoolean("smart_shuffle", defaults.smartShuffle),
            themePreference = runCatching {
                ThemePreference.valueOf(prefs.getString("theme_preference", null) ?: defaults.themePreference.name)
            }.getOrDefault(defaults.themePreference),
            miniPlayerSize = runCatching {
                MiniPlayerSize.valueOf(prefs.getString("mini_player_size", null) ?: defaults.miniPlayerSize.name)
            }.getOrDefault(defaults.miniPlayerSize),
            navigationSize = runCatching {
                NavigationSize.valueOf(prefs.getString("navigation_size", null) ?: defaults.navigationSize.name)
            }.getOrDefault(defaults.navigationSize),
            navigationStyle = runCatching {
                NavigationStyle.valueOf(prefs.getString("navigation_style", null) ?: defaults.navigationStyle.name)
            }.getOrDefault(defaults.navigationStyle),
            searchInNavigation = prefs.getBoolean("search_in_navigation", defaults.searchInNavigation),
            navigationLabels = prefs.getBoolean("navigation_labels", defaults.navigationLabels),
        )
    }

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean("mini_player_swipe", settings.gestures.miniPlayerSwipe)
            .putBoolean("player_swipe_down", settings.gestures.playerSwipeDown)
            .putBoolean("mini_player_long_press", settings.gestures.miniPlayerLongPress)
            .putFloat("sensitivity_dp", settings.gestures.sensitivityDp.coerceIn(40f, 160f))
            .putFloat("glass_intensity", settings.glassIntensity.coerceIn(0f, 1f))
            .putBoolean("smart_shuffle", settings.smartShuffle)
            .putString("theme_preference", settings.themePreference.name)
            .putString("mini_player_size", settings.miniPlayerSize.name)
            .putString("navigation_size", settings.navigationSize.name)
            .putString("navigation_style", settings.navigationStyle.name)
            .putBoolean("search_in_navigation", settings.searchInNavigation)
            .putBoolean("navigation_labels", settings.navigationLabels)
            .apply()
    }
}

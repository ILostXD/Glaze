package com.glaze


import android.content.Context
import com.glaze.shared.AppSettings
import com.glaze.shared.GestureConfig
import com.glaze.shared.MiniPlayerSize
import com.glaze.shared.NavigationStyle
import com.glaze.shared.NavigationSize
import com.glaze.shared.ThemePreference
import com.glaze.shared.restoreHomeLayout
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SettingsStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    fun hasCompanionAddress(): Boolean = prefs.contains("companion_url")

    fun load(): AppSettings {
        val defaults = AppSettings()
        val gestures = defaults.gestures
        val (homeSections, hiddenHomeSections) = restoreHomeLayout(
            prefs.getString("home_sections", null), prefs.getString("hidden_home_sections", null))
        return AppSettings(
            companionUrl = prefs.getString("companion_url", null).orEmpty(),
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
            favoritePlaylistKeys = prefs.getStringSet("favorite_playlists", emptySet())?.toSet() ?: emptySet(),
            homeSections = homeSections,
            hiddenHomeSections = hiddenHomeSections,
            artistViewColumns = prefs.getInt("artist_view_columns", defaults.artistViewColumns).coerceIn(1, 3),
            playlistViewColumns = prefs.getInt("playlist_view_columns", defaults.playlistViewColumns).coerceIn(1, 3),
            albumViewColumns = prefs.getInt("album_view_columns", defaults.albumViewColumns).coerceIn(1, 3),
            songViewColumns = prefs.getInt("song_view_columns", defaults.songViewColumns).coerceIn(1, 3),
        )
    }

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString("companion_url", settings.companionUrl)
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
            .putStringSet("favorite_playlists", settings.favoritePlaylistKeys)
            .putString("home_sections", settings.homeSections.joinToString(",") { it.name })
            .putString("hidden_home_sections", settings.hiddenHomeSections.joinToString(",") { it.name })
            .putInt("artist_view_columns", settings.artistViewColumns)
            .putInt("playlist_view_columns", settings.playlistViewColumns)
            .putInt("album_view_columns", settings.albumViewColumns)
            .putInt("song_view_columns", settings.songViewColumns)
            .apply()
    }
}

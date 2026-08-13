package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.model.SortOrder
import com.example.data.local.model.ThemeMode
import com.example.data.local.model.UserProfile
import com.example.data.local.model.ViewMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("noteflow_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(getThemeModeFromPrefs())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()

    private val _viewMode = MutableStateFlow(getViewModeFromPrefs())
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _sortBy = MutableStateFlow(getSortByFromPrefs())
    val sortBy: StateFlow<SortOrder> = _sortBy.asStateFlow()

    private val _appLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_APP_LOCK_ENABLED, false))
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()

    private val _userProfile = MutableStateFlow(getUserProfileFromPrefs())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isFirstRun = MutableStateFlow(prefs.getBoolean(KEY_IS_FIRST_RUN, true))
    val isFirstRun: StateFlow<Boolean> = _isFirstRun.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }

    fun setViewMode(mode: ViewMode) {
        prefs.edit().putString(KEY_VIEW_MODE, mode.name).apply()
        _viewMode.value = mode
    }

    fun setSortBy(sortOrder: SortOrder) {
        prefs.edit().putString(KEY_SORT_BY, sortOrder.name).apply()
        _sortBy.value = sortOrder
    }

    fun setAppLockEnabled(enabled: Boolean, pin: String? = null) {
        if (enabled && !pin.isNullOrEmpty()) {
            val hashedPin = hashPin(pin)
            prefs.edit().putString(KEY_PIN_HASH, hashedPin).apply()
        }
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        _appLockEnabled.value = enabled
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        return storedHash == hashPin(pin)
    }

    fun setUserProfile(displayName: String, avatarId: String = "default") {
        prefs.edit()
            .putString(KEY_PROFILE_NAME, displayName)
            .putString(KEY_PROFILE_AVATAR, avatarId)
            .apply()
        _userProfile.value = UserProfile(displayName, avatarId)
    }

    fun completeFirstRun() {
        prefs.edit().putBoolean(KEY_IS_FIRST_RUN, false).apply()
        _isFirstRun.value = false
    }

    private fun getThemeModeFromPrefs(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try { ThemeMode.valueOf(name) } catch (e: Exception) { ThemeMode.SYSTEM }
    }

    private fun getViewModeFromPrefs(): ViewMode {
        val name = prefs.getString(KEY_VIEW_MODE, ViewMode.GRID.name) ?: ViewMode.GRID.name
        return try { ViewMode.valueOf(name) } catch (e: Exception) { ViewMode.GRID }
    }

    private fun getSortByFromPrefs(): SortOrder {
        val name = prefs.getString(KEY_SORT_BY, SortOrder.UPDATED_DESC.name) ?: SortOrder.UPDATED_DESC.name
        return try { SortOrder.valueOf(name) } catch (e: Exception) { SortOrder.UPDATED_DESC }
    }

    private fun getUserProfileFromPrefs(): UserProfile {
        val name = prefs.getString(KEY_PROFILE_NAME, "NoteFlow User") ?: "NoteFlow User"
        val avatar = prefs.getString(KEY_PROFILE_AVATAR, "default") ?: "default"
        return UserProfile(name, avatar)
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(("NoteFlowSalt_" + pin).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_SORT_BY = "sort_by"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_AVATAR = "profile_avatar"
        private const val KEY_IS_FIRST_RUN = "is_first_run"
    }
}

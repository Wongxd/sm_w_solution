package io.wongxd.solution.compose.composeTheme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable


object AppThemeHolder {

    var themeAdapterAuto = true

    private const val KEY_GROUP = "AppThemeGroup"

    private const val KEY_APP_THEME = "keyAppTheme"

    private lateinit var preferences: SharedPreferences
    var currentTheme = AppTheme.Light
        private set

    fun initTheme(app: Application) {
        preferences = app.getSharedPreferences(KEY_GROUP, Context.MODE_PRIVATE)
        currentTheme = getAppTheme()
    }

    private fun getAppTheme(): AppTheme {
        val themeType = preferences.getInt(KEY_APP_THEME, 0)
        return AppTheme.values().find { it.type == themeType } ?: AppTheme.Light
    }

    fun onAppThemeChanged(appTheme: AppTheme) {
        preferences.edit().putInt(KEY_APP_THEME, appTheme.type).apply()
        currentTheme = appTheme
    }

    var lightColor: ColorAsset = DefLightColorAsset
    var darkColor: ColorAsset = DefDarkColorAsset

}
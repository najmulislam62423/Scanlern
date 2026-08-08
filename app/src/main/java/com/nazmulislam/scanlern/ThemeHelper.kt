package com.nazmulislam.scanlern

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    private const val PREFS_NAME = "ScanlernPrefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2

    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedMode = prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
        applyTheme(savedMode)
    }

    fun setTheme(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        applyTheme(mode)
    }

    fun getSavedTheme(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    private fun applyTheme(mode: Int) {
        val nightMode = when (mode) {
            MODE_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MODE_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
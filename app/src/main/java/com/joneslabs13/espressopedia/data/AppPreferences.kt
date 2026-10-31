package com.joneslabs13.espressopedia.data

import android.content.Context

object AppPreferences {
    private const val PREF_NAME = "espressopedia_prefs"
    private const val KEY_DEFAULT_BREW_TIME = "default_brew_time_millis"
    private const val KEY_DEFAULT_COFFEE_RATIO = "default_coffee_ratio"
    private const val KEY_DEFAULT_WATER_RATIO = "default_water_ratio"

    fun getDefaultBrewTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_DEFAULT_BREW_TIME, 180000L) // Default 3 minutes
    }

    fun setDefaultBrewTime(context: Context, millis: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_DEFAULT_BREW_TIME, millis).apply()
    }

    fun getDefaultCoffeeRatio(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEFAULT_COFFEE_RATIO, "1.0")?.toDoubleOrNull() ?: 1.0
    }

    fun setDefaultCoffeeRatio(context: Context, ratio: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEFAULT_COFFEE_RATIO, ratio.toString()).apply()
    }

    fun getDefaultWaterRatio(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEFAULT_WATER_RATIO, "16.0")?.toDoubleOrNull() ?: 16.0
    }

    fun setDefaultWaterRatio(context: Context, ratio: Double) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEFAULT_WATER_RATIO, ratio.toString()).apply()
    }

    private const val KEY_DEFAULT_RINGTONE_URI = "default_ringtone_uri"

    fun getDefaultRingtoneUri(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DEFAULT_RINGTONE_URI, "") ?: ""
    }

    fun setDefaultRingtoneUri(context: Context, uriString: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEFAULT_RINGTONE_URI, uriString).apply()
    }

    private const val KEY_THEME_MODE = "theme_mode"

    fun getThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }
}

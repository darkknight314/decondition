package com.social.media.decondition.utils

import android.content.Context

object SharedPreferencesUtils {
    private const val PREFS_NAME = "AppSelections"

    fun getSelectedApps(context: Context): MutableSet<String> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("selectedApps", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    fun saveSelectedApps(context: Context, selectedApps: MutableSet<String>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("selectedApps", selectedApps)
            apply()
        }
    }

    fun clearSelectedApps(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("selectedApps", emptySet())
            apply()
        }
    }
}

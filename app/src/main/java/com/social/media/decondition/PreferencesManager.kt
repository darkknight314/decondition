package com.social.media.decondition

import android.content.Context
import android.content.SharedPreferences
import android.util.Log


class PreferencesManager private constructor(context: Context) {
    private val TAG = "PreferencesManager"
    private val prefsName = "AppSelections"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)


    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    /**
     * Store a boolean preference
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
        Log.d(TAG, "Saved boolean: $key = $value")
    }

    /**
     * Get a string preference with default value
     */
    fun getString(key: String, default: String = ""): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    /**
     * Store a string preference
     */
    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
        Log.d(TAG, "Saved string: $key = $value")
    }

    /**
     * Get a string set preference
     */
    fun getStringSet(key: String): Set<String> {
        return sharedPreferences.getStringSet(key, emptySet()) ?: emptySet()
    }

    /**
     * Store a string set preference
     */
    fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
        Log.d(TAG, "Saved string set: $key = $value")
    }

    /**
     * Register a preference change listener
     */
    fun registerOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Unregister a preference change listener
     */
    fun unregisterOnChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Clear all preferences (use with caution)
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Cleared all preferences")
    }

    /**
     * Get direct access to the SharedPreferences instance
     * (only use when absolutely necessary)
     */
    fun getRawPreferences(): SharedPreferences {
        return sharedPreferences
    }

    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null

        /**
         * Get singleton instance
         */
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
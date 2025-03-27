package com.social.media.decondition

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log

/**
 * Central manager for preferences related to app monitoring and puzzle launch.
 * Handles retrieving and storing lists of monitored apps and domains.
 */
class AppPreferencesManager(private val context: Context) {

    private val TAG = "AppPreferencesManager"
    private val prefsName = "AppSelections"
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    // Preference keys
    private val selectedAppsKey = "selectedApps"
    private val monitoredDomainsKey = "monitoredDomains"
    private val globalActivityKey = "GLOBAL_ACTIVITY"
    private val defaultActivityClass = "com.social.media.decondition.SudokuPuzzleActivity"

    /**
     * Get the list of selected apps (package names) to monitor.
     */
    fun getSelectedApps(): Set<String> {
        return sharedPreferences.getStringSet(selectedAppsKey, emptySet()) ?: emptySet()
    }

    /**
     * Get the list of monitored domains.
     */
    fun getMonitoredDomains(): Set<String> {
        // Return empty set if not configured
        return sharedPreferences.getStringSet(monitoredDomainsKey, emptySet()) ?: emptySet()
    }

    /**
     * Set the list of selected apps to monitor.
     */
    fun setSelectedApps(apps: Set<String>) {
        sharedPreferences.edit().putStringSet(selectedAppsKey, apps).apply()
        Log.d(TAG, "Updated selected apps: $apps")
    }

    /**
     * Set the list of monitored domains.
     */
    fun setMonitoredDomains(domains: Set<String>) {
        sharedPreferences.edit().putStringSet(monitoredDomainsKey, domains).apply()
        Log.d(TAG, "Updated monitored domains: $domains")
    }

    /**
     * Add a single app to the monitored list.
     */
    fun addSelectedApp(packageName: String) {
        val currentApps = getSelectedApps().toMutableSet()
        currentApps.add(packageName)
        setSelectedApps(currentApps)
    }

    /**
     * Remove a single app from the monitored list.
     */
    fun removeSelectedApp(packageName: String) {
        val currentApps = getSelectedApps().toMutableSet()
        currentApps.remove(packageName)
        setSelectedApps(currentApps)
    }

    /**
     * Add a domain to the monitored list.
     */
    fun addMonitoredDomain(domain: String) {
        val currentDomains = getMonitoredDomains().toMutableSet()
        currentDomains.add(domain)
        setMonitoredDomains(currentDomains)
    }

    /**
     * Remove a domain from the monitored list.
     */
    fun removeMonitoredDomain(domain: String) {
        val currentDomains = getMonitoredDomains().toMutableSet()
        currentDomains.remove(domain)
        setMonitoredDomains(currentDomains)
    }

    /**
     * Check if an app is selected for monitoring.
     */
    fun isAppSelected(packageName: String): Boolean {
        return getSelectedApps().contains(packageName)
    }

    /**
     * Check if a domain is in the monitored list.
     */
    fun isDomainMonitored(domain: String): Boolean {
        return getMonitoredDomains().any { domain.endsWith(it, ignoreCase = true) }
    }

    /**
     * Get the class name of the puzzle activity to launch.
     */
    fun getPuzzleActivityClassName(): String {
        return sharedPreferences.getString(globalActivityKey, defaultActivityClass) ?: defaultActivityClass
    }

    /**
     * Set the class name of the puzzle activity to launch.
     */
    fun setPuzzleActivityClassName(className: String) {
        sharedPreferences.edit().putString(globalActivityKey, className).apply()
    }

    /**
     * Check if a puzzle has been solved for an app.
     */
    fun isAppPuzzleSolved(packageName: String): Boolean {
        return sharedPreferences.getBoolean(getAppPuzzleSolvedKey(packageName), false)
    }

    /**
     * Set whether a puzzle has been solved for an app.
     */
    fun setAppPuzzleSolved(packageName: String, solved: Boolean) {
        sharedPreferences.edit().putBoolean(getAppPuzzleSolvedKey(packageName), solved).apply()
    }

    /**
     * Check if a session is active for an app.
     */
    fun isAppSessionActive(packageName: String): Boolean {
        return sharedPreferences.getBoolean(getAppSessionActiveKey(packageName), false)
    }

    /**
     * Set whether a session is active for an app.
     */
    fun setAppSessionActive(packageName: String, active: Boolean) {
        sharedPreferences.edit().putBoolean(getAppSessionActiveKey(packageName), active).apply()
    }

    /**
     * Check if a puzzle has been solved for a domain.
     */
    fun isDomainPuzzleSolved(domain: String): Boolean {
        return sharedPreferences.getBoolean(getDomainPuzzleSolvedKey(domain), false)
    }

    /**
     * Set whether a puzzle has been solved for a domain.
     */
    fun setDomainPuzzleSolved(domain: String, solved: Boolean) {
        sharedPreferences.edit().putBoolean(getDomainPuzzleSolvedKey(domain), solved).apply()
    }

    /**
     * Check if a session is active for a domain.
     */
    fun isDomainSessionActive(domain: String): Boolean {
        return sharedPreferences.getBoolean(getDomainSessionActiveKey(domain), false)
    }

    /**
     * Set whether a session is active for a domain.
     */
    fun setDomainSessionActive(domain: String, active: Boolean) {
        sharedPreferences.edit().putBoolean(getDomainSessionActiveKey(domain), active).apply()
    }

    /**
     * Handle app launch - determines if puzzle should be shown.
     * Returns true if puzzle activity was launched.
     */
    fun handleAppLaunch(packageName: String): Boolean {
        // Skip our own package
        if (packageName == "com.social.media.decondition") {
            return false
        }

        // Check if this app is monitored
        if (!isAppSelected(packageName)) {
            return false
        }

        // Check if puzzle needs to be shown
        val puzzleSolved = isAppPuzzleSolved(packageName)
        val sessionActive = isAppSessionActive(packageName)

        if (puzzleSolved && sessionActive) {
            // App can be used normally
            return false
        }

        // Launch puzzle activity
        launchPuzzleActivity(packageName)
        return true
    }

    /**
     * Handle domain access - determines if puzzle should be shown.
     * Returns true if puzzle activity was launched.
     */
    fun handleDomainAccess(domain: String): Boolean {
        // Check if this domain is monitored
        if (!isDomainMonitored(domain)) {
            return false
        }

        Log.d(TAG, "Monitored domain detected: $domain")

        // Map domain to app package (this is a simplified approach)
        val packageName = when {
            domain.contains("facebook", ignoreCase = true) -> "com.facebook.katana"
            domain.contains("instagram", ignoreCase = true) -> "com.instagram.android"
            domain.contains("twitter", ignoreCase = true) -> "com.twitter.android"
            domain.contains("tiktok", ignoreCase = true) -> "com.zhiliaoapp.musically"
            else -> "unknown_package"  // Default placeholder
        }

        // Check if puzzle needs to be shown for this domain
        val puzzleSolved = isDomainPuzzleSolved(domain)
        val sessionActive = isDomainSessionActive(domain)

        if (puzzleSolved && sessionActive) {
            // Domain can be accessed normally
            Log.d(TAG, "Domain $domain has active session, allowing access")
            return false
        }

        // Launch puzzle activity with the inferred package name
        launchPuzzleActivity(packageName, domain)
        return true
    }

    /**
     * Launch the puzzle activity.
     */
    private fun launchPuzzleActivity(packageName: String, domain: String? = null) {
        try {
            val activityClass = Class.forName(getPuzzleActivityClassName())
            val intent = Intent(context, activityClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("APP_PACKAGE_NAME", packageName)
                // Pass domain information if available
                domain?.let { putExtra("DOMAIN", it) }
            }
            context.startActivity(intent)
            Log.d(TAG, "Launched puzzle activity for $packageName" + (domain?.let { ", domain: $it" } ?: ""))
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Activity class not found: ${getPuzzleActivityClassName()}", e)
        }
    }

    /**
     * Get the key for puzzle solved state for an app.
     */
    private fun getAppPuzzleSolvedKey(packageName: String): String {
        return "PUZZLE_SOLVED_APP_$packageName"
    }

    /**
     * Get the key for session active state for an app.
     */
    private fun getAppSessionActiveKey(packageName: String): String {
        return "SESSION_ACTIVE_APP_$packageName"
    }

    /**
     * Get the key for puzzle solved state for a domain.
     */
    private fun getDomainPuzzleSolvedKey(domain: String): String {
        return "PUZZLE_SOLVED_DOMAIN_$domain"
    }

    /**
     * Get the key for session active state for a domain.
     */
    private fun getDomainSessionActiveKey(domain: String): String {
        return "SESSION_ACTIVE_DOMAIN_$domain"
    }

    companion object {
        @Volatile
        private var INSTANCE: AppPreferencesManager? = null

        /**
         * Get singleton instance.
         */
        fun getInstance(context: Context): AppPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
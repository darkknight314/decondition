package com.social.media.decondition

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppLaunchAccessibilityService : AccessibilityService() {

    private val prefsName = "AppSelections"
    private lateinit var sharedPreferences: SharedPreferences
    private val selectedApps = mutableSetOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE)
        selectedApps.addAll(sharedPreferences.getStringSet("selectedApps", emptySet()) ?: emptySet())

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
        }
        serviceInfo = info

        Log.d("AppLaunchService", "Service connected with selected apps: $selectedApps")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            Log.d("AppLaunchService", "Detected app launch: $packageName")
            if (packageName != null && selectedApps.contains(packageName)) {
                Log.d("AppLaunchService", "Redirecting to MainActivity")
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Handle service interruption
    }
}
package com.social.media.decondition

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppLaunchAccessibilityService : AccessibilityService() {
    private val TAG = "AppLaunchService"

    // Preferences manager for handling selected apps
    private lateinit var preferencesManager: AppPreferencesManager

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Initialize preferences manager
        preferencesManager = AppPreferencesManager.getInstance(this)

        // Configure accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
        }
        serviceInfo = info

        Log.d(TAG, "Service connected with selected apps: ${preferencesManager.getSelectedApps()}")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Don't process our own app
            if (packageName == "com.social.media.decondition") return

            Log.d(TAG, "Detected app launch: $packageName")

            // Use preferences manager to handle app launch
            preferencesManager.handleAppLaunch(packageName)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
}
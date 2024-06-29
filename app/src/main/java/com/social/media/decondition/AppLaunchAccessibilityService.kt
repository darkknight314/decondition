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
        Log.d("MyAccessibilityService", "Accessibility event received: " + event.toString());
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            if(packageName==="com.social.media.decondition")    return
            Log.d("AppLaunchService", "Detected app launch: $packageName")
            if (packageName != null && selectedApps.contains(packageName)) {
                val puzzleSolved = sharedPreferences.getBoolean("PUZZLE_SOLVED_$packageName", false)
                val sessionActive = sharedPreferences.getBoolean("SESSION_ACTIVE_$packageName", false)
                if (!puzzleSolved || !sessionActive) {
                    Log.d("AppLaunchService", "Redirecting to SudokuPuzzleActivity")
                    val intent = Intent(this, SudokuPuzzleActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("APP_PACKAGE_NAME", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("AppLaunchService", "Service interrupted")
    }
}
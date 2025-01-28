package com.social.media.decondition

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class AppUsageMonitoringService : Service() {

    private val prefsName = "AppSelections"
    private lateinit var sharedPreferences: SharedPreferences
    private val selectedApps = mutableSetOf<String>()
    private lateinit var handler: Handler
    private val checkInterval: Long = 5000 // Check every second

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(prefsName, MODE_PRIVATE)
        selectedApps.addAll(sharedPreferences.getStringSet("selectedApps", emptySet()) ?: emptySet())
        handler = Handler(Looper.getMainLooper())
        startMonitoring()
        Log.d("AppLaunchMonitorService", "Service started with selected apps: $selectedApps")
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkForegroundApp()
                handler.postDelayed(this, checkInterval)
            }
        })
    }

    private fun checkForegroundApp() {
        val currentApp = getForegroundApp()
        if (currentApp == null || currentApp == packageName) return

        Log.d("AppLaunchMonitorService", "Detected foreground app: $currentApp")
        if (selectedApps.contains(currentApp)) {
            val puzzleSolved = sharedPreferences.getBoolean("PUZZLE_SOLVED_$currentApp", false)
            val sessionActive = sharedPreferences.getBoolean("SESSION_ACTIVE_$currentApp", false)
            if (!puzzleSolved || !sessionActive) {
                Log.d("AppLaunchMonitorService", "Redirecting to SudokuPuzzleActivity")
                val intent = Intent(this, SudokuPuzzleActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("DeCondition", currentApp)
                }
                startActivity(intent)
            }
        }
    }

    private fun getForegroundApp(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 10000 // Check the last 10 seconds

        var lastUsedAppTimeStamp = 0L
        var foregroundApp: String? = null

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {

                if (event.timeStamp > lastUsedAppTimeStamp) {
                    lastUsedAppTimeStamp = event.timeStamp
                    foregroundApp = event.packageName
                }
            }
        }
        return foregroundApp
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null because this is a started service, not a bound service
        return null
    }
}
package com.social.media.decondition.utils

import android.app.Activity
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog

object RequestPermissionUtils {

    /**
     * Checks if the app has Usage Stats (App Usage) permission.
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        // Check the app ops mode for GET_USAGE_STATS
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) {
            // On some ROMs, we still need to verify that usage stats are actually obtainable
            val usageStatsManager =
                context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 1000 * 60 * 60 // last hour (for example)

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY, beginTime, endTime
            )
            // If usageStats is null or empty, we might not really have permission
            if (usageStats == null || usageStats.isEmpty()) {
                Log.d("RequestPermissionUtils", "UsageStats is empty despite MODE_ALLOWED.")
                return false
            }
            return true
        }
        return false
    }

    /**
     * Shows a dialog prompting the user to grant Usage Stats (App Usage) permission.
     * Must be called from an Activity context to show the AlertDialog properly.
     */
    fun requestUsageStatsPermission(activity: Activity) {
        if (hasUsageStatsPermission(activity)) return

        AlertDialog.Builder(activity)
            .setTitle("Enable Usage Access")
            .setMessage(
                "Please grant usage access permission so the app can detect " +
                        "when a distracting app is in use."
            )
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Checks if the app can draw overlays on top of other apps.
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Prompts the user to grant the System Alert Window (overlay) permission.
     * Must be called from an Activity context to show the AlertDialog properly.
     */
    fun requestOverlayPermission(activity: Activity) {
        if (hasOverlayPermission(activity)) return

        AlertDialog.Builder(activity)
            .setTitle("Enable Overlay Permission")
            .setMessage(
                "This app requires permission to draw over other apps to show " +
                        "the puzzle or reminder screen on top of distracting apps."
            )
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Example convenience method that checks and requests both permissions in sequence.
     * You can adjust this flow to fit your UX needs.
     */
    fun checkAndRequestAllPermissions(activity: Activity) {
        // 1. Request Usage Access if not granted
        if (!hasUsageStatsPermission(activity)) {
            requestUsageStatsPermission(activity)
        }
        // 2. Request Overlay permission if not granted
        if (!hasOverlayPermission(activity)) {
            requestOverlayPermission(activity)
        }
    }
}
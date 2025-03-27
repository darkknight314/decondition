package com.social.media.decondition.utils

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.social.media.decondition.AppLaunchAccessibilityService
import com.social.media.decondition.TrafficMonitorVpnService

/**
 * Handles permission-related functionality for the app
 */
class AppPermissionHandler(private val activity: AppCompatActivity) {
    private val TAG = "AppPermissionHandler"
    private val VPN_REQUEST_CODE = 101

    private val activityResultLauncher: ActivityResultLauncher<Intent> = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "VPN permission granted through launcher, starting service")
            startVpnService(true)
        } else {
            Log.d(TAG, "VPN permission denied through launcher")
            Toast.makeText(
                activity,
                "VPN permission is required for monitoring DNS queries",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Request all required permissions
     */
    fun requestAllPermissions() {
        RequestPermissionUtils.checkAndRequestAllPermissions(activity)
        checkAndRequestAccessibilityPermission()
        requestVpnPermission()
    }

    /**
     * Check and request accessibility permission
     */
    fun checkAndRequestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled(activity, AppLaunchAccessibilityService::class.java)) {
            AlertDialog.Builder(activity)
                .setTitle("Enable Accessibility Service")
                .setMessage("To enable app blocking, please allow accessibility service for this app.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    activity.startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    /**
     * Check if accessibility service is enabled
     */
    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = enabledServices?.split(":") ?: return false
        return colonSplitter.any { it.contains(serviceClass.name) }
    }

    /**
     * Request VPN permission
     */
    fun requestVpnPermission() {
        try {
            val intent = VpnService.prepare(activity)
            if (intent != null) {
                Log.d(TAG, "Requesting VPN permission")
                activityResultLauncher.launch(intent)
            } else {
                Log.d(TAG, "VPN permission already granted, starting service")
                startVpnService(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching VPN permission request: ${e.message}", e)
            try {
                // Fallback to older method if the launcher fails
                @Suppress("DEPRECATION")
                activity.startActivityForResult(VpnService.prepare(activity), VPN_REQUEST_CODE)
            } catch (e2: Exception) {
                Log.e(TAG, "Both permission request methods failed: ${e2.message}", e2)
                Toast.makeText(
                    activity,
                    "Unable to request VPN permissions. Please restart the app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Start VPN service
     */
    fun startVpnService(hasPermission: Boolean) {
        if (hasPermission) {
            val serviceIntent = Intent(activity, TrafficMonitorVpnService::class.java)
            serviceIntent.action = "START_VPN"
            activity.startService(serviceIntent)
        } else {
            requestVpnPermission()
        }
    }

    /**
     * Handle activity result (for older API support)
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == VPN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "VPN permission granted through onActivityResult, starting service")
                startVpnService(true)
            } else {
                Log.d(TAG, "VPN permission denied through onActivityResult")
                Toast.makeText(
                    activity,
                    "VPN permission is required for monitoring DNS queries",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
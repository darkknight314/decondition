package com.social.media.decondition.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import com.social.media.decondition.AppLaunchAccessibilityService

object AccessibilityServiceUtils {
    fun isAccessibilityServiceEnabled(context: Context, service: Class<out android.accessibilityservice.AccessibilityService>): Boolean {
        val serviceId = "${context.packageName}/${service.name}"
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (!enabledServices.isNullOrEmpty()) {
            val splitter = TextUtils.SimpleStringSplitter(':')
            splitter.setString(enabledServices)
            while (splitter.hasNext()) {
                val componentName = splitter.next()
                if (componentName.equals(serviceId, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    fun showAccessibilityServiceDialog(context: Context) {
        if (!isAccessibilityServiceEnabled(context, AppLaunchAccessibilityService::class.java)) {
            AlertDialog.Builder(context)
                .setTitle("Enable Accessibility Service")
                .setMessage("Please enable the accessibility service to allow this app to monitor app launches.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}

package com.social.media.decondition.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

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
}

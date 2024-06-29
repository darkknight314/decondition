package com.social.media.decondition.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.social.media.decondition.data.AppDetail
import java.util.Locale

object AppUtils {
    fun getInstalledApps(context: Context): MutableList<AppDetail> {
        val pm = context.packageManager
        val apps = mutableListOf<AppDetail>()
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packages = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in packages) {
            val activityInfo = resolveInfo.activityInfo
            val packageName = activityInfo.packageName
            val appName = activityInfo.loadLabel(pm).toString()
            val icon = activityInfo.loadIcon(pm)
            apps.add(AppDetail(appName, packageName, icon))
        }
        apps.sortBy { it.appName.lowercase(Locale.ROOT).trim() }
        return apps
    }

    fun getSelectedAppDetailsList(context: Context, selectedApps: Set<String>): MutableList<AppDetail> {
        val pm = context.packageManager
        val apps = mutableListOf<AppDetail>()

        for (packageName in selectedApps) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(packageName)
                apps.add(AppDetail(appName, packageName, icon))
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        apps.sortBy { it.appName.lowercase(Locale.ROOT).trim() }
        return apps
    }

    fun getNonSelectedApps(context: Context, selectedApps: Set<String>): List<AppDetail> {
        val apps = getInstalledApps(context).toMutableList()
        apps.removeAll { selectedApps.contains(it.packageName) || context.packageName.equals(it.packageName) }
        return apps
    }
}

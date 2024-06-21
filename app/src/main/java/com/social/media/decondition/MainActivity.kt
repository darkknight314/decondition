package com.social.media.decondition

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var plusButton: Button
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: MutableList<AppDetail> = mutableListOf()
    private var selectedAppsList: MutableList<AppDetail> = mutableListOf()
    private var nonSelectedAppsList: MutableList<AppDetail> = mutableListOf()
    private val selectedApps = mutableSetOf<String>()
    private val prefsName = "AppSelections"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        // Load selected apps from SharedPreferences
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        selectedApps.addAll(sharedPreferences.getStringSet("selectedApps", emptySet()) ?: emptySet())

        // Initialize and display the selected apps by default
        selectedAppsList = getSelectedApps().toMutableList()
        appsAdapter = AppsAdapter(selectedAppsList, ::onAppSelected)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        // Ensure the selected apps list is displayed on startup
        searchView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.filterList(selectedAppsList)

        plusButton.setOnClickListener {
            if (searchView.visibility == View.VISIBLE) {
                searchView.visibility = View.GONE
                appsAdapter.filterList(selectedAppsList)
                hideKeyboard()
            } else {
                searchView.visibility = View.VISIBLE
                appsRecyclerView.visibility = View.VISIBLE
                searchView.requestFocus()
                searchView.setIconified(false) // Expand the search view if it's iconified
                showKeyboard(searchView)
                // Switch to displaying the non-selected app list
                nonSelectedAppsList = getNonSelectedApps().toMutableList()
                appsAdapter.filterList(nonSelectedAppsList)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val filteredList = nonSelectedAppsList.filter { it.appName.contains(newText, ignoreCase = true) }
                    appsAdapter.filterList(filteredList)
                }
                return true
            }
        })

        // Show the dialog to guide users to enable the accessibility service
        showAccessibilityServiceDialog()
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out android.accessibilityservice.AccessibilityService>): Boolean {
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

    private fun showAccessibilityServiceDialog() {
        if (!isAccessibilityServiceEnabled(this, AppLaunchAccessibilityService::class.java)) {
            AlertDialog.Builder(this)
                .setTitle("Enable Accessibility Service")
                .setMessage("Please enable the accessibility service to allow this app to monitor app launches.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onBackPressed() {
        if (searchView.visibility == View.VISIBLE) {
            // Hide search view and show default view
            searchView.visibility = View.GONE
            appsAdapter.filterList(selectedAppsList)
            hideKeyboard()
        } else {
            super.onBackPressed()
        }
    }

    private fun onAppSelected(app: AppDetail) {
        selectedApps.add(app.packageName)
        // Save selected apps to SharedPreferences
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("selectedApps", selectedApps)
            apply()
        }
        // Update the lists and adapter
        nonSelectedAppsList.remove(app)
        selectedAppsList.add(app)
        appsAdapter.notifyDataSetChanged()
    }

    private fun getInstalledApps(): MutableList<AppDetail> {
        val pm = packageManager
        val apps = mutableListOf<AppDetail>()

        // Use a different method to get only apps that can be launched
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

    private fun getSelectedApps(): List<AppDetail> {
        val pm = packageManager
        val apps = mutableListOf<AppDetail>()

        for (packageName in selectedApps) {
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val appName = pm.getApplicationLabel(appInfo).toString()
                val icon = pm.getApplicationIcon(packageName)
                apps.add(AppDetail(appName, packageName, icon))
            } catch (e: PackageManager.NameNotFoundException) {
                // Handle the case where the package name is not found
                e.printStackTrace()
            }
        }
        apps.sortBy { it.appName.lowercase(Locale.ROOT).trim() }
        return apps
    }

    private fun getNonSelectedApps(): List<AppDetail> {
        val pm = packageManager
        val apps = getInstalledApps().toMutableList()
        apps.removeAll { selectedApps.contains(it.packageName) || this.packageName.equals(it.packageName) }
        return apps
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    private fun clearSelectedApps() {
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("selectedApps", emptySet())
            apply()
        }
        selectedApps.clear()
    }
}
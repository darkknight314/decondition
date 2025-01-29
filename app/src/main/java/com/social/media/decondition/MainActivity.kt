package com.social.media.decondition

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail
import com.social.media.decondition.utils.*

class MainActivity : AppCompatActivity() {
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchBarView: SearchView
    private lateinit var plusButton: Button
    private lateinit var appsAdapter: AppsAdapter

    private val blacklistedAppPackageNamesList = mutableSetOf<String>()
    private var blacklistedAppDetailsList: MutableList<AppDetail> = mutableListOf()
    private var whitelistedAppsList: MutableList<AppDetail> = mutableListOf()

    // Flag to track current view state: true for blacklist, false for whitelist
    private var isViewingBlacklist = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        plusButton = findViewById(R.id.plusButton)
        searchBarView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        // Load blacklisted apps from SharedPreferences
        refreshBlacklist()

        resetSessionFlags()

        // Fetch details of blacklisted apps
        blacklistedAppDetailsList = AppUtils.getSelectedAppDetailsList(this, blacklistedAppPackageNamesList)

        // Initialize adapter
        appsAdapter = AppsAdapter(blacklistedAppDetailsList, isViewingBlacklist) { app ->
            onAppSelected(app)
        }

        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        // Initially, show blacklisted apps and hide search bar
        searchBarView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.updateList(blacklistedAppDetailsList)

        // Set up plus button to toggle views
        plusButton.setOnClickListener { toggleView() }

        // Set up search functionality
        searchBarView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isViewingBlacklist) {
                    val filteredList = if (!newText.isNullOrBlank()) {
                        whitelistedAppsList.filter { app -> app.appName.contains(newText, ignoreCase = true) }
                    } else {
                        whitelistedAppsList
                    }
                    appsAdapter.updateList(filteredList.toMutableList())
                }
                return true
            }
        })

        // Request necessary permissions
        RequestPermissionUtils.checkAndRequestAllPermissions(this)

        // Guide user to enable accessibility service
        promptEnableAccessibilityService()
    }

    override fun onBackPressed() {
        if (!isViewingBlacklist) {
            toggleView()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Refreshes the blacklist by reloading from SharedPreferences.
     */
    private fun refreshBlacklist() {
        blacklistedAppPackageNamesList.clear()
        blacklistedAppPackageNamesList.addAll(SharedPreferencesUtils.getBlacklistedApps(this))
    }

    /**
     * Handles the selection of an app from the RecyclerView.
     * Moves the app between blacklist and whitelist based on the current view.
     */
    private fun onAppSelected(app: AppDetail) {
        if (isViewingBlacklist) {
            // Move app from blacklist to whitelist
            blacklistedAppPackageNamesList.remove(app.packageName)
            blacklistedAppDetailsList.remove(app)
            whitelistedAppsList.add(app)
            showToast("${app.appName} removed from blacklist")
        } else {
            // Move app from whitelist to blacklist
            blacklistedAppPackageNamesList.add(app.packageName)
            blacklistedAppDetailsList.add(app)
            whitelistedAppsList.remove(app)
            showToast("${app.appName} added to blacklist")
        }

        // Save updated blacklist to SharedPreferences
        SharedPreferencesUtils.saveSelectedApps(this, blacklistedAppPackageNamesList)

        // Refresh the list dynamically
        refreshBlacklist()
        if (isViewingBlacklist) {
            appsAdapter.updateList(blacklistedAppDetailsList)
        } else {
            appsAdapter.updateList(whitelistedAppsList)
        }
    }

    /**
     * Resets session flags for all blacklisted apps.
     */
    private fun resetSessionFlags() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (packageName in blacklistedAppPackageNamesList) {
            editor.putBoolean("SESSION_ACTIVE_$packageName", false)
        }
        editor.apply()
    }

    /**
     * Toggles between viewing blacklisted and whitelisted apps.
     * Shows or hides the search bar accordingly.
     */
    private fun toggleView() {
        if (isViewingBlacklist) {
            // Switch to whitelisted view
            isViewingBlacklist = false
            searchBarView.visibility = View.VISIBLE
            plusButton.text = "Done"

            // Fetch whitelisted apps
            whitelistedAppsList = AppUtils.getNonSelectedApps(this, blacklistedAppPackageNamesList)
            appsAdapter.updateList(whitelistedAppsList)

            // Update adapter's view state
            appsAdapter.setViewState(isViewingBlacklist)

            appsRecyclerView.scrollToPosition(0)

            // Show keyboard for search
            searchBarView.requestFocus()
            searchBarView.isIconified = false
            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.showKeyboard(this, searchBarView)
            }, 200)
        } else {
            // Switch back to blacklisted view
            isViewingBlacklist = true
            searchBarView.visibility = View.GONE
            plusButton.text = "Add"

            // Show blacklisted apps
            appsAdapter.updateList(blacklistedAppDetailsList)

            // Update adapter's view state
            appsAdapter.setViewState(isViewingBlacklist)

            appsRecyclerView.scrollToPosition(0)

            // Hide keyboard
            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.hideKeyboard(this, searchBarView)
            }, 200)

            // Clear search query
            searchBarView.setQuery("", false)
        }
    }

    /**
     * Displays a toast message to the user.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Guides the user to enable the accessibility service manually.
     */
    private fun promptEnableAccessibilityService() {
        Toast.makeText(
            this,
            "Enable the Accessibility Service in Settings for full functionality.",
            Toast.LENGTH_LONG
        ).show()

        // Open Accessibility Settings
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    /**
     * Handles permission requests properly.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
            Toast.makeText(this, "Permissions are required for proper functionality", Toast.LENGTH_LONG).show()
            // Optionally, redirect them to settings
        }
    }
}
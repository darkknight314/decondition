package com.social.media.decondition

import android.content.Intent
import android.os.Bundle
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
        blacklistedAppPackageNamesList.addAll(SharedPreferencesUtils.getBlacklistedApps(this))

        resetSessionFlags()

        // Fetch details of blacklisted apps
        blacklistedAppDetailsList = AppUtils.getSelectedAppDetailsList(this, blacklistedAppPackageNamesList)

        // Initialize adapter with blacklisted apps and set initial view state to blacklist
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
                // No action needed on submit
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isViewingBlacklist) {
                    newText?.let {
                        val filteredList = whitelistedAppsList.filter { app ->
                            app.appName.contains(it, ignoreCase = true)
                        }
                        appsAdapter.updateList(filteredList.toMutableList())
                    }
                }
                return true
            }
        })
        RequestPermissionUtils.checkAndRequestAllPermissions(this)
        val serviceIntent = Intent(this, AppUsageMonitoringService::class.java)
        startService(serviceIntent)

    }

    override fun onBackPressed() {
        if (!isViewingBlacklist) {
            toggleView()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Handles the selection of an app from the RecyclerView.
     * Moves the app between blacklist and whitelist based on the current view.
     *
     * @param app The AppDetail object representing the selected app.
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

        // Update the displayed list based on current view
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
            plusButton.text = "Done" // Optionally change button text to indicate action

            // Fetch whitelisted apps
            whitelistedAppsList = AppUtils.getNonSelectedApps(this, blacklistedAppPackageNamesList)
            appsAdapter.updateList(whitelistedAppsList)

            // Update adapter's view state
            appsAdapter.setViewState(isViewingBlacklist)

            appsRecyclerView.scrollToPosition(0)

            // Show keyboard for search
            searchBarView.requestFocus()
            searchBarView.isIconified = false
            KeyboardUtils.showKeyboard(this, searchBarView)
        } else {
            // Switch back to blacklisted view
            isViewingBlacklist = true
            searchBarView.visibility = View.GONE
            plusButton.text = "Add" // Optionally revert button text

            // Show blacklisted apps
            appsAdapter.updateList(blacklistedAppDetailsList)

            // Update adapter's view state
            appsAdapter.setViewState(isViewingBlacklist)

            appsRecyclerView.scrollToPosition(0)

            // Hide keyboard
            KeyboardUtils.hideKeyboard(this, searchBarView)

            // Clear search query
            searchBarView.setQuery("", false)
        }
    }

    /**
     * Displays a toast message to the user.
     *
     * @param message The message to display.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
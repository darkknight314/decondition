package com.social.media.decondition.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.AppsAdapter
import com.social.media.decondition.ContentType
import com.social.media.decondition.PreferencesManager
import com.social.media.decondition.data.AppDetail

/**
 * Manages app list operations including displaying, searching, and selecting apps
 */
class AppListManager(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val searchView: SearchView,
    private val addButton: Button,
    private val preferencesManager: PreferencesManager
) {
    private val selectedAppsList = mutableSetOf<String>()
    private var selectedAppDetailsList: MutableList<AppDetail> = mutableListOf()
    private var unselectedAppsList: MutableList<AppDetail> = mutableListOf()
    private var isViewingSelectedApps = true

    private lateinit var appsAdapter: AppsAdapter

    /**
     * Initialize the app list manager
     */
    fun initialize() {
        loadSelectedApps()
        resetActiveSessions()
        setupRecyclerView()
        setupSearchView()
        setupAddButton()

        // Initial UI state
        isViewingSelectedApps = true
        searchView.visibility = View.GONE
        addButton.text = "Add"
        recyclerView.visibility = View.VISIBLE

        selectedAppDetailsList = AppUtils.getSelectedAppDetailsList(context, selectedAppsList)
        appsAdapter.updateList(selectedAppDetailsList)
        appsAdapter.setViewState(isViewingSelectedApps)
        recyclerView.scrollToPosition(0)
    }

    /**
     * Set up recycler view and adapter
     */
    private fun setupRecyclerView() {
        selectedAppDetailsList = AppUtils.getSelectedAppDetailsList(context, selectedAppsList)

        appsAdapter = AppsAdapter(selectedAppDetailsList, isViewingSelectedApps) { app ->
            onAppSelected(app)
        }

        recyclerView.adapter = appsAdapter
    }

    /**
     * Set up search view
     */
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isViewingSelectedApps) {
                    val filteredList = if (!newText.isNullOrBlank()) {
                        unselectedAppsList.filter { app ->
                            app.appName.contains(newText, ignoreCase = true)
                        }
                    } else {
                        unselectedAppsList
                    }
                    appsAdapter.updateList(filteredList.toMutableList())
                }
                return true
            }
        })
    }

    /**
     * Set up add button
     */
    private fun setupAddButton() {
        addButton.setOnClickListener { toggleView() }
    }

    /**
     * Load selected apps from preferences
     */
    private fun loadSelectedApps() {
        selectedAppsList.clear()
        selectedAppsList.addAll(preferencesManager.getStringSet("selectedApps"))
    }

    /**
     * Reset active sessions for all apps and domains
     */
    private fun resetActiveSessions() {
        // Reset all app sessions to inactive state
        for (packageName in selectedAppsList) {
            val sessionKey = "SESSION_ACTIVE_${ContentType.APP}_$packageName"
            preferencesManager.putBoolean(sessionKey, false)
        }

        // Reset all domain sessions too
        val monitoredDomains = preferencesManager.getStringSet("monitoredDomains")
        for (domain in monitoredDomains) {
            val sessionKey = "SESSION_ACTIVE_${ContentType.DOMAIN}_$domain"
            preferencesManager.putBoolean(sessionKey, false)
        }
    }

    /**
     * Handle app selection
     */
    private fun onAppSelected(app: AppDetail) {
        if (isViewingSelectedApps) {
            // Remove from selected apps
            selectedAppsList.remove(app.packageName)
            selectedAppDetailsList.remove(app)
            unselectedAppsList.add(app)
            showToast("${app.appName} removed from monitored apps")
        } else {
            // Add to selected apps
            selectedAppsList.add(app.packageName)
            selectedAppDetailsList.add(app)
            unselectedAppsList.remove(app)
            showToast("${app.appName} added to monitored apps")
        }

        // Save changes to preferences
        preferencesManager.putStringSet("selectedApps", selectedAppsList)

        // Update UI
        loadSelectedApps()
        if (isViewingSelectedApps) {
            appsAdapter.updateList(selectedAppDetailsList)
        } else {
            appsAdapter.updateList(unselectedAppsList)
        }
    }

    /**
     * Toggle between selected and unselected app views
     */
    private fun toggleView() {
        if (isViewingSelectedApps) {
            // Switching to unselected apps view
            isViewingSelectedApps = false
            searchView.visibility = View.VISIBLE
            addButton.text = "Done"

            unselectedAppsList = AppUtils.getNonSelectedApps(context, selectedAppsList)
            appsAdapter.updateList(unselectedAppsList)

            appsAdapter.setViewState(isViewingSelectedApps)
            recyclerView.scrollToPosition(0)

            searchView.requestFocus()
            searchView.isIconified = false
            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.showKeyboard(context, searchView)
            }, 200)
        } else {
            // Switching back to selected apps view
            isViewingSelectedApps = true
            searchView.visibility = View.GONE
            addButton.text = "Add"

            // Update adapter with selected apps list - THIS LINE WAS MISSING
            appsAdapter.updateList(selectedAppDetailsList)

            appsAdapter.setViewState(isViewingSelectedApps)
            recyclerView.scrollToPosition(0)

            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.hideKeyboard(context, searchView)
            }, 200)

            searchView.setQuery("", false)
        }
    }

    /**
     * Handle back button press
     * @return true if handled, false otherwise
     */
    fun handleBackPress(): Boolean {
        if (!isViewingSelectedApps) {
            toggleView()
            return true
        }
        return false
    }

    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
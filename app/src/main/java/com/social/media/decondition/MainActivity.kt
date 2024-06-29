package com.social.media.decondition

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail
import com.social.media.decondition.utils.AccessibilityServiceUtils
import com.social.media.decondition.utils.AppUtils
import com.social.media.decondition.utils.KeyboardUtils
import com.social.media.decondition.utils.SharedPreferencesUtils

class MainActivity : AppCompatActivity() {
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var plusButton: Button
    private lateinit var launchSudokuButton: Button
    private lateinit var appsAdapter: AppsAdapter
    private var appsList: MutableList<AppDetail> = mutableListOf()
    private var selectedAppsList: MutableList<AppDetail> = mutableListOf()
    private var nonSelectedAppsList: MutableList<AppDetail> = mutableListOf()
    private val selectedApps = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)
        launchSudokuButton = findViewById(R.id.launchSudokuButton)

        selectedApps.addAll(SharedPreferencesUtils.getSelectedApps(this))

        // Reset session flags for all selected apps
        resetSessionFlags()

        selectedAppsList = AppUtils.getSelectedApps(this, selectedApps).toMutableList()
        appsAdapter = AppsAdapter(selectedAppsList, ::onAppSelected)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        searchView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.filterList(selectedAppsList)

        plusButton.setOnClickListener {
            if (searchView.visibility == View.VISIBLE) {
                searchView.visibility = View.GONE
                appsAdapter.filterList(selectedAppsList)
                KeyboardUtils.hideKeyboard(this, searchView)
            } else {
                searchView.visibility = View.VISIBLE
                appsRecyclerView.visibility = View.VISIBLE
                searchView.requestFocus()
                searchView.setIconified(false)
                KeyboardUtils.showKeyboard(this, searchView)
                nonSelectedAppsList = AppUtils.getNonSelectedApps(this, selectedApps).toMutableList()
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

        showAccessibilityServiceDialog()

        launchSudokuButton.setOnClickListener {
            val intent = Intent(this, SudokuPuzzleActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAccessibilityServiceDialog() {
        if (!AccessibilityServiceUtils.isAccessibilityServiceEnabled(this, AppLaunchAccessibilityService::class.java)) {
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
            searchView.visibility = View.GONE
            appsAdapter.filterList(selectedAppsList)
            KeyboardUtils.hideKeyboard(this, searchView)
        } else {
            super.onBackPressed()
        }
    }

    private fun onAppSelected(app: AppDetail) {
        selectedApps.add(app.packageName)
        SharedPreferencesUtils.saveSelectedApps(this, selectedApps)
        nonSelectedAppsList.remove(app)
        selectedAppsList.add(app)
        appsAdapter.notifyDataSetChanged()
    }

    private fun resetSessionFlags() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (packageName in selectedApps) {
            editor.putBoolean("SESSION_ACTIVE_$packageName", false)
        }
        editor.apply()
    }
}
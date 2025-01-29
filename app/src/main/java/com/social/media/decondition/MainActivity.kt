package com.social.media.decondition

import android.accessibilityservice.AccessibilityService
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail
import com.social.media.decondition.utils.*
// TODO: Permission isnt requested again if given the first time
class MainActivity : AppCompatActivity() {
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var searchBarView: SearchView
    private lateinit var plusButton: Button
    private lateinit var appsAdapter: AppsAdapter

    private val blacklistedAppPackageNamesList = mutableSetOf<String>()
    private var blacklistedAppDetailsList: MutableList<AppDetail> = mutableListOf()
    private var whitelistedAppsList: MutableList<AppDetail> = mutableListOf()

    private var isViewingBlacklist = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchBarView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        refreshBlacklist()
        resetSessionFlags()

        blacklistedAppDetailsList = AppUtils.getSelectedAppDetailsList(this, blacklistedAppPackageNamesList)

        appsAdapter = AppsAdapter(blacklistedAppDetailsList, isViewingBlacklist) { app ->
            onAppSelected(app)
        }

        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        searchBarView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.updateList(blacklistedAppDetailsList)

        plusButton.setOnClickListener { toggleView() }

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

        RequestPermissionUtils.checkAndRequestAllPermissions(this)
        checkAndRequestAccessibilityPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity  ::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (!isViewingBlacklist) {
            toggleView()
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshBlacklist() {
        blacklistedAppPackageNamesList.clear()
        blacklistedAppPackageNamesList.addAll(SharedPreferencesUtils.getBlacklistedApps(this))
    }

    private fun onAppSelected(app: AppDetail) {
        if (isViewingBlacklist) {
            blacklistedAppPackageNamesList.remove(app.packageName)
            blacklistedAppDetailsList.remove(app)
            whitelistedAppsList.add(app)
            showToast("${app.appName} removed from blacklist")
        } else {
            blacklistedAppPackageNamesList.add(app.packageName)
            blacklistedAppDetailsList.add(app)
            whitelistedAppsList.remove(app)
            showToast("${app.appName} added to blacklist")
        }

        SharedPreferencesUtils.saveSelectedApps(this, blacklistedAppPackageNamesList)

        refreshBlacklist()
        if (isViewingBlacklist) {
            appsAdapter.updateList(blacklistedAppDetailsList)
        } else {
            appsAdapter.updateList(whitelistedAppsList)
        }
    }

    private fun resetSessionFlags() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (packageName in blacklistedAppPackageNamesList) {
            editor.putBoolean("SESSION_ACTIVE_$packageName", false)
        }
        editor.apply()
    }

    private fun toggleView() {
        if (isViewingBlacklist) {
            isViewingBlacklist = false
            searchBarView.visibility = View.VISIBLE
            plusButton.text = "Done"

            whitelistedAppsList = AppUtils.getNonSelectedApps(this, blacklistedAppPackageNamesList)
            appsAdapter.updateList(whitelistedAppsList)

            appsAdapter.setViewState(isViewingBlacklist)
            appsRecyclerView.scrollToPosition(0)

            searchBarView.requestFocus()
            searchBarView.isIconified = false
            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.showKeyboard(this, searchBarView)
            }, 200)
        } else {
            isViewingBlacklist = true
            searchBarView.visibility = View.GONE
            plusButton.text = "Add"

            appsAdapter.updateList(blacklistedAppDetailsList)
            appsAdapter.setViewState(isViewingBlacklist)
            appsRecyclerView.scrollToPosition(0)

            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.hideKeyboard(this, searchBarView)
            }, 200)

            searchBarView.setQuery("", false)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkAndRequestAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled(this, AppLaunchAccessibilityService::class.java)) {
            AlertDialog.Builder(this)
                .setTitle("Enable Accessibility Service")
                .setMessage("To enable app blocking, please allow accessibility service for this app.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val colonSplitter = enabledServices?.split(":") ?: return false
        return colonSplitter.any { it.contains(serviceClass.name) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
            Toast.makeText(this, "Permissions are required for proper functionality", Toast.LENGTH_LONG).show()
        }
    }
}
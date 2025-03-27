package com.social.media.decondition

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.utils.AppListManager
import com.social.media.decondition.utils.AppPermissionHandler

/**
 * MainActivity for the app monitoring selection interface.
 * Delegates most functionality to helper classes for cleaner organization.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var appListManager: AppListManager
    private lateinit var permissionHandler: AppPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize managers
        preferencesManager = PreferencesManager.getInstance(this)
        permissionHandler = AppPermissionHandler(this)

        // Set up UI components
        val recyclerView: RecyclerView = findViewById(R.id.appsRecyclerView)
        val searchView: SearchView = findViewById(R.id.searchView)
        val addButton: Button = findViewById(R.id.plusButton)

        // Set up recycler view layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize app list manager
        appListManager = AppListManager(
            this,
            recyclerView,
            searchView,
            addButton,
            preferencesManager
        )
        appListManager.initialize()

        // Request permissions
        permissionHandler.requestAllPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Let app list manager handle back press first
        if (!appListManager.handleBackPress()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
            Toast.makeText(this, "Permissions are required for proper functionality", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionHandler.handleActivityResult(requestCode, resultCode)
    }
}
package com.social.media.decondition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.SearchView
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
    private var appsList: List<AppDetail> = listOf()
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

        plusButton.setOnClickListener {
            if (searchView.visibility == View.VISIBLE) {
                searchView.visibility = View.GONE
                appsRecyclerView.visibility = View.GONE
                hideKeyboard()
            } else {
                searchView.visibility = View.VISIBLE
                appsRecyclerView.visibility = View.VISIBLE
                searchView.requestFocus()
                searchView.setIconified(false) // Expand the search view if it's iconified
                showKeyboard(searchView)
            }
        }

        appsList = getInstalledApps()
        appsAdapter = AppsAdapter(appsList, ::onAppSelected)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val filteredList = appsList.filter { it.appName.contains(newText, ignoreCase = true) }
                    appsAdapter.filterList(filteredList)
                }
                return true
            }
        })
    }

    private fun onAppSelected(app: AppDetail) {
        selectedApps.add(app.appName)
        // Save selected apps to SharedPreferences
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("selectedApps", selectedApps)
            apply()
        }
    }

    private fun getInstalledApps(): List<AppDetail> {
        val pm = packageManager
        val apps = mutableListOf<AppDetail>()

        // Use a different method to get only apps that can be launched
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packages = pm.queryIntentActivities(intent, 0)

        for (resolveInfo in packages) {
            val activityInfo = resolveInfo.activityInfo
            val appName = activityInfo.loadLabel(pm).toString()
            val packageName = activityInfo.packageName
            val icon = activityInfo.loadIcon(pm)
            apps.add(AppDetail(appName, packageName, icon))
        }
        apps.sortBy { it.appName.lowercase(Locale.ROOT).trim() }
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
}
package com.social.media.decondition

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SearchView
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
    private lateinit var appsAdapter: AppsAdapter
    private val selectedAppPackageNamesList = mutableSetOf<String>()
    private var selectedAppDetailsList: MutableList<AppDetail> = mutableListOf()
    private var nonSelectedAppsList: MutableList<AppDetail> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        selectedAppPackageNamesList.addAll(SharedPreferencesUtils.getSelectedApps(this))
        // Reset session flags for all selected apps
        resetSessionFlags()
        selectedAppDetailsList = AppUtils.getSelectedAppDetailsList(this, selectedAppPackageNamesList)
        appsAdapter = AppsAdapter(selectedAppDetailsList, ::onAppSelected)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        searchView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.filterList(selectedAppDetailsList)

        plusButton.setOnClickListener { toggleView() }

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

        AccessibilityServiceUtils.showAccessibilityServiceDialog(this)
    }


    override fun onBackPressed() {
        if (searchView.visibility == View.VISIBLE) {
            searchView.visibility = View.GONE
            appsAdapter.filterList(selectedAppDetailsList)
            KeyboardUtils.hideKeyboard(this, searchView)
        } else {
            super.onBackPressed()
        }
    }

    private fun onAppSelected(app: AppDetail) {
        selectedAppPackageNamesList.add(app.packageName)
        SharedPreferencesUtils.saveSelectedApps(this, selectedAppPackageNamesList)
        nonSelectedAppsList.remove(app)
        selectedAppDetailsList.add(app)
        appsAdapter.notifyDataSetChanged()
    }

    private fun resetSessionFlags() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        for (packageName in selectedAppPackageNamesList) {
            editor.putBoolean("SESSION_ACTIVE_$packageName", false)
        }
        editor.apply()
    }

    private fun toggleView() {
        if (searchView.visibility == View.VISIBLE) {
            searchView.visibility = View.GONE
            appsAdapter.filterList(selectedAppDetailsList)
            KeyboardUtils.hideKeyboard(this, searchView)
        } else {
            searchView.visibility = View.VISIBLE
            appsRecyclerView.visibility = View.VISIBLE
            searchView.requestFocus()
            searchView.setIconified(false)
            KeyboardUtils.showKeyboard(this, searchView)
            nonSelectedAppsList = AppUtils.getNonSelectedApps(this, selectedAppPackageNamesList)
            appsAdapter.filterList(nonSelectedAppsList)
        }
    }
}
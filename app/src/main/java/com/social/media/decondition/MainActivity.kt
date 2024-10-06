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
    private lateinit var searchBarView: SearchView
    private lateinit var plusButton: Button
    private lateinit var appsAdapter: AppsAdapter
    private val blacklistedAppPackageNamesList = mutableSetOf<String>()
    private var blacklistedAppDetailsList: MutableList<AppDetail> = mutableListOf()
    private var whitelistedAppsList: MutableList<AppDetail> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchBarView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        blacklistedAppPackageNamesList.addAll(SharedPreferencesUtils.getBlacklistedApps(this))
        resetSessionFlags()
        blacklistedAppDetailsList = AppUtils.getSelectedAppDetailsList(this, blacklistedAppPackageNamesList)
        appsAdapter = AppsAdapter(blacklistedAppDetailsList, ::onAppSelected)
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appsAdapter

        searchBarView.visibility = View.GONE
        appsRecyclerView.visibility = View.VISIBLE
        appsAdapter.filterList(blacklistedAppDetailsList)

        plusButton.setOnClickListener { toggleView() }

        searchBarView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    val filteredList = whitelistedAppsList.filter { it.appName.contains(newText, ignoreCase = true) }
                    appsAdapter.filterList(filteredList)
                }
                return true
            }
        })

        AccessibilityServiceUtils.showAccessibilityServiceDialog(this)
    }


    override fun onBackPressed() {
        if (searchBarView.visibility == View.VISIBLE) {
            searchBarView.visibility = View.GONE
            appsAdapter.filterList(blacklistedAppDetailsList)
            KeyboardUtils.hideKeyboard(this, searchBarView)
        } else {
            super.onBackPressed()
        }
    }

    private fun onAppSelected(app: AppDetail) {
        blacklistedAppPackageNamesList.add(app.packageName)
        SharedPreferencesUtils.saveSelectedApps(this, blacklistedAppPackageNamesList)
        whitelistedAppsList.remove(app)
        blacklistedAppDetailsList.add(app)
        appsAdapter.notifyDataSetChanged()
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
        if (searchBarView.visibility == View.VISIBLE) {
            searchBarView.visibility = View.GONE
            appsAdapter.filterList(blacklistedAppDetailsList)
            KeyboardUtils.hideKeyboard(this, searchBarView)
        } else {
            searchBarView.visibility = View.VISIBLE
            appsRecyclerView.visibility = View.VISIBLE
            searchBarView.requestFocus()
            searchBarView.setIconified(false)
            KeyboardUtils.showKeyboard(this, searchBarView)
            whitelistedAppsList = AppUtils.getNonSelectedApps(this, blacklistedAppPackageNamesList)
            appsAdapter.filterList(whitelistedAppsList)
        }
    }
}
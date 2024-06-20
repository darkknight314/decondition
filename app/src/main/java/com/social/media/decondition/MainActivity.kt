package com.social.media.decondition

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        plusButton = findViewById(R.id.plusButton)
        searchView = findViewById(R.id.searchView)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        plusButton.setOnClickListener {
            if (searchView.visibility == View.VISIBLE) {
                searchView.visibility = View.GONE
                appsRecyclerView.visibility = View.GONE
            } else {
                searchView.visibility = View.VISIBLE
                appsRecyclerView.visibility = View.VISIBLE
            }
        }

        appsList = getInstalledApps()
        appsAdapter = AppsAdapter(appsList)
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
}
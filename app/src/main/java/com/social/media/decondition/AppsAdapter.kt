package com.social.media.decondition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail

class AppsAdapter(
    private var appsList: List<AppDetail>,
    private val onAppSelected: (AppDetail) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appName: TextView = itemView.findViewById(R.id.appName)
        var appIcon: ImageView = itemView.findViewById(R.id.appIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = appsList[position]
        holder.appName.text = app.appName
        holder.appIcon.setImageDrawable(app.icon)

        holder.itemView.setOnClickListener {
            onAppSelected(app)
        }
    }

    override fun getItemCount() = appsList.size

    fun filterList(filteredAppsList: List<AppDetail>) {
        this.appsList = filteredAppsList
        notifyDataSetChanged()
    }
}
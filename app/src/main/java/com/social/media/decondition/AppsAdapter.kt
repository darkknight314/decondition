package com.social.media.decondition

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail

class AppsAdapter(
    private var appsList: MutableList<AppDetail>,
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
            showConfirmationDialog(holder.itemView, app, position)
        }
    }

    override fun getItemCount() = appsList.size

    private fun showConfirmationDialog(view: View, app: AppDetail, position: Int) {
        val context = view.context
        AlertDialog.Builder(context)
            .setTitle("Confirm Selection")
            .setMessage("Do you want to remove ${app.appName}?")
            .setPositiveButton("Yes") { _, _ ->
                onAppSelected(app)
                appsList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, appsList.size)
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun filterList(filteredAppsList: List<AppDetail>) {
        this.appsList = filteredAppsList.toMutableList()
        notifyDataSetChanged()
    }
}
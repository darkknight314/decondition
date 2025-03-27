package com.social.media.decondition

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.social.media.decondition.data.AppDetail

class AppsAdapter(
    private var appsList: MutableList<AppDetail>,
    private var isMonitoredAppsView: Boolean,
    private val onAppSelected: (AppDetail) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    /**
     * Updates the current view state.
     *
     * @param isMonitoredView true if viewing monitored apps, false if viewing all apps.
     */
    fun setViewState(isMonitoredView: Boolean) {
        this.isMonitoredAppsView = isMonitoredView
        notifyDataSetChanged()
    }

    /**
     * Updates the adapter's list with optimized diffing for smoother UI updates.
     *
     * @param newList The new list of AppDetail objects to display.
     */
    fun updateList(newList: List<AppDetail>) {
        appsList.clear()
        appsList.addAll(newList)
        notifyDataSetChanged()  // Ensure RecyclerView updates
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appName: TextView = itemView.findViewById(R.id.appName)
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
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
            showConfirmationDialog(holder.itemView, app)
        }
    }

    override fun getItemCount() = appsList.size

    private fun showConfirmationDialog(view: View, app: AppDetail) {
        val context = view.context
        val dialogTitle: String
        val dialogMessage: String

        if (isMonitoredAppsView) {
            dialogTitle = "Remove App"
            dialogMessage = "Stop monitoring ${app.appName}?"
        } else {
            dialogTitle = "Add App"
            dialogMessage = "Start monitoring ${app.appName}?"
        }

        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton("Yes") { _, _ -> onAppSelected(app) }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * DiffUtil Callback to efficiently calculate list differences
     */
    private class AppDiffCallback(
        private val oldList: List<AppDetail>,
        private val newList: List<AppDetail>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
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
    private var isBlacklistView: Boolean, // Flag to determine current view
    private val onAppSelected: (AppDetail) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    /**
     * Updates the current view state and refreshes the adapter.
     *
     * @param isBlacklist true if viewing blacklist, false if viewing whitelist.
     */
    fun setViewState(isBlacklist: Boolean) {
        this.isBlacklistView = isBlacklist
        notifyDataSetChanged()
    }

    /**
     * Updates the adapter's list and refreshes the RecyclerView.
     *
     * @param newList The new list of AppDetail objects to display.
     */
    fun updateList(newList: MutableList<AppDetail>) {
        this.appsList = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for app items.
     */
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
            showConfirmationDialog(holder.itemView, app)
        }
    }

    override fun getItemCount() = appsList.size

    /**
     * Displays a confirmation dialog based on the current view state.
     *
     * @param view The item view.
     * @param app The AppDetail object representing the selected app.
     */
    private fun showConfirmationDialog(view: View, app: AppDetail) {
        val context = view.context
        val dialogTitle: String
        val dialogMessage: String

        if (isBlacklistView) {
            dialogTitle = "Confirm Removal"
            dialogMessage = "Do you want to remove ${app.appName} from the blacklist?"
        } else {
            dialogTitle = "Confirm Addition"
            dialogMessage = "Do you want to add ${app.appName} to the blacklist?"
        }

        AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton("Yes") { _, _ ->
                onAppSelected(app)
                // Do NOT remove the app from the adapter's list here
                // Let the activity handle list modifications and adapter updates
            }
            .setNegativeButton("No", null)
            .show()
    }
}
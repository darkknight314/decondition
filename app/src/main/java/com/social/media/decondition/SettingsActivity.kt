package com.social.media.decondition

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("AppSelections", Context.MODE_PRIVATE)

        // Show activity selection dialog on launch
        showActivitySelectionDialog()
    }

    private fun showActivitySelectionDialog() {
        val activities = arrayOf(
            "Sudoku Puzzle" to "com.social.media.decondition.SudokuPuzzleActivity",
            "Math Challenge" to "com.social.media.decondition.MathChallengeActivity",
            "Typing Test" to "com.social.media.decondition.TypingTestActivity"
        )

        val activityNames = activities.map { it.first }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select an Activity")
            .setItems(activityNames) { _, which ->
                val selectedActivity = activities[which].second
                saveSelectedActivity(selectedActivity)
            }
            .setOnDismissListener {
                finish() // Close settings after selection
            }
            .show()
    }

    private fun saveSelectedActivity(activityClassName: String) {
        sharedPreferences.edit().putString("GLOBAL_ACTIVITY", activityClassName).apply()
        Toast.makeText(this, "Activity set successfully!", Toast.LENGTH_SHORT).show()
    }
}
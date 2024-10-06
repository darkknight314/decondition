package com.social.media.decondition

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.social.media.decondition.ui.theme.SudokuCellDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SudokuPuzzleActivity : AppCompatActivity() {

    private lateinit var dbHelper: SudokuDatabaseHelper
    private lateinit var solution: String
    private lateinit var editTexts: Array<EditText?>
    private var triggeringAppPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_puzzle)

        dbHelper = SudokuDatabaseHelper(this)

        val randomPuzzle = dbHelper.getRandomSudokuPuzzle()
        randomPuzzle?.let {
//            solution = it.solution
//            var puzzle = solution.substring(0, 10) + '0' + solution.substring(10 + 1)
//            displaySudokuPuzzle(puzzle)
             displaySudokuPuzzle(it.puzzle)
        }

        triggeringAppPackageName = intent.getStringExtra("APP_PACKAGE_NAME")
    }

    private fun displaySudokuPuzzle(puzzle: String) {
        val tableLayout = findViewById<TableLayout>(R.id.sudoku_table)
        val BORDER_WIDTH = 3

        editTexts = arrayOfNulls(81)
        for (i in 0 until 9) {
            val row = TableRow(this)
            row.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            for (j in 0 until 9) {
                val index = i * 9 + j
                val editText = EditText(this)

                val cellDrawable = SudokuCellDrawable(
                    Color.WHITE,
                    Color.BLACK,
                    i,
                    j,
                    BORDER_WIDTH
                )

                editText.background = cellDrawable

                editText.filters = arrayOf(
                    InputFilter { source, _, _, _, _, _ ->
                        source.filter { it.isDigit() }
                    },
                    InputFilter.LengthFilter(1)
                )
                editText.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
                editText.setTextSize(18f)
                editText.gravity = android.view.Gravity.CENTER
                editText.isFocusable = puzzle[index] == '0'
                editText.isFocusableInTouchMode = puzzle[index] == '0'
                if (puzzle[index] != '0') {
                    editText.setText(puzzle[index].toString())
                    editText.isEnabled = false
                } else {
                    editText.setText("")
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable?) {
                            if (isPuzzleComplete()) {
                                checkSolution()
                            }
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {}
                    })
                }
                editTexts[index] = editText
                row.addView(editText)
            }
            tableLayout.addView(row)
        }
    }

    private fun isPuzzleComplete(): Boolean {
        for (i in 0 until 81) {
            if (editTexts[i]?.text.toString().isEmpty()) {
                return false
            }
        }
        return true
    }

    private fun checkSolution() {
        var correct = true
        for (i in 0 until 81) {
            val enteredText = editTexts[i]?.text.toString()
            val correctText = solution[i].toString()
            if (enteredText != correctText) {
                correct = false
                break
            }
        }
        if (correct) {
            Toast.makeText(
                this,
                "Congratulations! The puzzle is solved correctly.",
                Toast.LENGTH_LONG
            ).show()
            setPuzzleSolvedFlag()
            launchOriginalApp()
            finish()
        } else {
            Toast.makeText(
                this,
                "The solution is incorrect. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setPuzzleSolvedFlag() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("PUZZLE_SOLVED_$triggeringAppPackageName", true)
        editor.putBoolean("SESSION_ACTIVE_$triggeringAppPackageName", true)
        editor.apply()
        println("Setting as solved for " + triggeringAppPackageName)
    }

    private fun launchOriginalApp() {
        triggeringAppPackageName?.let {
            val launchIntent = packageManager.getLaunchIntentForPackage(it)
            if (launchIntent != null) {
                setPuzzleSolvedFlag()
                startActivity(launchIntent)
                GlobalScope.launch(Dispatchers.Main) {
                    delay(10000)
                    resetSessionFlag()
                }
            } else {
                Toast.makeText(this, "Unable to launch the app.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resetSessionFlag() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("SESSION_ACTIVE_$triggeringAppPackageName", false)
        println("resetting session flag")
        editor.apply()
    }
}
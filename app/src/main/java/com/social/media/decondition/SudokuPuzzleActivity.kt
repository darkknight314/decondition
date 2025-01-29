package com.social.media.decondition

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
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

    // We'll store EditText references for each cell
    private lateinit var editTexts: Array<EditText?>
    private var triggeringAppPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_puzzle)

        dbHelper = SudokuDatabaseHelper(this)

        // For demonstration, we're fetching a random puzzle from the DB
        val randomPuzzle = dbHelper.getRandomSudokuPuzzle()
        randomPuzzle?.let {
            // If you also want to track a solution, uncomment these lines:
            // solution = it.solution
            // displaySudokuPuzzle(it.puzzle)

            // Currently just displaying the puzzle:
            displaySudokuPuzzle("0" + it.solution.substring(1, it.solution.length), it.solution) //TODO: REPLACE WITH it.puzzle
        }


        // Retrieve package name of the original “triggering” app
        triggeringAppPackageName = intent.getStringExtra("APP_PACKAGE_NAME")
    }

    private fun displaySudokuPuzzle(puzzle: String, solution: String) {
        val tableLayout = findViewById<TableLayout>(R.id.sudoku_table)

        // Configure line widths for SudokuCellDrawable

        editTexts = arrayOfNulls(81)

        for (i in 0 until 9) {
            val row = TableRow(this).apply {
                layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (j in 0 until 9) {
                val index = i * 9 + j
                val editText = EditText(this).apply {
                    // Use the SudokuCellDrawable with thin/thick widths
                    background = SudokuCellDrawable(
                        backgroundColor = Color.WHITE,
                        borderColor     = Color.BLACK,
                        row             = i,
                        col             = j
                    )

                    filters = arrayOf(
                        // Restrict input to digits only
                        InputFilter { source, _, _, _, _, _ ->
                            source.filter { it.isDigit() }
                        },
                        // Max length = 1 character
                        InputFilter.LengthFilter(1)
                    )

                    // Basic layout & style
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )
                    setTextSize(18f)
                    gravity = Gravity.CENTER

                    // If puzzle[index] != '0', it's a prefilled cell
                    if (puzzle[index] != '0') {
                        setText(puzzle[index].toString())
                        isEnabled = false
                    } else {
                        // Otherwise, it's an editable cell
                        setText("")
                        isFocusable = true
                        isFocusableInTouchMode = true

                        addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(s: Editable?) {
                                // If you want to check solution upon full input:
                                if (isPuzzleComplete()) {
                                    checkSolution(solution)
                                }
                            }
                            override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                            ) {}
                            override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
                            ) {}
                        })
                    }
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

    private fun checkSolution(solution: String) {
        // If you have a known solution string, compare each cell’s content:
        // (Uncomment if you’re storing the solution in `solution`)

         var correct = true
         for (i in 0 until 81) {
             val enteredText = editTexts[i]?.text.toString()
             if (enteredText != solution[i].toString()) {
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

        // If you don’t have a “known” solution, just do your own logic
//        Toast.makeText(this, "Puzzle complete!5", Toast.LENGTH_LONG).show()
    }

    private fun setPuzzleSolvedFlag() {
        val sharedPreferences = getSharedPreferences("AppSelections", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("PUZZLE_SOLVED_$triggeringAppPackageName", true)
        editor.putBoolean("SESSION_ACTIVE_$triggeringAppPackageName", true)
        editor.apply()
    }

    private fun launchOriginalApp() {
        triggeringAppPackageName?.let {
            val launchIntent = packageManager.getLaunchIntentForPackage(it)
            if (launchIntent != null) {
                setPuzzleSolvedFlag()
                startActivity(launchIntent)

                // Reset session after some delay if needed
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
        editor.apply()
    }
}
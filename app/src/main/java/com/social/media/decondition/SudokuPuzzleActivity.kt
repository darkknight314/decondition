package com.social.media.decondition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.GridLayout
import android.widget.EditText
import android.view.ViewGroup.LayoutParams
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast

class SudokuPuzzleActivity : AppCompatActivity() {

    private lateinit var dbHelper: SudokuDatabaseHelper
    private lateinit var solution: String
    private lateinit var editTexts: Array<EditText?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku_puzzle)

        dbHelper = SudokuDatabaseHelper(this)

        val randomPuzzle = dbHelper.getRandomSudokuPuzzle()
        randomPuzzle?.let {
            solution = it.solution
            println("SOLUTION: "+solution)
            displaySudokuPuzzle(it.puzzle)
        }
    }

    private fun displaySudokuPuzzle(puzzle: String) {
        val gridLayout = findViewById<GridLayout>(R.id.sudoku_grid)
        gridLayout.columnCount = 9
        gridLayout.rowCount = 9

        editTexts = arrayOfNulls(81)
        for (i in 0 until 81) {
            val textView = EditText(this)
            textView.setTextSize(18f)
            textView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            textView.isFocusable = puzzle[i] == '0'
            textView.isFocusableInTouchMode = puzzle[i] == '0'
            if (puzzle[i] != '0') {
                textView.setText(puzzle[i].toString())
                textView.isEnabled = false
            } else {
                textView.setText("")
                textView.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        if (isPuzzleComplete()) {
                            checkSolution()
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
            editTexts[i] = textView
            gridLayout.addView(textView)
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
            Toast.makeText(this, "Congratulations! The puzzle is solved correctly.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "The solution is incorrect. Please try again.", Toast.LENGTH_LONG).show()
        }
    }
}
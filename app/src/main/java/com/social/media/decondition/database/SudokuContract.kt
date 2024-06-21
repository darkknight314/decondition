package com.social.media.decondition

import android.provider.BaseColumns

object SudokuContract {
    object SudokuEntry : BaseColumns {
        const val TABLE_NAME = "sudoku"
        const val COLUMN_NAME_PUZZLE = "puzzle"
        const val COLUMN_NAME_SOLUTION = "solution"
    }
}
package com.social.media.decondition

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.social.media.decondition.data.SudokuPuzzle
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream

class SudokuDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {
        if (!checkDatabase()) {
            try {
                copyDatabase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    private fun checkDatabase(): Boolean {
//        val dbFile = context.getDatabasePath(DATABASE_NAME)
//        return dbFile.exists()
        return true
    }

    @Throws(IOException::class)
    private fun copyDatabase() {
        val inputStream = context.assets.open(DATABASE_NAME)
        val outputFile = "/Users/pratikkakade/Downloads/sudoku_puzzles.csv"
        val outputStream: OutputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        // Insert initial data if necessary
        loadCsvIntoDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    private fun loadCsvIntoDatabase(db: SQLiteDatabase) {
        val inputStream = context.assets.open("sudoku_puzzles.csv")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        db.beginTransaction()
        try {
            var line = bufferedReader.readLine()
            while (line != null) {
                val columns = line.split(",")
                if (columns.size == 2) {
                    val puzzle = columns[0]
                    val solution = columns[1]
                    val values = ContentValues().apply {
                        put(SudokuContract.SudokuEntry.COLUMN_NAME_PUZZLE, puzzle)
                        put(SudokuContract.SudokuEntry.COLUMN_NAME_SOLUTION, solution)
                    }
                    db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, values)
                }
                line = bufferedReader.readLine()
            }
            db.setTransactionSuccessful()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            bufferedReader.close()
        }
    }

    fun getRandomSudokuPuzzle(): SudokuPuzzle? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${SudokuContract.SudokuEntry.TABLE_NAME} ORDER BY RANDOM() LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            val puzzle = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_NAME_PUZZLE))
            val solution = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_NAME_SOLUTION))
            cursor.close()
            SudokuPuzzle(puzzle, solution)
        } else {
            cursor.close()
            null
        }
    }

    companion object {
        const val DATABASE_NAME = "SudokuPuzzles.db"
        const val DATABASE_VERSION = 1

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${SudokuContract.SudokuEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${SudokuContract.SudokuEntry.COLUMN_NAME_PUZZLE} TEXT," +
                    "${SudokuContract.SudokuEntry.COLUMN_NAME_SOLUTION} TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${SudokuContract.SudokuEntry.TABLE_NAME}"
    }

    private val databasePath: String = context.getDatabasePath(DATABASE_NAME).absolutePath
}
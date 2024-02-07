package com.geekglasses.wordy.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.geekglasses.wordy.entity.Word

class DataBaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "wordyDB"
        const val DATABASE_VERSION = 1
        const val WORD_TABLE = "WORD_TABLE"
        const val COLUMN_WRITING_FORM = "WRITING_FORM"
        const val COLUMN_TRANSLATION = "TRANSLATION"
        const val COLUMN_STRUGGLE = "STRUGGLE"
        const val COLUMN_FRESHNESS = "FRESHNESS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableStatement = "CREATE TABLE $WORD_TABLE (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_WRITING_FORM TEXT, " +
                "$COLUMN_TRANSLATION TEXT, " +
                "$COLUMN_STRUGGLE INT, " +
                "$COLUMN_FRESHNESS INT)"
        db.execSQL(createTableStatement)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun addOne(word: Word): Boolean {
        val cv = ContentValues().apply {
            put(COLUMN_WRITING_FORM, word.writingForm)
            put(COLUMN_TRANSLATION, word.translation)
            put(COLUMN_STRUGGLE, word.struggle)
            put(COLUMN_FRESHNESS, word.freshness)
        }
        return try {
            writableDatabase.insert(WORD_TABLE, null, cv) > 0
        } catch (e: Exception) {
            println("Failed to add data to DB, message: ${e.message}")
            false
        }
    }

    fun isWordExists(word: String): Boolean {
        val cursor: Cursor = readableDatabase.rawQuery("SELECT * FROM $WORD_TABLE WHERE $COLUMN_WRITING_FORM=?", arrayOf(word))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun clearWordTable() {
        writableDatabase.delete(WORD_TABLE, null, null)
        writableDatabase.close()
    }

    fun getAllWords(): List<Word> {
        val wordList = mutableListOf<Word>()
        val cursor: Cursor = writableDatabase.rawQuery("SELECT * FROM $WORD_TABLE", null)
        cursor.use {
            val idColumnIndex = it.getColumnIndex("ID")
            val writingFormColumnIndex = it.getColumnIndex("WRITING_FORM")
            val translationColumnIndex = it.getColumnIndex("TRANSLATION")
            val struggleColumnIndex = it.getColumnIndex("STRUGGLE")
            val freshnessColumnIndex = it.getColumnIndex("FRESHNESS")

            while (it.moveToNext()) {
                val word = Word().apply {
                    writingForm = it.getString(writingFormColumnIndex)
                    translation = it.getString(translationColumnIndex)
                    struggle = it.getInt(struggleColumnIndex)
                    freshness = it.getInt(freshnessColumnIndex)
                }
                wordList.add(word)
            }
        }
        return wordList
    }

    fun deleteWordByWritingForm(wordToDelete: String): Boolean {
        val deletedRows = writableDatabase.delete(WORD_TABLE, "$COLUMN_WRITING_FORM=?", arrayOf(wordToDelete))
        return deletedRows > 0
    }

    fun updateStruggleForWord(word: String, struggleCount: Int) {
        writableDatabase.execSQL("UPDATE $WORD_TABLE SET $COLUMN_STRUGGLE = $struggleCount WHERE $COLUMN_WRITING_FORM = '$word'")
    }

    fun updateFreshnessForWord(word: String, freshnessIncrement: Int) {
        writableDatabase.execSQL("UPDATE $WORD_TABLE SET $COLUMN_FRESHNESS = $COLUMN_FRESHNESS + $freshnessIncrement WHERE $COLUMN_WRITING_FORM = ?", arrayOf(word))
    }
}

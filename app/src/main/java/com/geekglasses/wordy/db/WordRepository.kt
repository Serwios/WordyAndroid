package com.geekglasses.wordy.db

import android.content.Context
import android.database.Cursor
import com.geekglasses.wordy.entity.Word

class WordRepository(context: Context?) : DataBaseHelper(context) {
    fun isWordExists(word: String): Boolean {
        val cursor: Cursor = readableDatabase.rawQuery(
            "SELECT * FROM $WORD_TABLE WHERE $COLUMN_WRITING_FORM=?",
            arrayOf(word)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllWords(): List<Word> {
        val wordList = mutableListOf<Word>()
        val cursor: Cursor = writableDatabase.rawQuery("SELECT * FROM $WORD_TABLE", null)
        cursor.use {
            val idColumnIndex = it.getColumnIndex("ID")
            val writingFormColumnIndex = it.getColumnIndex(COLUMN_WRITING_FORM)
            val translationColumnIndex = it.getColumnIndex(COLUMN_TRANSLATION)
            val struggleColumnIndex = it.getColumnIndex(COLUMN_STRUGGLE)
            val freshnessColumnIndex = it.getColumnIndex(COLUMN_FRESHNESS)

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

    fun getMostStruggledWord(): String? {
        var mostStruggledWord: String? = null

        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT $COLUMN_WRITING_FORM, MAX($COLUMN_STRUGGLE) FROM $WORD_TABLE", null)

        cursor.use {
            if (it.moveToFirst()) {
                val writingFormIndex = it.getColumnIndex(COLUMN_WRITING_FORM)
                if (writingFormIndex != -1) {
                    mostStruggledWord = it.getString(writingFormIndex)
                }
            }
        }

        cursor.close()
        return mostStruggledWord
    }

    fun deleteWordByWritingForm(wordToDelete: String): Boolean {
        val deletedRows =
            writableDatabase.delete(WORD_TABLE, "$COLUMN_WRITING_FORM=?", arrayOf(wordToDelete))
        return deletedRows > 0
    }

    fun updateStruggleForWord(word: String, struggleCount: Int) {
        writableDatabase.execSQL("UPDATE $WORD_TABLE SET $COLUMN_STRUGGLE = $struggleCount WHERE $COLUMN_WRITING_FORM = '$word'")
    }

    fun updateFreshnessForWord(word: String, freshnessIncrement: Int) {
        writableDatabase.execSQL(
            "UPDATE $WORD_TABLE SET $COLUMN_FRESHNESS = $COLUMN_FRESHNESS + $freshnessIncrement WHERE $COLUMN_WRITING_FORM = ?",
            arrayOf(word)
        )
    }


}
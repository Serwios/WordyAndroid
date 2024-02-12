package com.geekglasses.wordy.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.geekglasses.wordy.entity.Word

class DataBaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "wordyDB"
        const val DATABASE_VERSION = 5
        const val DICTIONARY_TABLE = "DICTIONARY_TABLE"
        const val COLUMN_DICTIONARY_ID = "DICTIONARY_ID"
        const val COLUMN_DICTIONARY_NAME = "DICTIONARY_NAME"
        const val COLUMN_IS_PICKED = "IS_PICKED"
        const val WORD_TABLE = "WORD_TABLE"
        const val COLUMN_WORD_ID = "ID"
        const val COLUMN_WRITING_FORM = "WRITING_FORM"
        const val COLUMN_TRANSLATION = "TRANSLATION"
        const val COLUMN_STRUGGLE = "STRUGGLE"
        const val COLUMN_FRESHNESS = "FRESHNESS"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createDictionaryTableStatement =
            "CREATE TABLE $DICTIONARY_TABLE ($COLUMN_DICTIONARY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_DICTIONARY_NAME TEXT, $COLUMN_IS_PICKED INTEGER)"
        val createWordTableStatement =
            "CREATE TABLE $WORD_TABLE ($COLUMN_WORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_DICTIONARY_ID INTEGER, " +
                    "$COLUMN_WRITING_FORM TEXT, " +
                    "$COLUMN_TRANSLATION TEXT, " +
                    "$COLUMN_STRUGGLE INT, " +
                    "$COLUMN_FRESHNESS INT, " +
                    "FOREIGN KEY($COLUMN_DICTIONARY_ID) REFERENCES $DICTIONARY_TABLE($COLUMN_DICTIONARY_ID))"

        db.execSQL(createDictionaryTableStatement)
        db.execSQL(createWordTableStatement)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS $WORD_TABLE")
            db.execSQL("DROP TABLE IF EXISTS $DICTIONARY_TABLE")
            onCreate(db)
        }
    }

    fun addOneWordForPickedDictionary(word: Word): Boolean {
        val currentDictionary = getCurrentPickedDictionary() ?: return false
        return addOneWordForDictionary(word, currentDictionary)
    }

    fun addOneWordForDictionary(word: Word, dictionaryName: String): Boolean {
        val dictionaryId = getDictionaryIdByName(dictionaryName) ?: return false // Retrieve dictionary ID
        val cv = ContentValues().apply {
            put(COLUMN_DICTIONARY_ID, dictionaryId)
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

    private fun getDictionaryIdByName(dictionaryName: String): Long? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_DICTIONARY_ID FROM $DICTIONARY_TABLE WHERE $COLUMN_DICTIONARY_NAME = ?",
            arrayOf(dictionaryName)
        )
        return cursor.use {
            val columnIndex = it.getColumnIndex(COLUMN_DICTIONARY_ID)
            if (columnIndex != -1 && it.moveToFirst()) {
                it.getLong(columnIndex)
            } else {
                null
            }
        }
    }

    fun isWordExists(word: String): Boolean {
        val cursor: Cursor = readableDatabase.rawQuery(
            "SELECT * FROM $WORD_TABLE WHERE $COLUMN_WRITING_FORM=?",
            arrayOf(word)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun clearWordTable() {
        writableDatabase.delete(WORD_TABLE, null, null)
        writableDatabase.close()
    }

    fun clearAllWordsForPickedDictionary() {
        val currentDictionaryName = getCurrentPickedDictionary() ?: return
        clearWordsForDictionary(currentDictionaryName)
    }

    fun clearWordsForDictionary(dictionaryName: String) {
        val dictionaryId = getDictionaryIdByName(dictionaryName) ?: return
        writableDatabase.delete(WORD_TABLE, "$COLUMN_DICTIONARY_ID=?", arrayOf(dictionaryId.toString()))
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

    fun isDictionaryExists(dictionaryName: String): Boolean {
        val cursor: Cursor = readableDatabase.rawQuery(
            "SELECT * FROM $DICTIONARY_TABLE WHERE $COLUMN_DICTIONARY_NAME=?",
            arrayOf(dictionaryName)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun addDictionary(dictionaryName: String): Boolean {
        val cv = ContentValues().apply {
            put(COLUMN_DICTIONARY_NAME, dictionaryName)
        }
        return try {
            writableDatabase.insert(DICTIONARY_TABLE, null, cv) > 0
        } catch (e: Exception) {
            println("Failed to add dictionary to DB, message: ${e.message}")
            false
        }
    }

    fun addInitialDictionary(dictionaryName: String): Boolean {
        val cv = ContentValues().apply {
            put(COLUMN_DICTIONARY_NAME, dictionaryName)
            put(COLUMN_IS_PICKED, 1)
        }
        return try {
            writableDatabase.insert(DICTIONARY_TABLE, null, cv) > 0
        } catch (e: Exception) {
            println("Failed to add dictionary to DB, message: ${e.message}")
            false
        }
    }

    fun hasDictionaries(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $DICTIONARY_TABLE", null)
        cursor.use {
            if (it.moveToFirst()) {
                val count = it.getInt(0)
                return count > 0
            }
        }
        return false
    }

    fun getCurrentPickedDictionary(): String? {
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT * FROM $DICTIONARY_TABLE WHERE $COLUMN_IS_PICKED = 1", null)
        cursor.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(COLUMN_DICTIONARY_NAME)
                if (columnIndex != -1) {
                    return it.getString(columnIndex)
                }
            }
        }
        return null
    }

    fun getWordsForCurrentPickedDictionary(): List<Word>? {
        val currentDictionaryName = getCurrentPickedDictionary()

        if (currentDictionaryName != null) {
            val words = mutableListOf<Word>()
            val db = readableDatabase

            val query = """
            SELECT $COLUMN_WRITING_FORM, $COLUMN_TRANSLATION, $COLUMN_STRUGGLE, $COLUMN_FRESHNESS 
            FROM $WORD_TABLE 
            WHERE $COLUMN_DICTIONARY_ID = (
                SELECT $COLUMN_DICTIONARY_ID 
                FROM $DICTIONARY_TABLE 
                WHERE $COLUMN_DICTIONARY_NAME = ?
            )
        """.trimIndent()

            val selectionArgs = arrayOf(currentDictionaryName)
            val cursor = db.rawQuery(query, selectionArgs)

            cursor.use {
                val writingFormIndex = cursor.getColumnIndex(COLUMN_WRITING_FORM)
                val translationIndex = cursor.getColumnIndex(COLUMN_TRANSLATION)
                val struggleIndex = cursor.getColumnIndex(COLUMN_STRUGGLE)
                val freshnessIndex = cursor.getColumnIndex(COLUMN_FRESHNESS)

                while (cursor.moveToNext()) {
                    val writingForm = if (writingFormIndex != -1) cursor.getString(writingFormIndex) else ""
                    val translation = if (translationIndex != -1) cursor.getString(translationIndex) else ""
                    val struggle = if (struggleIndex != -1) cursor.getInt(struggleIndex) else 0
                    val freshness = if (freshnessIndex != -1) cursor.getInt(freshnessIndex) else 0

                    val word = Word(writingForm, translation, struggle, freshness)
                    words.add(word)
                }
            }
            return words
        } else {
            return null
        }
    }

    fun clearDb() {
        val db = readableDatabase
        db.execSQL("DROP TABLE IF EXISTS $WORD_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $DICTIONARY_TABLE")
        onCreate(db)
    }
}

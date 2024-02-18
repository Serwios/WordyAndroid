package com.geekglasses.wordy.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.geekglasses.wordy.entity.Dictionary
import com.geekglasses.wordy.entity.Word

class DictionaryRepository(context: Context?) : DataBaseHelper(context) {
    fun addOneWordForPickedDictionary(word: Word): Boolean {
        val currentDictionary = getCurrentPickedDictionary() ?: return false
        return addOneWordForDictionary(word, currentDictionary)
    }

    fun addOneWordForDictionary(word: Word, dictionaryName: String): Boolean {
        val dictionaryId = getDictionaryIdByName(dictionaryName) ?: return false
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

    fun clearAllWordsForPickedDictionary() {
        val currentDictionaryName = getCurrentPickedDictionary() ?: return
        clearWordsForDictionary(currentDictionaryName)
    }

    fun clearWordsForDictionary(dictionaryName: String) {
        val dictionaryId = getDictionaryIdByName(dictionaryName) ?: return
        writableDatabase.delete(WORD_TABLE, "$COLUMN_DICTIONARY_ID=?", arrayOf(dictionaryId.toString()))
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

    fun getAllDictionaries(): List<Dictionary> {
        val dictionaryList = mutableListOf<Dictionary>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $DICTIONARY_TABLE", null)

        cursor.use {
            val idIndex = it.getColumnIndex(COLUMN_DICTIONARY_ID)
            val nameIndex = it.getColumnIndex(COLUMN_DICTIONARY_NAME)
            val isPickedIndex = it.getColumnIndex(COLUMN_IS_PICKED)

            while (it.moveToNext()) {
                val id = if (idIndex != -1) it.getLong(idIndex) else 0
                val name = if (nameIndex != -1) it.getString(nameIndex) else ""
                val isPicked = if (isPickedIndex != -1) it.getInt(isPickedIndex) == 1 else false

                val dictionary = Dictionary(id, name, isPicked)
                dictionaryList.add(dictionary)
            }
        }

        return dictionaryList
    }

    fun getDictionarySize(dictionaryName: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $WORD_TABLE WHERE $COLUMN_DICTIONARY_ID = (SELECT $COLUMN_DICTIONARY_ID FROM $DICTIONARY_TABLE WHERE $COLUMN_DICTIONARY_NAME = ?)",
            arrayOf(dictionaryName)
        )

        cursor.use {
            if (it.moveToFirst()) {
                return it.getInt(0)
            }
        }

        return 0
    }

    fun pickDictionary(dictionaryName: String): Boolean {
        val db = writableDatabase
        return try {
            db.beginTransaction()
            db.execSQL("UPDATE $DICTIONARY_TABLE SET $COLUMN_IS_PICKED = 0 WHERE $COLUMN_IS_PICKED = 1")
            db.execSQL("UPDATE $DICTIONARY_TABLE SET $COLUMN_IS_PICKED = 1 WHERE $COLUMN_DICTIONARY_NAME = ?", arrayOf(dictionaryName))
            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            println("Failed to pick dictionary, message: ${e.message}")
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteDictionary(dictionaryName: String): Boolean {
        val db = writableDatabase
        return try {
            db.beginTransaction()
            val dictionaryId = getDictionaryIdByName(dictionaryName)
            dictionaryId?.let { id ->
                db.delete(WORD_TABLE, "$COLUMN_DICTIONARY_ID = ?", arrayOf(id.toString()))
            }
            db.delete(DICTIONARY_TABLE, "$COLUMN_DICTIONARY_NAME = ?", arrayOf(dictionaryName))

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            println("Failed to delete dictionary from DB, message: ${e.message}")
            false
        } finally {
            db.endTransaction()
        }
    }
}
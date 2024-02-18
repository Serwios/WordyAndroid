package com.geekglasses.wordy.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

open class DataBaseHelper(context: Context?) :
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
}

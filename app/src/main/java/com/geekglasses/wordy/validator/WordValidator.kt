package com.geekglasses.wordy.validator

import com.geekglasses.wordy.db.DataBaseHelper

object WordValidator {
    fun isWordValid(word: String?): Boolean {
        return !word.isNullOrBlank()
    }

    fun isWordExist(word: String, dbHelper: DataBaseHelper): Boolean {
        return dbHelper.isWordExists(word);
    }
}
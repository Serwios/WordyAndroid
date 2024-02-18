package com.geekglasses.wordy.validator

import com.geekglasses.wordy.db.WordRepository

object WordValidator {
    fun isWordValid(word: String?): Boolean {
        return !word.isNullOrBlank()
    }

    fun isWordExist(word: String, wordRepo: WordRepository): Boolean {
        return wordRepo.isWordExists(word)
    }
}
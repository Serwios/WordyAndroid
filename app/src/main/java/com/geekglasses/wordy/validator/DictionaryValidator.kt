package com.geekglasses.wordy.validator

import com.geekglasses.wordy.db.DictionaryRepository

object DictionaryValidator {
    const val MAXIMUM_DICTIONARY_NAME_LENGTH = 10

    fun isDictionaryValid(dictionaryName: String?): Boolean {
        return !dictionaryName.isNullOrBlank() && dictionaryName.length < MAXIMUM_DICTIONARY_NAME_LENGTH
    }

    fun isDictionaryExist(dictionaryName: String, dictionaryRepo: DictionaryRepository): Boolean {
        return dictionaryRepo.isDictionaryExists(dictionaryName)
    }
}
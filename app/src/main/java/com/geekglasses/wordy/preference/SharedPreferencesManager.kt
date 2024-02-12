package com.geekglasses.wordy.preference

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCES_NAME = "common_state_pref"
        private const val CURRENT_DICTIONARY = "current_dictionary"
    }

    fun getCurrentDictionary(): String? {
        return sharedPreferences.getString(CURRENT_DICTIONARY, null)
    }

    fun setCurrentDictionary(value: String) {
        sharedPreferences.edit().putString(CURRENT_DICTIONARY, value).apply()
    }
}
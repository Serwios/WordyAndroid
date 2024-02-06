package com.geekglasses.wordy.service.scheduler.freshness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geekglasses.wordy.db.DataBaseHelper

class FreshnessUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DataBaseHelper(context)
        val wordList = dbHelper.allWords
        for (word in wordList) {
            dbHelper.updateFreshnessForWord(word.writingForm, -1)
        }
    }
}
package com.geekglasses.wordy.service.scheduler.freshness

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geekglasses.wordy.db.WordRepository

class FreshnessUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wordRepo = WordRepository(context)
        val wordList = wordRepo.getAllWords()
        for (word in wordList) {
            word.writingForm?.let { wordRepo.updateFreshnessForWord(it, -1) }
        }
    }
}
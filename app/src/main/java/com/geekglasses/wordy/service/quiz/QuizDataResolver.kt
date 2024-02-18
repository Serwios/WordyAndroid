package com.geekglasses.wordy.service.quiz

import android.os.Parcelable
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.mapper.WordToQuizDataMapper
import com.geekglasses.wordy.model.QuizData
import com.geekglasses.wordy.service.word.WordProcessor

class QuizDataResolver {
    companion object {
        fun resolveQuizData(dbHelper: DataBaseHelper): ArrayList<QuizData> {
            val allWords = dbHelper.getAllWords()
            return ArrayList(
                WordToQuizDataMapper(
                    WordProcessor().getProcessedWords(
                        allWords,
                        allWords.size
                    )
                ).mapToQuizData()
            )
        }
    }
}
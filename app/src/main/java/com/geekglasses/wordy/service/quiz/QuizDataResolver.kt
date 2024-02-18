package com.geekglasses.wordy.service.quiz

import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.mapper.WordsToQuizDataMapper
import com.geekglasses.wordy.model.QuizData
import com.geekglasses.wordy.service.word.WordProcessor

class QuizDataResolver {
    companion object {
        fun resolveQuizData(dbHelper: DataBaseHelper): ArrayList<QuizData> {
            val allWords = dbHelper.getAllWords()
            return ArrayList(
                WordsToQuizDataMapper(
                    WordProcessor().getProcessedWords(
                        allWords,
                        allWords.size
                    )
                ).mapToQuizData()
            )
        }
    }
}
package com.geekglasses.wordy.mapper

import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.QuizData
import com.geekglasses.wordy.service.word.OptionsGenerator.generateOptions

class WordsToQuizDataMapper(private val words: List<Word>) {
    fun mapToQuizData(): List<QuizData> {
        val quizDataList = mutableListOf<QuizData>()

        words.forEach { word ->
            val otherWords = words.filter { it != word }
            val correctTranslation = word.translation
            val options = generateOptions(otherWords.map { it.translation }, correctTranslation)
            val quizData = QuizData(word.writingForm, correctTranslation ?: "", options)
            quizDataList.add(quizData)
        }

        return quizDataList
    }
}

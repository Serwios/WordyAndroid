package com.geekglasses.wordy.mapper

import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.QuizData
import kotlin.random.Random

class WordToQuizDataMapper(private val words: List<Word>) {

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

    private fun generateOptions(allTranslations: List<String?>, correctTranslation: String?): List<String> {
        val options = mutableListOf<String>()
        if (correctTranslation != null) {
            options.add(correctTranslation)
            while (options.size < 3) {
                val randomIndex = Random.nextInt(allTranslations.size)
                val randomTranslation = allTranslations[randomIndex]
                if (randomTranslation != correctTranslation && !options.contains(randomTranslation)) {
                    randomTranslation?.let { options.add(it) }
                }
            }
        }
        options.shuffle()
        return options
    }
}

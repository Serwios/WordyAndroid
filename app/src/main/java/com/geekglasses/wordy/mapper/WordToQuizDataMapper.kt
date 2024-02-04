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
            val remainingTranslations = allTranslations.filter { it != correctTranslation }.toMutableList()
            while (options.size < 3) {
                if (remainingTranslations.isEmpty()) {
                    repeat(3 - options.size) {
                        options.add("-")
                    }
                    break
                }
                val randomIndex = Random.nextInt(remainingTranslations.size)
                val randomTranslation = remainingTranslations[randomIndex]
                if (!options.contains(randomTranslation)) {
                    randomTranslation?.let {
                        options.add(it)
                        remainingTranslations.removeAt(randomIndex)
                    }
                }
            }
        }
        options.shuffle()
        return options
    }
}

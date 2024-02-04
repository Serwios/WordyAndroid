package com.geekglasses.wordy.mapper

import android.os.Parcelable
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.QuizData
import kotlin.random.Random

class WordToQuizDataMapper(private val words: List<Word>) {

    fun mapToQuizData(): ArrayList<out Parcelable> {
        val quizDataList = ArrayList<QuizData>()

        words.forEach { word ->
            val otherWords = words.filter { it != word }
            val correctTranslation = word.translation
            val options = generateOptions(otherWords.map { it.translation }, correctTranslation)
            val quizData = QuizData(word.writingForm, correctTranslation, options)
            quizDataList.add(quizData)
        }

        return quizDataList
    }

    private fun generateOptions(allTranslations: List<String>, correctTranslation: String): List<String> {
        val options = mutableListOf<String>()
        options.add(correctTranslation)
        while (options.size < 3) {
            val randomIndex = Random.nextInt(allTranslations.size)
            val randomTranslation = allTranslations[randomIndex]
            if (randomTranslation != correctTranslation && !options.contains(randomTranslation)) {
                options.add(randomTranslation)
            }
        }
        options.shuffle()
        return options
    }
}

package com.geekglasses.wordy.service.word

import kotlin.random.Random

object OptionsGenerator {
    fun generateOptions(allTranslations: List<String?>, correctTranslation: String?): List<String> {
        val options = mutableListOf<String>()
        if (correctTranslation != null) {
            val remainingTranslations =
                allTranslations.filter { it != correctTranslation }.toMutableList()
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
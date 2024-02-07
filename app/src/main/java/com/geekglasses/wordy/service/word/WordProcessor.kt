package com.geekglasses.wordy.service.word

import com.geekglasses.wordy.entity.Word
import kotlin.math.ceil

/**
 * The WordProcessor class provides methods to process words based on a predefined distribution.
 * The distribution is 32/32/36, where each sector consists of the same sub-distribution.
 * During the inner distribution by struggle, each sector is reduced to REMAINING_PERCENTAGE percentage.
 * This effectively ignores the least struggled 20% from the input for each freshness category.
 */
class WordProcessor {
    private companion object {
        const val MOST_FRESHNESS_PERCENTAGE = 0.32f
        const val MEDIUM_FRESHNESS_PERCENTAGE = 0.32f
        const val LESS_FRESHNESS_PERCENTAGE = 0.36f
        const val MOST_STRUGGLE_PERCENTAGE = 0.32f
        const val MEDIUM_STRUGGLE_PERCENTAGE = 0.32f
        const val LESS_STRUGGLE_PERCENTAGE = 0.36f
        const val REMAINING_PERCENTAGE = 0.8f
    }

    fun getProcessedWords(wordList: List<Word>, initialPick: Int): ArrayList<Word> {
        wordList.sortedBy { it.freshness }

        val mostFreshnessWords = ArrayList<Word>()
        val mediumFreshnessWords = ArrayList<Word>()
        val lessFreshnessWords = ArrayList<Word>()

        distributeWordsByFreshness(
            wordList,
            calculateValueFromPercentage(initialPick, MOST_FRESHNESS_PERCENTAGE),
            calculateValueFromPercentage(initialPick, MEDIUM_FRESHNESS_PERCENTAGE),
            calculateValueFromPercentage(initialPick, LESS_FRESHNESS_PERCENTAGE),
            mostFreshnessWords,
            mediumFreshnessWords,
            lessFreshnessWords
        )

        val result = ArrayList<Word>()
        result.addAll(spreadXWordsByStruggle(mostFreshnessWords, calculateValueFromPercentage(mostFreshnessWords.size, REMAINING_PERCENTAGE)))
        result.addAll(spreadXWordsByStruggle(mediumFreshnessWords, calculateValueFromPercentage(mediumFreshnessWords.size, REMAINING_PERCENTAGE)))
        result.addAll(spreadXWordsByStruggle(lessFreshnessWords, calculateValueFromPercentage(lessFreshnessWords.size, REMAINING_PERCENTAGE)))

        return result
    }

    private fun distributeWordsByFreshness(
        wordList: List<Word>,
        numMostFreshness: Int,
        numMediumFreshness: Int,
        numLessFreshness: Int,
        mostFreshnessWords: MutableList<Word>,
        mediumFreshnessWords: MutableList<Word>,
        lessFreshnessWords: MutableList<Word>
    ) {
        var countMostFreshness = 0
        var countMediumFreshness = 0
        var countLessFreshness = 0

        for (word in wordList) {
            if (countMostFreshness < numMostFreshness) {
                mostFreshnessWords.add(word)
                countMostFreshness++
            } else if (countMediumFreshness < numMediumFreshness) {
                mediumFreshnessWords.add(word)
                countMediumFreshness++
            } else if (countLessFreshness < numLessFreshness) {
                lessFreshnessWords.add(word)
                countLessFreshness++
            } else {
                break
            }
        }
    }

    private fun spreadXWordsByStruggle(wordList: ArrayList<Word>, x: Int): ArrayList<Word> {
        wordList.sortedBy { it.freshness }

        val numMostStruggle = calculateValueFromPercentage(x, MOST_STRUGGLE_PERCENTAGE)
        val numMediumStruggle = calculateValueFromPercentage(x, MEDIUM_STRUGGLE_PERCENTAGE)
        val numLessStruggle = calculateValueFromPercentage(x, LESS_STRUGGLE_PERCENTAGE)

        val mostStruggleWords = ArrayList<Word>()
        val mediumStruggleWords = ArrayList<Word>()
        val lessStruggleWords = ArrayList<Word>()

        distributeWordsByStruggle(
            wordList,
            numMostStruggle,
            numMediumStruggle,
            numLessStruggle,
            mostStruggleWords,
            mediumStruggleWords,
            lessStruggleWords
        )

        val result = ArrayList<Word>()
        result.addAll(mostStruggleWords)
        result.addAll(mediumStruggleWords)
        result.addAll(lessStruggleWords)

        return result
    }

    private fun distributeWordsByStruggle(
        wordList: ArrayList<Word>,
        numMostStruggle: Int,
        numMediumStruggle: Int,
        numLessStruggle: Int,
        mostStruggleWords: MutableList<Word>,
        mediumStruggleWords: MutableList<Word>,
        lessStruggleWords: MutableList<Word>
    ) {
        var countMostStruggle = 0
        var countMediumStruggle = 0
        var countLessStruggle = 0

        for (word in wordList) {
            if (countMostStruggle < numMostStruggle) {
                mostStruggleWords.add(word)
                countMostStruggle++
            } else if (countMediumStruggle < numMediumStruggle) {
                mediumStruggleWords.add(word)
                countMediumStruggle++
            } else if (countLessStruggle < numLessStruggle) {
                lessStruggleWords.add(word)
                countLessStruggle++
            } else {
                break
            }
        }
    }

    private fun calculateValueFromPercentage(value: Int, percentage: Float): Int {
        return ceil(value * percentage).toInt()
    }
}

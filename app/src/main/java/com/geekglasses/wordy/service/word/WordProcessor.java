package com.geekglasses.wordy.service.word;

import com.geekglasses.wordy.entity.Word;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The WordProcessor class provides methods to process words based on a predefined distribution.
 * The distribution is 32/32/36, where each sector consists of the same sub-distribution.
 * During the inner distribution by struggle, each sector is reduced to 80% percentage.
 * This effectively ignores the least struggled 20% from the input for each freshness category.
 * The inner ignorance of 20% ensures the same total ignorance across all sectors.
 */
public class WordProcessor {
    private static final float MOST_FRESHNESS_PERCENTAGE = 0.32F;
    private static final float MEDIUM_FRESHNESS_PERCENTAGE = 0.32F;
    private static final float LESS_FRESHNESS_PERCENTAGE = 0.36F;
    private static final float MOST_STRUGGLE_PERCENTAGE = 0.32F;
    private static final float MEDIUM_STRUGGLE_PERCENTAGE = 0.32F;
    private static final float LESS_STRUGGLE_PERCENTAGE = 0.36F;
    private static final float REMAINING_PERCENTAGE = 0.8F;
    
    public List<Word> getProcessedWords(List<Word> wordList, int initialPick) {
        wordList.sort(Comparator.comparingInt(Word::getFreshness));

        List<Word> mostFreshnessWords = new ArrayList<>();
        List<Word> mediumFreshnessWords = new ArrayList<>();
        List<Word> lessFreshnessWords = new ArrayList<>();

        distributeWordsByFreshness(wordList,
                calculateValueFromPercentage(initialPick, MOST_FRESHNESS_PERCENTAGE),
                calculateValueFromPercentage(initialPick, MEDIUM_FRESHNESS_PERCENTAGE),
                calculateValueFromPercentage(initialPick, LESS_FRESHNESS_PERCENTAGE),
                mostFreshnessWords,
                mediumFreshnessWords,
                lessFreshnessWords);

        List<Word> result = new ArrayList<>();
        result.addAll(spreadXWordsByStruggle(mostFreshnessWords, calculateValueFromPercentage(mostFreshnessWords.size(), REMAINING_PERCENTAGE)));
        result.addAll(spreadXWordsByStruggle(mediumFreshnessWords, calculateValueFromPercentage(mediumFreshnessWords.size(), REMAINING_PERCENTAGE)));
        result.addAll(spreadXWordsByStruggle(lessFreshnessWords, calculateValueFromPercentage(lessFreshnessWords.size(), REMAINING_PERCENTAGE)));

        return result;
    }

    private void distributeWordsByFreshness(List<Word> wordList, int numMostFreshness, int numMediumFreshness, int numLessFreshness,
                                            List<Word> mostFreshnessWords, List<Word> mediumFreshnessWords, List<Word> lessFreshnessWords) {
        int countMostFreshness = 0;
        int countMediumFreshness = 0;
        int countLessFreshness = 0;

        for (Word word : wordList) {
            if (countMostFreshness < numMostFreshness) {
                mostFreshnessWords.add(word);
                countMostFreshness++;
            } else if (countMediumFreshness < numMediumFreshness) {
                mediumFreshnessWords.add(word);
                countMediumFreshness++;
            } else if (countLessFreshness < numLessFreshness) {
                lessFreshnessWords.add(word);
                countLessFreshness++;
            } else {
                break;
            }
        }
    }

    private List<Word> spreadXWordsByStruggle(List<Word> wordList, int x) {
        wordList.sort(Comparator.comparingInt(Word::getFreshness));

        int numMostStruggle = calculateValueFromPercentage(x, MOST_STRUGGLE_PERCENTAGE);
        int numMediumStruggle = calculateValueFromPercentage(x, MEDIUM_STRUGGLE_PERCENTAGE);
        int numLessStruggle = calculateValueFromPercentage(x, LESS_STRUGGLE_PERCENTAGE);

        List<Word> mostStruggleWords = new ArrayList<>();
        List<Word> mediumStruggleWords = new ArrayList<>();
        List<Word> lessStruggleWords = new ArrayList<>();

        distributeWordsByStruggle(wordList, numMostStruggle, numMediumStruggle, numLessStruggle, mostStruggleWords, mediumStruggleWords, lessStruggleWords);

        List<Word> result = new ArrayList<>();
        result.addAll(mostStruggleWords);
        result.addAll(mediumStruggleWords);
        result.addAll(lessStruggleWords);

        return result;
    }

    private void distributeWordsByStruggle(List<Word> wordList, int numMostStruggle, int numMediumStruggle, int numLessStruggle,
                                           List<Word> mostStruggleWords, List<Word> mediumStruggleWords, List<Word> lessStruggleWords) {
        int countMostStruggle = 0;
        int countMediumStruggle = 0;
        int countLessStruggle = 0;

        for (Word word : wordList) {
            if (countMostStruggle < numMostStruggle) {
                mostStruggleWords.add(word);
                countMostStruggle++;
            } else if (countMediumStruggle < numMediumStruggle) {
                mediumStruggleWords.add(word);
                countMediumStruggle++;
            } else if (countLessStruggle < numLessStruggle) {
                lessStruggleWords.add(word);
                countLessStruggle++;
            } else {
                break;
            }
        }
    }

    private int calculateValueFromPercentage(int value, float percentage) {
        return (int) Math.ceil(value * percentage);
    }
}

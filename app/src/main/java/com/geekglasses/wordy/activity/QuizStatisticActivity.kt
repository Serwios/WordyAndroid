package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.model.QuizStatisticData
import com.geekglasses.wordy.db.WordRepository

class QuizStatisticActivity : AppCompatActivity() {
    private lateinit var wordRepository: WordRepository

    private lateinit var okButton: Button
    private lateinit var numberOfWords: TextView
    private lateinit var numberOfGuessedWords: TextView
    private lateinit var timeSpendValue: TextView
    private lateinit var mostStruggledWordTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_stat)

        wordRepository = WordRepository(this)
        initViews()
        displayQuizStat()
        setButtonClickListeners()
    }

    private fun initViews() {
        okButton = findViewById(R.id.okButton)
        numberOfWords = findViewById(R.id.numberOfWords)
        numberOfGuessedWords = findViewById(R.id.numberOfGuessedWords)
        timeSpendValue = findViewById(R.id.timeSpendOnQuiz)
        mostStruggledWordTextView = findViewById(R.id.mostStruggledWord)
    }

    private fun displayQuizStat() {
        val quizStatisticData = intent.getParcelableExtra<QuizStatisticData>(QUIZ_STAT_DATA_EXTRA)
        numberOfWords.text = "Words #${quizStatisticData?.numberOfWords}"
        numberOfGuessedWords.text = "Guessed #${quizStatisticData?.numberOfGuessedWords}"
        val timeSpentInMillis = quizStatisticData?.timeSpentOnQuiz ?: 0L
        val timeSpentInSeconds = timeSpentInMillis / 1000
        timeSpendValue.text = "Time spent: $timeSpentInSeconds seconds"

        val mostStruggledWord = getMostStruggledWord()
        mostStruggledWord?.let {
            mostStruggledWordTextView.text = "Most struggled word now: $it"
        }
    }

    private fun getMostStruggledWord(): String? {
        return wordRepository.getMostStruggledWord()
    }

    private fun setButtonClickListeners() {
        okButton.setOnClickListener { finish() }
    }

    companion object {
        val QUIZ_STAT_DATA_EXTRA = "quizStatData"

        fun createIntent(
            context: Context,
            correctGuesses: Int,
            totalQuizzes: Int,
            timeSpentOnQuiz: Long
        ): Intent {
            return Intent(context, QuizStatisticActivity::class.java).apply {
                putExtra(
                    QUIZ_STAT_DATA_EXTRA,
                    QuizStatisticData(correctGuesses, totalQuizzes, timeSpentOnQuiz)
                )
            }
        }
    }
}

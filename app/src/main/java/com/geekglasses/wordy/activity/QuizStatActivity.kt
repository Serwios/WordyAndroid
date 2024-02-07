package com.geekglasses.wordy.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.model.QuizResultingData

class QuizStatActivity : AppCompatActivity() {
    private lateinit var okButton: Button
    private lateinit var numberOfWords: TextView
    private lateinit var numberOfGuessedWords: TextView
    private lateinit var timeSpendValue: TextView
    private lateinit var mostStruggledWordTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_stat)

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
        val quizResultingData = intent.getParcelableExtra<QuizResultingData>("quizStatData")
        numberOfWords.text = "Words #${quizResultingData?.numberOfWords}"
        numberOfGuessedWords.text = "Guessed #${quizResultingData?.numberOfGuessedWords}"
        val timeSpentInMillis = quizResultingData?.timeSpentOnQuiz ?: 0L
        val timeSpentInSeconds = timeSpentInMillis / 1000
        timeSpendValue.text = "Time spent: $timeSpentInSeconds seconds"

        val mostStruggledWord = getMostStruggledWord()
        mostStruggledWord?.let {
            mostStruggledWordTextView.text = "Most struggled word now: $it"
        }
    }

    private fun getMostStruggledWord(): String? {
        val dbHelper = DataBaseHelper(this)
        return dbHelper.getMostStruggledWord()
    }

    private fun setButtonClickListeners() {
        okButton.setOnClickListener { finish() }
    }
}

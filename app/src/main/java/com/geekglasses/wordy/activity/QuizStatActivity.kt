package com.geekglasses.wordy.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.model.QuizStatData

class QuizStatActivity : AppCompatActivity() {
    private lateinit var okButton: Button
    private lateinit var numberOfWords: TextView
    private lateinit var numberOfGuessedWords: TextView

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
    }

    private fun displayQuizStat() {
        val quizStatData = intent.getParcelableExtra<QuizStatData>("quizStatData")
        numberOfWords.text = "Words #${quizStatData?.numberOfWords}"
        numberOfGuessedWords.text = "Guessed #${quizStatData?.numberOfGuessedWords}"
    }

    private fun setButtonClickListeners() {
        okButton.setOnClickListener { finish() }
    }
}

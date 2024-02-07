package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.model.QuizData
import com.geekglasses.wordy.model.QuizStatData

class QuizActivity : AppCompatActivity() {
    private lateinit var wordCounterText: TextView
    private lateinit var correctGuessCounter: TextView
    private lateinit var guessedWordText: TextView
    private lateinit var wordButtons: List<Button>
    private var currentQuizIndex = 0
    private val totalQuizzes = 3
    private var correctGuesses = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initViews()
        setUpInitialTexts()
        setUpButtonClickListeners()

        startGame(intent.getParcelableArrayListExtra("quizDataList"))
    }

    private fun initViews() {
        with(findViewById<TextView>(R.id.numberWordText)) { wordCounterText = this }
        with(findViewById<TextView>(R.id.correctGuessCounter)) { correctGuessCounter = this }
        with(findViewById<TextView>(R.id.guessedWordText)) { guessedWordText = this }
        wordButtons = listOf(
            findViewById(R.id.word1Button),
            findViewById(R.id.word2Button),
            findViewById(R.id.word3Button),
            findViewById(R.id.word4Button)
        )
    }

    private fun setUpInitialTexts() {
        wordCounterText.text = "${currentQuizIndex + 1}"
        correctGuessCounter.text = "0"
    }

    private fun startGame(quizDataList: ArrayList<QuizData>?) {
        quizDataList?.takeIf { it.isNotEmpty() }?.let { quizData ->
            val data = quizData.removeAt(0)
            with(intent) {
                putExtra("currentWord", data?.correctWord)
                putExtra("correctTranslation", data?.correctWord)
                putParcelableArrayListExtra("quizDataList", quizData)
            }
            setUpOptionsRandomly(
                data?.correctWord.orEmpty(),
                data?.correctTranslation.orEmpty(),
                data?.options.orEmpty().take(3)
            )
        }
    }

    private fun setUpOptionsRandomly(
        wordToGuess: String,
        correctTranslation: String,
        incorrectOptions: List<String>
    ) {
        val options = (incorrectOptions + correctTranslation).shuffled()
        guessedWordText.text = wordToGuess
        wordButtons.forEachIndexed { index, button -> button.text = options[index] }
    }

    private fun loadQuiz(index: Int) {
        if (index < totalQuizzes) {
            wordCounterText.text = "${index + 1}"
            correctGuessCounter.text = correctGuesses.toString()
            startGame(intent.getParcelableArrayListExtra("quizDataList"))
        } else {
            startActivity(createQuizStatIntent(correctGuesses, totalQuizzes))
            finish()
        }
    }

    private fun setUpButtonClickListeners() {
        wordButtons.forEach { button ->
            button.setOnClickListener { checkAnswer(button.text.toString()) }
        }
    }

    private fun checkAnswer(selectedOption: String) {
        val correctWord = intent.getStringExtra("currentWord")
        val dbHelper = DataBaseHelper(this)
        val struggleValue = if (selectedOption != intent.getStringExtra("correctTranslation")) 1 else -1

        correctGuesses += (-1 * struggleValue)
        correctWord?.let { dbHelper.updateStruggleForWord(it, struggleValue) }
        correctWord?.let { dbHelper.updateFreshnessForWord(it, 1) }

        if (++currentQuizIndex < totalQuizzes) loadQuiz(currentQuizIndex)
        else {
            startActivity(createQuizStatIntent(correctGuesses, totalQuizzes))
            finish()
        }
    }

    private fun createQuizStatIntent(correctGuesses: Int, totalQuizzes: Int): Intent =
        Intent(this, QuizStatActivity::class.java).apply {
            putExtra("quizStatData", QuizStatData(correctGuesses, totalQuizzes))
        }

    companion object {
        fun createIntent(context: Context, quizDataList: ArrayList<Parcelable>): Intent =
            Intent(context, QuizActivity::class.java).apply {
                putParcelableArrayListExtra("quizDataList", quizDataList)
            }
    }
}

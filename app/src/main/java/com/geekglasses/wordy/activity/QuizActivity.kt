package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.model.QuizData
import com.geekglasses.wordy.model.QuizStatData

class QuizActivity : AppCompatActivity() {
    private lateinit var wordCounterText: TextView
    private lateinit var correctGuessCounter: TextView

    private lateinit var guessedWordText: TextView
    private lateinit var word1Button: Button
    private lateinit var word2Button: Button
    private lateinit var word3Button: Button
    private lateinit var word4Button: Button

    private var currentQuizIndex = 0
    private val totalQuizzes = 3
    private var correctGuesses = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        wordCounterText = findViewById(R.id.numberWordText)
        correctGuessCounter = findViewById(R.id.correctGuessCounter)

        guessedWordText = findViewById(R.id.guessedWordText)
        word1Button = findViewById(R.id.word1Button)
        word2Button = findViewById(R.id.word2Button)
        word3Button = findViewById(R.id.word3Button)
        word4Button = findViewById(R.id.word4Button)

        wordCounterText.text = (currentQuizIndex + 1).toString()
        correctGuessCounter.text = "0"

        val parcelableExtra = intent.getParcelableArrayListExtra<QuizData>("quizDataList")
        val quizDataList = parcelableExtra as? ArrayList<QuizData>
        setUpButtonClickListeners()
        startGame(quizDataList)
    }

    private fun startGame(quizDataList: ArrayList<QuizData>?) {
        if (!quizDataList.isNullOrEmpty()) {
            val quizData = quizDataList.removeAt(0)
            val incorrectOptions = quizData?.options.orEmpty()
            intent.putExtra("correctTranslation", quizData?.correctTranslation)
            setUpOptionsRandomly(
                quizData?.correctWord.orEmpty(),
                quizData?.correctTranslation.orEmpty(),
                incorrectOptions.getOrNull(0).orEmpty(),
                incorrectOptions.getOrNull(1).orEmpty(),
                incorrectOptions.getOrNull(2).orEmpty()
            )

            intent.putParcelableArrayListExtra("quizDataList", quizDataList)
        }
    }

    private fun setUpOptionsRandomly(wordToGuess: String, correctTranslation: String, incorrectOptionWord1: String, incorrectOptionWord2: String, incorrectOptionWord3: String) {
        val options = listOf(correctTranslation, incorrectOptionWord1, incorrectOptionWord2, incorrectOptionWord3)
        val shuffledOptions = options.shuffled()

        guessedWordText.text = wordToGuess;

        word1Button.text = shuffledOptions[0]
        word2Button.text = shuffledOptions[1]
        word3Button.text = shuffledOptions[2]
        word4Button.text = shuffledOptions[3]
    }

    private fun loadQuiz(index: Int) {
        if (index < totalQuizzes) {
            wordCounterText.text = (index + 1).toString()
            startGame(intent.getParcelableArrayListExtra<QuizData>("quizDataList") as? ArrayList<QuizData>)
        } else {
            val intent = Intent(this, QuizStatActivity::class.java)
            intent.putExtra("quizStatData", QuizStatData(correctGuesses, index))
            startActivity(intent)
            finish()
        }
    }

    private fun setUpButtonClickListeners() {
        word1Button.setOnClickListener { checkAnswer(word1Button.text.toString()) }
        word2Button.setOnClickListener { checkAnswer(word2Button.text.toString()) }
        word3Button.setOnClickListener { checkAnswer(word3Button.text.toString()) }
        word4Button.setOnClickListener { checkAnswer(word4Button.text.toString()) }
    }

    private fun checkAnswer(selectedOption: String) {
        if (selectedOption == intent.getStringExtra("correctTranslation")) {
            correctGuesses += 1
            correctGuessCounter.text = correctGuesses.toString()
        }

        currentQuizIndex++
        loadQuiz(currentQuizIndex)
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, QuizActivity::class.java)
        }
    }
}

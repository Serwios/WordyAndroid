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
import com.geekglasses.wordy.model.QuizResultingData
import kotlin.properties.Delegates

class QuizActivity : AppCompatActivity() {
    private lateinit var wordCounterText: TextView
    private lateinit var correctGuessCounter: TextView
    private lateinit var guessedWordText: TextView
    private lateinit var wordButtons: List<Button>
    private var currentQuizIndex = 0

    private var totalQuizzes by Delegates.notNull<Int>()

    private var correctGuesses = 0
    private var quizStartTime = 0L
    private var quizEndTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        initViews()
        setUpInitialTexts()
        setUpButtonClickListeners()

        quizStartTime = System.currentTimeMillis()
        totalQuizzes = intent.getIntExtra(TOTAL_QUIZZES_EXTRA, TOTAL_QUIZZES_DEFAULT_SIZE)

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
                putExtra("correctTranslation", data?.correctTranslation)
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
        println()
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
            quizEndTime = System.currentTimeMillis()
            val timeSpentOnQuiz = quizEndTime - quizStartTime
            startActivity(createQuizStatIntent(correctGuesses, totalQuizzes, timeSpentOnQuiz))
            finish()
        }
    }

    private fun setUpButtonClickListeners() {
        wordButtons.forEach { button ->
            button.setOnClickListener { checkAnswer(button.text.toString()) }
        }
    }

    private fun checkAnswer(selectedOption: String) {
        val isCorrect = selectedOption == intent.getStringExtra("correctTranslation")

        if (isCorrect) {
            correctGuesses++
        }

        intent.getStringExtra("currentWord")?.let {
            DataBaseHelper(this).updateStruggleForWord(it, if (isCorrect) -1 else 1)
            DataBaseHelper(this).updateFreshnessForWord(it, 1)
        }

        if (++currentQuizIndex < totalQuizzes) {
            loadQuiz(currentQuizIndex)
        } else {
            quizEndTime = System.currentTimeMillis()
            val timeSpentOnQuiz = quizEndTime - quizStartTime
            startActivity(createQuizStatIntent(correctGuesses, totalQuizzes, timeSpentOnQuiz))
            finish()
        }
    }


    private fun createQuizStatIntent(correctGuesses: Int, totalQuizzes: Int, timeSpentOnQuiz: Long): Intent {
        val QUIZ_STAT_DATA_EXTRA = "quizStatData"
        return Intent(this, QuizStatActivity::class.java).apply {
            putExtra(QUIZ_STAT_DATA_EXTRA, QuizResultingData(correctGuesses, totalQuizzes, timeSpentOnQuiz))
        }
    }

    companion object {
        val QUIZ_DATA_LIST_EXTRA = "quizDataList"
        val TOTAL_QUIZZES_EXTRA = "totalQuizzes"
        val TOTAL_QUIZZES_DEFAULT_SIZE = 3

        fun createIntent(context: Context, quizDataList: ArrayList<Parcelable>, totalQuizzes: Int): Intent {
            return Intent(context, QuizActivity::class.java).apply {
                putParcelableArrayListExtra(QUIZ_DATA_LIST_EXTRA, quizDataList)
                putExtra(TOTAL_QUIZZES_EXTRA, totalQuizzes)
            }
        }
    }
}

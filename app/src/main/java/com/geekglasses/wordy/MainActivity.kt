package com.geekglasses.wordy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.mapper.WordToQuizDataMapper
import com.geekglasses.wordy.service.WordProcessor

class MainActivity : AppCompatActivity() {
    private lateinit var wordEditText: EditText
    private lateinit var translationEditText: EditText
    private lateinit var saveWordButton: Button
    private lateinit var quizButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wordEditText = findViewById(R.id.wordEditText)
        translationEditText = findViewById(R.id.translationEditText)
        saveWordButton = findViewById(R.id.saveWordButton)
        quizButton = findViewById(R.id.quizButton)

        saveWordButton.setOnClickListener {
            wordEditText.text.clear()
            translationEditText.text.clear()

            DataBaseHelper(this).addOne(
                Word(
                    wordEditText.text.toString(),
                    translationEditText.text.toString(),
                    Constants.MINIMAL_STRUGGLE,
                    Constants.MAXIMAL_FRESHNESS
                )
            )
        }

        quizButton.setOnClickListener {
            val quizActivity = Intent(this, QuizActivity::class.java)
            val resolveAllWords = DataBaseHelper(this).resolveAllWords()

            quizActivity.putParcelableArrayListExtra(
                "quizDataList", WordToQuizDataMapper(
                    WordProcessor().getProcessedWords(
                        resolveAllWords,
                        resolveAllWords.size
                    )
                ).mapToQuizData()
            )

            startActivity(quizActivity)
        }
    }
}
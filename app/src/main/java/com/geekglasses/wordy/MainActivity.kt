package com.geekglasses.wordy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.QuizData
import java.io.Serializable
import java.util.LinkedList
import java.util.Queue

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
            val writingForm = wordEditText.text.toString()
            val translation = translationEditText.text.toString()

            wordEditText.text.clear()
            translationEditText.text.clear()

            val dataBaseHelper = DataBaseHelper(this)
            val result = dataBaseHelper.addOne(
                Word(
                    writingForm,
                    translation,
                    Constants.MINIMAL_STRUGGLE,
                    Constants.MAXIMAL_FRESHNESS
                )
            )

            println(result)
        }

        quizButton.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            val quizDataList: ArrayList<QuizData> = arrayListOf(
                QuizData("Question 1", "Correct1", listOf("Option 1", "Option 3", "Option 2")),
                QuizData("Question 2", "Correct1", listOf("Option 1", "Option 3", "Option 2")),
                QuizData("Question 3", "Correct1", listOf("Option 1", "Option 2", "Option 3"))
            )

            intent.putParcelableArrayListExtra("quizDataList", quizDataList)
            startActivity(intent)
        }
    }
}
package com.geekglasses.wordy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
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
    private lateinit var optionsMenu: View

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wordEditText = findViewById(R.id.wordEditText)
        translationEditText = findViewById(R.id.translationEditText)
        saveWordButton = findViewById(R.id.saveWordButton)
        quizButton = findViewById(R.id.quizButton)
        optionsMenu = findViewById(R.id.optionsMenu)

        wordEditText.setOnTouchListener { _, _ ->
            wordEditText.text.clear()
            false
        }

        translationEditText.setOnTouchListener { _, _ ->
            translationEditText.text.clear()
            false
        }

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

            hideKeyboard()
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

        optionsMenu.setOnClickListener {
            showPopupMenu(optionsMenu)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(wordEditText.windowToken, 0)
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.example_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_dictionary -> {
                    // clear dictionary
                    true
                }
                R.id.show_dictionary -> {
                    showToast("Item 2 selected")
                    true
                }
                else -> {
                    showToast("Item 2 selected")
                    false
                }
            }
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.example_menu, menu)
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
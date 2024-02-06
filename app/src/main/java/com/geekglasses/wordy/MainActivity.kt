package com.geekglasses.wordy

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.activity.WordListActivity
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.service.notification.NotificationScheduler
import com.geekglasses.wordy.service.quiz.QuizDataResolver.Companion.resolveQuizData
import com.geekglasses.wordy.validator.WordValidator.isWordExist
import com.geekglasses.wordy.validator.WordValidator.isWordValid
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var wordEditText: EditText
    private lateinit var translationEditText: EditText
    private lateinit var saveWordButton: Button
    private lateinit var quizButton: Button
    private lateinit var optionsMenu: View

    private var dbHelper = DataBaseHelper(this)

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
            if (isWordValid(wordEditText.text.toString()) && isWordValid(translationEditText.text.toString())) {
                if (isWordExist(wordEditText.text.toString(), dbHelper)) {
                    showToast("Input word already exist")
                    return@setOnClickListener;
                }

                dbHelper.addOne(
                    Word(
                        wordEditText.text.toString(),
                        translationEditText.text.toString(),
                        Constants.MINIMAL_STRUGGLE,
                        Constants.MAXIMAL_FRESHNESS
                    )
                )

                hideKeyboard()
                wordEditText.text.clear()
                translationEditText.text.clear()
            } else {
                showToast("Input words invalid")
            }
        }

        quizButton.setOnClickListener {
            startActivity(QuizActivity.createIntent(this, resolveQuizData(dbHelper)))
        }

        optionsMenu.setOnClickListener {
            showPopupMenu(optionsMenu)
        }

        val workRequest = PeriodicWorkRequestBuilder<NotificationScheduler>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "QuizNotificationWork",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun hideKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(wordEditText.windowToken, 0)
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.example_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.clear_dictionary -> {
                    dbHelper.clearWordTable()
                    showToast("Cleared dictionary")
                    true
                }

                R.id.show_dictionary -> {
                    val wordListActivityIntent = Intent(this, WordListActivity::class.java)
                    val arrayList = ArrayList(dbHelper.allWords)
                    wordListActivityIntent.putExtra("wordList", arrayList)
                    startActivity(wordListActivityIntent)
                    true
                }

                else -> {
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
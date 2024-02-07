package com.geekglasses.wordy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MotionEvent
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
import com.geekglasses.wordy.service.quiz.QuizDataResolver.Companion.resolveQuizData
import com.geekglasses.wordy.service.scheduler.freshness.FreshnessUpdateCheckScheduler
import com.geekglasses.wordy.service.scheduler.notification.NotificationScheduler
import com.geekglasses.wordy.validator.WordValidator.isWordExist
import com.geekglasses.wordy.validator.WordValidator.isWordValid
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var wordEditText: EditText
    private lateinit var translationEditText: EditText
    private lateinit var saveWordButton: Button
    private lateinit var quizButton: Button
    private lateinit var optionsMenu: View

    companion object {
        const val INITIAL_STRUGGLE = 0;
        const val INITIAL_FRESHNESS = 0;
    }

    private val dbHelper by lazy { DataBaseHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()

        scheduleTasks()
    }

    private fun initViews() {
        wordEditText = findViewById(R.id.wordEditText)
        translationEditText = findViewById(R.id.translationEditText)
        saveWordButton = findViewById(R.id.saveWordButton)
        quizButton = findViewById(R.id.quizButton)
        optionsMenu = findViewById(R.id.optionsMenu)
    }

    private fun setupListeners() {
        wordEditText.clearOnTouchListener()
        translationEditText.clearOnTouchListener()

        saveWordButton.setOnClickListener {
            if (isWordValid(wordEditText.text.toString()) && isWordValid(translationEditText.text.toString())) {
                if (isWordExist(wordEditText.text.toString(), dbHelper)) {
                    showToast("Input word already exists")
                    return@setOnClickListener
                }

                saveWord()
                clearFields()
            } else {
                showToast("Input words invalid")
            }
        }

        quizButton.setOnClickListener {
            startActivity(QuizActivity.createIntent(this, resolveQuizData(dbHelper)))
        }

        optionsMenu.setOnClickListener {
            showPopupMenu()
        }
    }

    private fun scheduleTasks() {
        FreshnessUpdateCheckScheduler(this).scheduleFreshnessUpdate()
        scheduleQuizRequestNotification()
    }

    private fun saveWord() {
        dbHelper.addOne(
            Word(
                wordEditText.text.toString(),
                translationEditText.text.toString(),
                INITIAL_STRUGGLE,
                INITIAL_FRESHNESS
            )
        )
        hideKeyboard()
    }

    private fun clearFields() {
        wordEditText.text.clear()
        translationEditText.text.clear()
    }

    private fun View.clearOnTouchListener() {
        setOnTouchListener(fun(_: View, _: MotionEvent): Boolean {
            (this as EditText).text.clear()
            return false
        })
    }

    private fun scheduleQuizRequestNotification() {
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

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this, optionsMenu)
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
                    val arrayList = ArrayList(dbHelper.getAllWords())
                    wordListActivityIntent.putExtra("wordList", arrayList)
                    startActivity(wordListActivityIntent)
                    true
                }
                else -> false
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
package com.geekglasses.wordy

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.activity.QuizActivity.Companion.TOTAL_QUIZZES_DEFAULT_SIZE
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
    private lateinit var dictionarySpinner: Spinner
//    private lateinit var dictionarySize: TextView

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
        dictionarySpinner = findViewById(R.id.dictionary_spinner)

        val spinnerData = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 1", "Option 2", "Option 3", "Option 4", "Option 5", "Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerData)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        dictionarySpinner.adapter = adapter

//        dictionarySize = findViewById(R.id.dictionary_size)
//        dictionarySize.text = "Dictionary size: ${dbHelper.getAllWords().size}"
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

        optionsMenu.setOnClickListener {
            showPopupMenu()
        }

        quizButton.setOnClickListener {
            val quizSize = getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                .getInt("quizSize", TOTAL_QUIZZES_DEFAULT_SIZE)

            val numberOfWordsInDb = dbHelper.getAllWords().size

            if (quizSize > numberOfWordsInDb) {
                showToast("There are not enough words for a quiz of size $quizSize")
            } else {
                startActivity(
                    QuizActivity.createIntent(
                        this, resolveQuizData(dbHelper), quizSize
                    )
                )
            }
        }
    }

    private fun scheduleTasks() {
        FreshnessUpdateCheckScheduler(this).scheduleFreshnessUpdate()
        scheduleQuizRequestNotification()
    }

    private fun saveWord() {
        dbHelper.addOneWord(
            Word(
                wordEditText.text.toString(),
                translationEditText.text.toString(),
                INITIAL_STRUGGLE,
                INITIAL_FRESHNESS
            )
        )
        hideKeyboard()

//        "Dictionary size: ${(dictionarySize.text.split(" ")[2].toInt() + 1)}".also { dictionarySize.text = it }
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
                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage("Are you sure you want to clear the dictionary?")
                        .setCancelable(false)
                        .setPositiveButton("Yes") { _, _ ->
                            dbHelper.clearWordTable()
                            showToast("Cleared dictionary")
//                            dictionarySize.text = "Dictionary size: 0"
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.cancel()
                        }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    true
                }

                R.id.show_dictionary -> {
                    startActivity(WordListActivity.createIntent(this, ArrayList(dbHelper.getAllWords())))
                    true
                }

                R.id.set_quiz_size -> {
                    showSetQuizSizeDialog()
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

    private fun showSetQuizSizeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set quiz size")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val inputValue = input.text.toString()
            if (inputValue.isNotEmpty()) {
                val quizSize = inputValue.toInt()
                if (quizSize > 0) {
                    if (quizSize > dbHelper.getAllWords().size) {
                        showToast("There are not enough words for a quiz of size $quizSize")
                        showSetQuizSizeDialog()
                        return@setPositiveButton
                    }

                    getSharedPreferences("user_pref", Context.MODE_PRIVATE)
                        .edit()
                        .putInt("quizSize", quizSize)
                        .apply()
                } else {
                    showToast("Please, enter a valid size")
                    showSetQuizSizeDialog()
                    return@setPositiveButton
                }
            } else {
                showToast("Please, enter a valid size")
                showSetQuizSizeDialog()
                return@setPositiveButton
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val INITIAL_STRUGGLE = 0;
        const val INITIAL_FRESHNESS = 0;
    }
}
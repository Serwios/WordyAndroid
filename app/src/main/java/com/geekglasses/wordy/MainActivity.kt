package com.geekglasses.wordy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
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
    private lateinit var dictionarySize: TextView
    private lateinit var dictionaryName: TextView

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

        dictionarySize = findViewById(R.id.dictionary_size)
        dictionaryName = findViewById(R.id.dictionary_name)

        dictionarySize.text = "Size: ${dbHelper.getWordsForCurrentPickedDictionary()?.size}"
        dictionaryName.text = "Name: ${dbHelper.getCurrentPickedDictionary()}"
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
        dbHelper.addOneWordForPickedDictionary(
            Word(
                wordEditText.text.toString(),
                translationEditText.text.toString(),
                INITIAL_STRUGGLE,
                INITIAL_FRESHNESS
            )
        )
        hideKeyboard()

        "Dictionary size: ${(dictionarySize.text.split(" ")[1].toInt() + 1)}".also { dictionarySize.text = it }
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
                            dbHelper.clearAllWordsForPickedDictionary()
                            showToast("Cleared dictionary")
                            dictionarySize.text = "Size: 0"
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.cancel()
                        }

                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                    true
                }

                R.id.show_dictionary -> {
                    startActivity(dbHelper.getWordsForCurrentPickedDictionary()?.let { ArrayList(it) }
                        ?.let {
                            WordListActivity.createIntent(this,
                                it
                            )
                        })
                    true
                }

                R.id.set_quiz_size -> {
                    showSetQuizSizeDialog()
                    true
                }

                R.id.dictionaries -> {

                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.example_menu, menu)

        val dictionaryItem: MenuItem? = menu.findItem(R.id.menu_dictionary)
        if (dictionaryItem != null) {
            val pickedDictionaryName = dbHelper.getCurrentPickedDictionary()
            dictionaryItem.title = pickedDictionaryName ?: "Dictionary"
        }

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
                    if (quizSize > dbHelper.getWordsForCurrentPickedDictionary()?.size!!) {
                        showToast("There are not enough words for a quiz of size $quizSize")
                        showSetQuizSizeDialog()
                        return@setPositiveButton
                    }

                    // TODO: Make quiz size different for different dictionary
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
        const val INITIAL_STRUGGLE = 0
        const val INITIAL_FRESHNESS = 0

        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {}
        }
    }
}
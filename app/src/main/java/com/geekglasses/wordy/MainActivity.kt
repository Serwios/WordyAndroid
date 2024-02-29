package com.geekglasses.wordy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geekglasses.wordy.activity.DictionariesManagementActivity
import com.geekglasses.wordy.activity.QuizActivity
import com.geekglasses.wordy.activity.QuizActivity.Companion.TOTAL_QUIZZES_DEFAULT_SIZE
import com.geekglasses.wordy.activity.WordsManagementActivity
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.db.DictionaryRepository
import com.geekglasses.wordy.db.WordRepository
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
    private lateinit var dictionarySize: TextView
    private lateinit var dictionaryName: TextView

    private val wordRepo by lazy { WordRepository(this) }
    private val dictionaryRepo by lazy { DictionaryRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        wordEditText = findViewById(R.id.wordEditText)
        translationEditText = findViewById(R.id.translationEditText)
        saveWordButton = findViewById(R.id.saveWordButton)
        quizButton = findViewById(R.id.quizButton)
        optionsMenu = findViewById(R.id.optionsMenu)

        dictionarySize = findViewById(R.id.dictionary_size)
        dictionaryName = findViewById(R.id.dictionary_name)

        dictionarySize.text = "Size: ${dictionaryRepo.getWordsForCurrentPickedDictionary()?.size}"
        dictionaryName.text = "Name: ${dictionaryRepo.getCurrentPickedDictionary()}"
    }

    private fun setupListeners() {
        saveWordButton.setOnClickListener {
            if (isWordValid(wordEditText.text.toString()) && isWordValid(translationEditText.text.toString())) {
                if (isWordExist(wordEditText.text.toString(), wordRepo)) {
                    showToast("Input word already exists")
                    return@setOnClickListener
                }

                saveWord()
                clearInputTexts()
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

            val numberOfWordsInDb = wordRepo.getAllWords().size

            if (quizSize > numberOfWordsInDb) {
                showToast("There are not enough words for a quiz of size $quizSize")
            } else {
                startActivity(
                    QuizActivity.createIntent(
                        this, resolveQuizData(wordRepo), quizSize
                    )
                )
            }
        }
    }

    private fun saveWord() {
        dictionaryRepo.addOneWordForPickedDictionary(
            Word(
                wordEditText.text.toString(),
                translationEditText.text.toString(),
                INITIAL_STRUGGLE,
                INITIAL_FRESHNESS
            )
        )
        hideKeyboard()

        "Size: ${(dictionarySize.text.split(" ")[1].toInt() + 1)}".also { dictionarySize.text = it }
    }

    private fun clearInputTexts() {
        wordEditText.text.clear()
        translationEditText.text.clear()
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
                            dictionaryRepo.clearAllWordsForPickedDictionary()
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
                    startActivity(dictionaryRepo.getWordsForCurrentPickedDictionary()?.let { ArrayList(it) }
                        ?.let {
                            WordsManagementActivity.createIntent(this,
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
                    startActivity(DictionariesManagementActivity.createIntent(this))
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
            val pickedDictionaryName = dictionaryRepo.getCurrentPickedDictionary()
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
                    if (quizSize > dictionaryRepo.getWordsForCurrentPickedDictionary()?.size!!) {
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
        const val INITIAL_STRUGGLE = 0
        const val INITIAL_FRESHNESS = 0

        fun createIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {}
        }
    }
}
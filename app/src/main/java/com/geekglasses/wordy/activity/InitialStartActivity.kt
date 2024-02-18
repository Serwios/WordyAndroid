package com.geekglasses.wordy.activity

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.MainActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DictionaryRepository


class InitialStartActivity : AppCompatActivity() {
    private lateinit var dictionaryRepo: DictionaryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        dictionaryRepo = DictionaryRepository(this)
        if (dictionaryRepo.getCurrentPickedDictionary() != null) {
            navigateToMainActivity()
        }

        val editTextDictionaryName = findViewById<EditText>(R.id.editTextDictionaryName)
        val buttonEnter = findViewById<Button>(R.id.buttonEnter)

        editTextDictionaryName.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                dictionaryRepo.addInitialDictionary(editTextDictionaryName.text.toString())
                navigateToMainActivity()

                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        buttonEnter.setOnClickListener {
            dictionaryRepo.addInitialDictionary(editTextDictionaryName.text.toString())
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.createIntent(this))
        finish()
    }
}
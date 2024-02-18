package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.geekglasses.wordy.MainActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DictionaryRepository
import com.geekglasses.wordy.validator.DictionaryValidator

class DictionariesManagementActivity : AppCompatActivity() {
    private lateinit var dictionaryRepo: DictionaryRepository
    private lateinit var dictionaryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_dictionaries)

        dictionaryRepo = DictionaryRepository(this)
        dictionaryContainer = findViewById(R.id.dictionaryContainer)

        findViewById<Button>(R.id.backButtonDictionaries).setOnClickListener {
            navigateToMainActivity()
        }

        findViewById<Button>(R.id.addDictionaryButton).setOnClickListener {
            showAddDictionaryDialog()
        }

        populateDictionaryTable()
    }

    private fun populateDictionaryTable() {
        val dictionaries = dictionaryRepo.getAllDictionaries()
        val currentPickedDictionary = dictionaryRepo.getCurrentPickedDictionary()

        for (dictionary in dictionaries) {
            val container = LinearLayout(this)
            container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            container.orientation = LinearLayout.HORIZONTAL

            val pickDictionaryButton = Button(this)
            pickDictionaryButton.text = dictionary.name
            pickDictionaryButton.setOnClickListener {
                handleDictionarySelection(dictionary.name)
            }

            val buttonParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2f)
            container.addView(pickDictionaryButton, buttonParams)

            val spaceView = Space(this)
            val spaceParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            container.addView(spaceView, spaceParams)

            val dictionarySizeTextView = TextView(this)
            dictionarySizeTextView.text = dictionaryRepo.getDictionarySize(dictionary.name).toString()
            dictionarySizeTextView.gravity = Gravity.CENTER
            val sizeParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            sizeParams.setMargins(resources.getDimensionPixelSize(R.dimen.dictionary_buttons_margin), 0, resources.getDimensionPixelSize(R.dimen.dictionary_buttons_margin), 0)
            container.addView(dictionarySizeTextView, sizeParams)

            val deleteButton = Button(this)
            deleteButton.text = "X"
            deleteButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            deleteButton.setOnClickListener {
                if (dictionary.name != currentPickedDictionary) {
                    dictionaryRepo.deleteDictionary(dictionary.name)
                    refreshDictionaryButtons()
                } else {
                    Toast.makeText(this, "Cannot delete the currently picked dictionary", Toast.LENGTH_SHORT).show()
                }
            }

            val deleteParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            deleteParams.setMargins(0, 0, 0, 0)
            container.addView(deleteButton, deleteParams)

            dictionaryContainer.addView(container)
        }
    }

    private fun handleDictionarySelection(dictionaryName: String) {
        if (dictionaryRepo.pickDictionary(dictionaryName)) {
            Toast.makeText(this, "Picked dictionary: $dictionaryName", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Failed to pick dictionary", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDictionaryButtons() {
        dictionaryContainer.removeAllViews()
        populateDictionaryTable()
    }

    private fun showAddDictionaryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Dictionary")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val dictionaryName = input.text.toString()
            if (dictionaryName.isNotBlank()) {
                addNewDictionary(dictionaryName)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addNewDictionary(dictionaryName: String) {
        if (!isDictionaryNameValid(dictionaryName)) {
            return
        }

        if (dictionaryRepo.addDictionary(dictionaryName)) {
            refreshDictionaryButtons()
        } else {
            Toast.makeText(this, "Failed to add dictionary", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDictionaryNameValid(dictionaryName: String): Boolean {
        if (DictionaryValidator.isDictionaryExist(dictionaryName, dictionaryRepo)) {
            Toast.makeText(this, "Dictionary already exist in your repo", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!DictionaryValidator.isDictionaryValid(dictionaryName)) {
            Toast.makeText(this, "Dictionary name is invalid", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.createIntent(this))
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, DictionariesManagementActivity::class.java)
        }
    }
}
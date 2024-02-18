package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.MainActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper

class AllDictionariesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var scrollView: ScrollView
    private lateinit var dictionaryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_dictionaries)

        dbHelper = DataBaseHelper(this)
        scrollView = findViewById(R.id.scrollView)
        dictionaryContainer = findViewById(R.id.dictionaryContainer)

        val backButton: Button = findViewById(R.id.backButtonDictionaries)
        backButton.setOnClickListener {
            startActivity(MainActivity.createIntent(this))
        }

        val addDictionaryButton: Button = findViewById(R.id.addDictionaryButton)
        addDictionaryButton.setOnClickListener {
            showAddDictionaryDialog()
        }

        populateDictionaryButtons()
    }

    private fun populateDictionaryButtons() {
        val dictionaries = dbHelper.getAllDictionaries()
        val currentPickedDictionary = dbHelper.getCurrentPickedDictionary()

        for (dictionary in dictionaries) {
            val container = RelativeLayout(this)
            container.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val dictionaryButton = Button(this)
            dictionaryButton.text = dictionary.name
            dictionaryButton.setOnClickListener {
                handleDictionarySelection(dictionary.name)
            }
            val buttonParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            container.addView(dictionaryButton, buttonParams)

            val deleteButton = Button(this)
            deleteButton.text = "X"
            deleteButton.setOnClickListener {
                if (dictionary.name != currentPickedDictionary) {
                    dbHelper.deleteDictionary(dictionary.name)
                    refreshDictionaryButtons()
                } else {
                    Toast.makeText(this, "Cannot delete the currently picked dictionary", Toast.LENGTH_SHORT).show()
                }
            }
            val deleteParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            deleteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            container.addView(deleteButton, deleteParams)

            val dictionarySizeTextView = TextView(this)
            dictionarySizeTextView.text = dbHelper.getDictionarySize(dictionary.name).toString()
            val sizeParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            sizeParams.topMargin = resources.getDimensionPixelSize(R.dimen.text_margin_top)
            sizeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
            container.addView(dictionarySizeTextView, sizeParams)

            dictionaryContainer.addView(container)
        }
    }
    private fun handleDictionarySelection(dictionaryName: String) {
        if (dbHelper.pickDictionary(dictionaryName)) {
            Toast.makeText(this, "Picked dictionary: $dictionaryName", Toast.LENGTH_SHORT).show()
            refreshDictionaryButtons()
        } else {
            Toast.makeText(this, "Failed to pick dictionary", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDictionaryButtons() {
        dictionaryContainer.removeAllViews()
        populateDictionaryButtons()
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
        if (dbHelper.addDictionary(dictionaryName)) {
            refreshDictionaryButtons()
        } else {
            Toast.makeText(this, "Failed to add dictionary", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AllDictionariesActivity::class.java).apply {}
        }
    }
}

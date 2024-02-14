package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper

class AllDictionariesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper
    private lateinit var tableLayout: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_dictionaries)

        dbHelper = DataBaseHelper(this)
        tableLayout = findViewById(R.id.tableLayout)

        val backButton: Button = findViewById(R.id.backButtonDictionaries)
        backButton.setOnClickListener {
            onBackPressed()
        }

        val addDictionaryButton: Button = findViewById(R.id.addDictionaryButton)
        addDictionaryButton.setOnClickListener {
            showAddDictionaryDialog()
        }

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
        if (dbHelper.addDictionary(dictionaryName)) {
            refreshDictionaryTable()
        } else {
            Toast.makeText(this, "Failed to add dictionary", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshDictionaryTable() {
        tableLayout.removeAllViews()
        populateDictionaryTable()
    }

    private fun populateDictionaryTable() {
        val dictionaries = dbHelper.getAllDictionaries()

        for (dictionary in dictionaries) {
            val row = TableRow(this)
            val params = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
            row.layoutParams = params

            val dictionaryNameTextView = TextView(this)
            dictionaryNameTextView.text = dictionary.name
            row.addView(dictionaryNameTextView)

            val dictionarySizeTextView = TextView(this)
            dictionarySizeTextView.text = dbHelper.getDictionarySize(dictionary.name).toString()
            row.addView(dictionarySizeTextView)

            val deleteButton = Button(this)
            deleteButton.text = "X"
            deleteButton.setOnClickListener {
                dbHelper.deleteDictionary(dictionary.name)
                refreshDictionaryTable()
            }
            row.addView(deleteButton)

            tableLayout.addView(row)
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, AllDictionariesActivity::class.java).apply {}
        }
    }
}

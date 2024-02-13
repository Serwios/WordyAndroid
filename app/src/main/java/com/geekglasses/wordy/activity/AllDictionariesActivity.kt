package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.geekglasses.wordy.MainActivity
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper

class AllDictionariesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_dictionaries)

        dbHelper = DataBaseHelper(this)

        val backButton: Button = findViewById(R.id.backButtonDictionaries)
        backButton.setOnClickListener {
            onBackPressed()
        }

        populateDictionaryTable()
    }

    private fun populateDictionaryTable() {
        val dictionaries = dbHelper.getAllDictionaries()
        val tableLayout = findViewById<TableLayout>(R.id.tableLayout)

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
                tableLayout.removeView(row)
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

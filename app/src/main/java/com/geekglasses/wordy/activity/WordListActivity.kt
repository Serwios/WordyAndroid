package com.geekglasses.wordy.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geekglasses.wordy.R
import com.geekglasses.wordy.db.DataBaseHelper
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.WordAdapter

class WordListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private lateinit var dbHelper: DataBaseHelper
    private var wordList: List<Word> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        initViews()
        setUpRecyclerView()
        setUpButtonBack()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        dbHelper = DataBaseHelper(this)
        wordList = intent.getParcelableArrayListExtra("wordList") ?: emptyList()
    }

    private fun setUpRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WordAdapter(wordList.toMutableList())
        recyclerView.adapter = adapter
    }

    private fun setUpButtonBack() {
        findViewById<Button>(R.id.buttonBack).setOnClickListener { finish() }
    }

    fun onDeleteButtonClick(view: View) {
        val parentLayout = view.parent as RelativeLayout
        val textViewWritingForm = parentLayout.findViewById<TextView>(R.id.textViewWritingForm)
        val wordToDelete = textViewWritingForm.text.toString().substring(6)

        if (dbHelper.deleteWordByWritingForm(wordToDelete)) {
            Toast.makeText(this, "Successfully deleted", Toast.LENGTH_SHORT).show()
        }

        wordList = dbHelper.getAllWords()
        adapter.updateList(wordList)
    }
}

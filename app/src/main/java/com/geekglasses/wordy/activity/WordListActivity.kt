package com.geekglasses.wordy.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geekglasses.wordy.MainActivity
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
        wordList = intent.getParcelableArrayListExtra(WORD_LIST_EXTRA) ?: emptyList()
    }

    private fun setUpRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WordAdapter(wordList.toMutableList())
        recyclerView.adapter = adapter
    }

    private fun setUpButtonBack() {
        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
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

    companion object {
        val WORD_LIST_EXTRA = "wordList"

        fun createIntent(
            context: Context,
            words: ArrayList<Word>
        ): Intent {
            return Intent(context, WordListActivity::class.java).apply {
                putExtra(WORD_LIST_EXTRA, words)
            }
        }
    }
}

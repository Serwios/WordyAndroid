package com.geekglasses.wordy.activity

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geekglasses.wordy.R
import com.geekglasses.wordy.entity.Word
import com.geekglasses.wordy.model.WordAdapter

class WordListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WordAdapter
    private var wordList: List<Word> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_list)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        wordList = intent.getParcelableArrayListExtra("wordList") ?: emptyList()

        adapter = WordAdapter(wordList)
        recyclerView.adapter = adapter

        val buttonBack: Button = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            finish()
        }
    }
}

package com.geekglasses.wordy.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.geekglasses.wordy.R
import com.geekglasses.wordy.entity.Word

class WordAdapter(private val wordList: MutableList<Word>) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_word_item, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val word = wordList[position]
        holder.bind(word)
    }

    override fun getItemCount(): Int = wordList.size

    fun updateList(newWordList: List<Word>) {
        wordList.clear()
        wordList.addAll(newWordList)
        notifyDataSetChanged()
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewWritingForm: TextView = itemView.findViewById(R.id.textViewWritingForm)
        private val textViewTranslation: TextView = itemView.findViewById(R.id.textViewTranslation)
        private val textViewStruggle: TextView = itemView.findViewById(R.id.textViewStruggle)
        private val textViewFreshness: TextView = itemView.findViewById(R.id.textViewFreshness)

        fun bind(word: Word) {
            textViewWritingForm.text = "Word: ${word.writingForm}"
            textViewTranslation.text = "Translation: ${word.translation}"
            textViewStruggle.text = "Struggle point: ${word.struggle}"
            textViewFreshness.text = "Freshness point: ${word.freshness}"
        }
    }
}

package com.geekglasses.wordy.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.geekglasses.wordy.R;
import com.geekglasses.wordy.entity.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<Word> wordList;

    public WordAdapter(List<Word> wordList) {
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.textViewWritingForm.setText(word.getWritingForm());
        holder.textViewTranslation.setText(word.getTranslation());
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWritingForm;
        TextView textViewTranslation;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWritingForm = itemView.findViewById(R.id.textViewWritingForm);
            textViewTranslation = itemView.findViewById(R.id.textViewTranslation);
        }
    }
}
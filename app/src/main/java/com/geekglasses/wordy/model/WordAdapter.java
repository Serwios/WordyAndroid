package com.geekglasses.wordy.model;

import android.annotation.SuppressLint;
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
    private final List<Word> wordList;
    private final String TEXT_VIEW_WRITING_FROM_PREFIX = "Word: ";
    private final String TEXT_VIEW_TRANSLATION_PREFIX = "Translation: ";
    private final String TEXT_VIEW_STRUGGLE_PREFIX = "Struggle point: ";
    private final String TEXT_VIEW_FRESHNESS_PREFIX = "Freshness point: ";


    public WordAdapter(List<Word> wordList) {
        this.wordList = wordList;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.textViewWritingForm.setText(String.format("%s%s", TEXT_VIEW_WRITING_FROM_PREFIX, word.getWritingForm()));
        holder.textViewTranslation.setText(String.format("%s%s", TEXT_VIEW_TRANSLATION_PREFIX, word.getTranslation()));
        holder.textViewStruggle.setText(String.format("%s%d", TEXT_VIEW_STRUGGLE_PREFIX, word.getStruggle()));
        holder.textViewFreshness.setText(String.format("%s%d", TEXT_VIEW_FRESHNESS_PREFIX, word.getFreshness()));
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWritingForm;
        TextView textViewTranslation;
        TextView textViewStruggle;
        TextView textViewFreshness;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWritingForm = itemView.findViewById(R.id.textViewWritingForm);
            textViewTranslation = itemView.findViewById(R.id.textViewTranslation);
            textViewStruggle = itemView.findViewById(R.id.textViewStruggle);
            textViewFreshness = itemView.findViewById(R.id.textViewFreshness);
        }
    }

    public void updateList(List<Word> newWordList) {
        wordList.clear();
        wordList.addAll(newWordList);
        notifyDataSetChanged();
    }
}
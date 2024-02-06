package com.geekglasses.wordy.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.geekglasses.wordy.entity.Word;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String WORD_TABLE = "WORD_TABLE";

    private static final String COLUMN_WRITING_FORM = "WRITING_FORM";
    private static final String COLUMN_TRANSLATION = "TRANSLATION";
    private static final String COLUMN_STRUGGLE = "STRUGGLE";
    private static final String COLUMN_FRESHNESS = "FRESHNESS";

    public DataBaseHelper(@Nullable Context context) {
        super(context, "wordyDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + WORD_TABLE + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_WRITING_FORM + " TEXT, "
                + COLUMN_TRANSLATION + " TEXT, "
                + COLUMN_STRUGGLE + " INT, "
                + COLUMN_FRESHNESS + " INT)";
        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean addOne(Word word) {
        try {
            ContentValues cv = new ContentValues();

            cv.put(COLUMN_WRITING_FORM, word.getWritingForm());
            cv.put(COLUMN_TRANSLATION, word.getTranslation());
            cv.put(COLUMN_STRUGGLE, word.getStruggle());
            cv.put(COLUMN_FRESHNESS, word.getFreshness());

            return this.getWritableDatabase().insert(WORD_TABLE, null, cv) > 0;
        } catch (Exception e) {
            System.out.println("Failed to add data to DB, message: " + e.getMessage());
            return false;
        }
    }

    public boolean isWordExists(String word) {
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + WORD_TABLE + " WHERE " + COLUMN_WRITING_FORM + "=?", new String[]{word});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void clearWordTable() {
        this.getWritableDatabase().delete(WORD_TABLE, null, null);
        this.getWritableDatabase().close();
    }

    public List<Word> getAllWords() {
        List<Word> wordList = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM WORD_TABLE", null);
        if (cursor != null) {
            try {
                int idColumnIndex = cursor.getColumnIndex("ID");
                int writingFormColumnIndex = cursor.getColumnIndex("WRITING_FORM");
                int translationColumnIndex = cursor.getColumnIndex("TRANSLATION");
                int struggleColumnIndex = cursor.getColumnIndex("STRUGGLE");
                int freshnessColumnIndex = cursor.getColumnIndex("FRESHNESS");

                while (cursor.moveToNext()) {
                    if (isColumnIndexesValid(idColumnIndex, writingFormColumnIndex, translationColumnIndex, struggleColumnIndex, freshnessColumnIndex)) {
                        Word word = new Word();
                        word.setWritingForm(cursor.getString(writingFormColumnIndex));
                        word.setTranslation(cursor.getString(translationColumnIndex));
                        word.setStruggle(cursor.getInt(struggleColumnIndex));
                        word.setFreshness(cursor.getInt(freshnessColumnIndex));
                        wordList.add(word);
                    } else {
                        System.out.println("Missed column");
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return wordList;
    }

    private static boolean isColumnIndexesValid(int idColumnIndex, int writingFormColumnIndex, int translationColumnIndex, int struggleColumnIndex, int freshnessColumnIndex) {
        return idColumnIndex != -1 && writingFormColumnIndex != -1 &&
                translationColumnIndex != -1 && struggleColumnIndex != -1 &&
                freshnessColumnIndex != -1;
    }

    public boolean deleteWordByWritingForm(String wordToDelete) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(WORD_TABLE, COLUMN_WRITING_FORM + "=?", new String[]{wordToDelete});
        db.close();
        return deletedRows > 0;
    }

    public void updateStruggleForWord(String word, int struggleCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + WORD_TABLE + " SET " + COLUMN_STRUGGLE + " = " + struggleCount + " WHERE " + COLUMN_WRITING_FORM + " = '" + word + "'";
        db.execSQL(query);
        db.close();
    }

    public void updateFreshnessForWord(String word, int freshnessIncrement) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + WORD_TABLE + " SET " + COLUMN_FRESHNESS + " = " + COLUMN_FRESHNESS + " + " + freshnessIncrement + " WHERE " + COLUMN_WRITING_FORM + " = ?";
        db.execSQL(query, new String[]{word});
        db.close();
    }
}

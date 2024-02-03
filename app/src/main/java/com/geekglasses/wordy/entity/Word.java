package com.geekglasses.wordy.entity;

import java.util.Objects;

public class Word {
    private int id;
    private String writingForm;
    private String translation;
    private int struggle;
    private int freshness;

    public Word() {
    }

    public Word(String writingForm, String translation, int struggle, int freshness) {
        this.writingForm = writingForm;
        this.translation = translation;
        this.struggle = struggle;
        this.freshness = freshness;
    }

    public int getId() {
        return id;
    }

    public String getWritingForm() {
        return writingForm;
    }

    public String getTranslation() {
        return translation;
    }

    public int getStruggle() {
        return struggle;
    }

    public int getFreshness() {
        return freshness;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWritingForm(String writingForm) {
        this.writingForm = writingForm;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public void setStruggle(int struggle) {
        this.struggle = struggle;
    }

    public void setFreshness(int freshness) {
        this.freshness = freshness;
    }

    @Override
    public String toString() {
        return "Word{" +
                "writingForm='" + writingForm + '\'' +
                ", translation='" + translation + '\'' +
                ", struggle=" + struggle +
                ", freshness=" + freshness +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return struggle == word.struggle && freshness == word.freshness && Objects.equals(writingForm, word.writingForm) && Objects.equals(translation, word.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(writingForm, translation, struggle, freshness);
    }
}

package com.geekglasses.wordy.model

import android.os.Parcel
import android.os.Parcelable

data class QuizStatData(
    val numberOfGuessedWords: Int,
    val numberOfWords: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(numberOfGuessedWords)
        parcel.writeInt(numberOfWords)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuizStatData> {
        override fun createFromParcel(parcel: Parcel): QuizStatData {
            return QuizStatData(parcel)
        }

        override fun newArray(size: Int): Array<QuizStatData?> {
            return arrayOfNulls(size)
        }
    }
}
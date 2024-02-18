package com.geekglasses.wordy.model

import android.os.Parcel
import android.os.Parcelable

data class QuizStatisticData(
    val numberOfGuessedWords: Int,
    val numberOfWords: Int,
    val timeSpentOnQuiz: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(numberOfGuessedWords)
        parcel.writeInt(numberOfWords)
        parcel.writeLong(timeSpentOnQuiz)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuizStatisticData> {
        override fun createFromParcel(parcel: Parcel): QuizStatisticData {
            return QuizStatisticData(parcel)
        }

        override fun newArray(size: Int): Array<QuizStatisticData?> {
            return arrayOfNulls(size)
        }
    }
}
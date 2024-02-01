package com.geekglasses.wordy.model

import android.os.Parcel
import android.os.Parcelable

data class QuizData(
    val correctWord: String,
    val correctTranslation: String,
    val options: List<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(correctWord)
        parcel.writeString(correctTranslation)
        parcel.writeStringList(options)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuizData> {
        override fun createFromParcel(parcel: Parcel): QuizData {
            return QuizData(parcel)
        }

        override fun newArray(size: Int): Array<QuizData?> {
            return arrayOfNulls(size)
        }
    }
}
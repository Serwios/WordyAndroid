package com.geekglasses.wordy.entity

import android.os.Parcel
import android.os.Parcelable

data class Word(
    var writingForm: String? = null,
    var translation: String? = null,
    var struggle: Int = 0,
    var freshness: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(writingForm)
        parcel.writeString(translation)
        parcel.writeInt(struggle)
        parcel.writeInt(freshness)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Word> {
        override fun createFromParcel(parcel: Parcel): Word {
            return Word(parcel)
        }

        override fun newArray(size: Int): Array<Word?> {
            return arrayOfNulls(size)
        }
    }
}

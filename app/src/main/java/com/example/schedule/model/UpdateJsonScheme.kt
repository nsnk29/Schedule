package com.example.schedule.model

import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder

data class UpdateJsonScheme(
    val link: String?,
    val size: Double?,
    val changelog: ArrayList<VersionLog>?,
    val error: String?
)

data class VersionLog(
    val version: Int,
    val description: String,
    val upload_time: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(version)
        parcel.writeString(description)
        parcel.writeLong(upload_time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VersionLog> {
        override fun createFromParcel(parcel: Parcel): VersionLog {
            return VersionLog(parcel)
        }

        override fun newArray(size: Int): Array<VersionLog?> {
            return arrayOfNulls(size)
        }
    }
}

data class VersionLogSpannable(
    val version: Int,
    val description: SpannableStringBuilder,
    val upload_time: Long
)
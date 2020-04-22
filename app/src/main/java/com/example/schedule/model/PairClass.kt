package com.example.schedule.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class PairClass() : RealmObject(), Parcelable {
    @SerializedName("studyroom")
    var studyroom: String = ""

    @SerializedName("day")
    var day: Int = 0

    @SerializedName("even")
    var even: Int = 0

    @SerializedName("group")
    var group: String = ""

    @SerializedName("lecturer")
    var lecturer: String = ""

    @SerializedName("number")
    var number: Int = 0

    @SerializedName("name")
    var title: String = ""

    @SerializedName("type")
    var type: Int = 0

    constructor(parcel: Parcel) : this() {
        studyroom = parcel.readString() ?: ""
        day = parcel.readInt()
        even = parcel.readInt()
        group = parcel.readString() ?: ""
        lecturer = parcel.readString() ?: ""
        number = parcel.readInt()
        title = parcel.readString() ?: ""
        type = parcel.readInt()
    }

    fun clear() {
        studyroom = ""
        day = 0
        even = 0
        group = ""
        lecturer = ""
        number = 0
        title = ""
        type = 0
    }

    fun setFields(pair: PairClass) {
        studyroom = pair.studyroom
        day = pair.day
        even = pair.even
        group = pair.group
        lecturer = pair.lecturer
        number = pair.number
        title = pair.title
        type = pair.type
    }

    fun isEmpty(): Boolean =
        studyroom.isBlank()
                && day == 0
                && even == 0
                && group.isBlank()
                && lecturer.isBlank()
                && number == 0
                && title.isBlank()
                && type == 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(studyroom)
        parcel.writeInt(day)
        parcel.writeInt(even)
        parcel.writeString(group)
        parcel.writeString(lecturer)
        parcel.writeInt(number)
        parcel.writeString(title)
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PairClass> {
        override fun createFromParcel(parcel: Parcel): PairClass {
            return PairClass(parcel)
        }

        override fun newArray(size: Int): Array<PairClass?> {
            return arrayOfNulls(size)
        }
    }
}
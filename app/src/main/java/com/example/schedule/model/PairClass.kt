package com.example.schedule.model

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class PairClass : RealmObject() {
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
}
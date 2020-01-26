package com.example.schedule.model

import io.realm.RealmObject

open class PairClass : RealmObject(){
    var studyroom: String = ""
    var day: Int = 0
    var even: Int = 0
    var group: String = ""
    var lecturer: String = ""
    var number: Int = 0
    var name: String = ""
    var type: Int = 0

    fun clear(){
        studyroom = ""
        day = 0
        even = 0
        group = ""
        lecturer = ""
        number = 0
        name = ""
        type = 0
    }
}
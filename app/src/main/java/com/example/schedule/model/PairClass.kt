package com.example.schedule.model

import io.realm.RealmObject

open class PairClass : RealmObject(){
    var aud: String? = null
    var day: Int = 0
    var even: String? = null
    var group: String? = null
    var lecturer: String? = null
    var number: Int = 0
    var subjectName: String? = null
}
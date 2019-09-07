package com.example.schedule.model

import io.realm.RealmObject

open class PairClass : RealmObject(){
    var studyroom: String? = null
    var day: Int = 0
    var even: Int = 0
    var group: String? = null
    var lecturer: String? = null
    var number: Int = 0
    var name: String? = null
    var type: Int = 0
}
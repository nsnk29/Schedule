package com.example.schedule.model

import io.realm.RealmList
import io.realm.RealmObject

open class ListOfStringClass: RealmObject(){
    var type: String = ""
    var data: RealmList<String> = RealmList()
}

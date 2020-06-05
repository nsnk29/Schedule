package com.example.schedule.database

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.schedule.R
import com.example.schedule.model.LessonJsonStructure
import com.example.schedule.model.ListOfStringClass
import com.example.schedule.model.PairClass
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.createObject

class DatabaseHelper(val context: Context) {
    private var connection: Realm

    init {
        Realm.init(context)
        val configuration =
            RealmConfiguration.Builder().name("schedule.realm").schemaVersion(1).build()
        Realm.setDefaultConfiguration(configuration)
        connection = Realm.getInstance(configuration)
    }


    fun setVersion(newVersion: Long) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
            putLong("version", newVersion)
            apply()
        }
    }

    fun addInformationToDBFromJSON(lessonJsonStructure: LessonJsonStructure) {
        deleteAllPairsFromBD()
        val groupList: RealmList<String> = RealmList()
        val lecturerList: RealmList<String> = RealmList()

        connection.executeTransaction { connection ->
            for (pair in lessonJsonStructure.pairs) {
                connection.createObject<PairClass>().apply { setFields(pair) }
                groupList.add(pair.group)
                lecturerList.add(pair.lecturer)
            }

            val groupToDB = connection.createObject<ListOfStringClass>()
            groupToDB.data = getRealListFromList(groupList.distinct())
            groupToDB.type = context.getString(R.string.groups)
            val lecturerToDB = connection.createObject<ListOfStringClass>()
            lecturerToDB.data = getRealListFromList(lecturerList.distinct())
            lecturerToDB.type = context.getString(R.string.lecturers)
        }
    }

    private fun getRealListFromList(data: List<String>): RealmList<String> {
        val result: RealmList<String> = RealmList()
        for (str in data) if (str.isNotBlank()) result.add(str)
        return result
    }

    fun getListOfGroupsOrLecturer(type: Int): List<String> {
        val allData =
            connection.where(ListOfStringClass::class.java)
                .equalTo(context.getString(R.string.type_parameter), context.getString(type))
                .findFirst()
        return allData?.data ?: emptyList()
    }


    private fun deleteAllPairsFromBD() {
        connection.executeTransaction { connection ->
            connection.delete(PairClass::class.java)
            connection.delete(ListOfStringClass::class.java)
        }
    }

    fun getPairsOfGroup(group: String, day: Int, even: Int): RealmResults<PairClass> {
        return connection.where(PairClass::class.java)
            .equalTo(context.getString(R.string.group_parameter), group)
            .equalTo(context.getString(R.string.day_parameter), day + 1)
            .notEqualTo(context.getString(R.string.even_parameter), even)
            .findAll()
    }

    fun getPairsOfLecturer(lecturer: String, day: Int, even: Int): RealmResults<PairClass> {
        return connection.where(PairClass::class.java)
            .equalTo(context.getString(R.string.lecturer_parameter), lecturer)
            .equalTo(context.getString(R.string.day_parameter), day + 1)
            .notEqualTo(context.getString(R.string.even_parameter), even)
            .findAll()
    }

    fun closeConnection() {
        if (!connection.isClosed) connection.close()
    }
}
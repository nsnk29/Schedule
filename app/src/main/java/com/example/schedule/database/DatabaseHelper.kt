package com.example.schedule.database

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.schedule.R
import com.example.schedule.model.LessonJSONStructure
import com.example.schedule.model.ListOfStringClass
import com.example.schedule.model.PairClass
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
import io.realm.RealmResults
import io.realm.kotlin.createObject

object DatabaseHelper {
    private lateinit var connection: Realm
    private var versionOfSchedule = 0.toLong()
    private lateinit var mPreference: SharedPreferences
    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
        mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        getConnection()
    }

    fun getConnection(): Realm {
        if (::connection.isInitialized) {
            closeConnection()
        }
        Realm.init(context)
        val configuration =
            RealmConfiguration.Builder().name("schedule.realm").schemaVersion(1).build()
        Realm.setDefaultConfiguration(configuration)
        connection = Realm.getInstance(configuration)
        return connection
    }


    fun setVersion(newVersion: Long) {
        val editor = mPreference.edit()
        editor.putLong("version", newVersion)
        editor.apply()
        versionOfSchedule = newVersion
    }

    fun addInformationToDBFromJSON(json: LessonJSONStructure) {
        deleteAllPairsFromBD()
        val groupList: RealmList<String> = RealmList()
        val lecturerList: RealmList<String> = RealmList()

        connection.executeTransaction { connection ->
            for (pair in json.pairs) {
                val localPair = connection.createObject<PairClass>()
                localPair.studyroom = pair.studyroom
                localPair.day = pair.day
                localPair.even = pair.even
                localPair.group = pair.group
                localPair.lecturer = pair.lecturer
                localPair.number = pair.number
                localPair.title = pair.title
                localPair.type = pair.type
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
        for (str in data) if (str != "") result.add(str)
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
            .equalTo(context.getString(R.string.day_parameter), day)
            .notEqualTo(context.getString(R.string.even_parameter), even)
            .findAll()
    }

    fun getPairsOfLecturer(lecturer: String, day: Int, even: Int): RealmResults<PairClass> {
        return connection.where(PairClass::class.java)
            .equalTo(context.getString(R.string.lecturer_parameter), lecturer)
            .equalTo(context.getString(R.string.day_parameter), day)
            .notEqualTo(context.getString(R.string.even_parameter), even)
            .findAll()
    }

    fun closeConnection() {
        if (!connection.isClosed) connection.close()
    }
}
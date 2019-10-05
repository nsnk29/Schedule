package com.example.schedule.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.schedule.R
import com.example.schedule.adapters.PickerRecycleAdapter
import com.example.schedule.adapters.ViewPageAdapter
import com.example.schedule.fragments.PickerFragment
import com.example.schedule.model.ListOfStringClass
import com.example.schedule.model.MyJSONFile
import com.example.schedule.model.PairClass
import com.example.schedule.model.VersionClass
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmList
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_picker.*
import okhttp3.*
import java.io.IOException
import java.net.URL


class PickerActivity : AppCompatActivity() {

    lateinit var realm: Realm
    lateinit var adapter: ViewPageAdapter
    lateinit var groupsFragment: PickerFragment
    lateinit var lecturersFragment: PickerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        getJSON()
        adapter = ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        groupsFragment = PickerFragment(true)
        lecturersFragment = PickerFragment(false)

        adapter.addFragment(groupsFragment, "Группы")
        adapter.addFragment(lecturersFragment, "Преподаватели")

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(hideKeyBoard())
    }


    private fun hideKeyBoard(): ViewPager.OnPageChangeListener {
        return object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    val imm =
                        viewPager.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewPager.windowToken, 0)

                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
            }
        }
    }

    fun getOnQueryTextListener(adapter: PickerRecycleAdapter): SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.filter.filter(query)
                return false
            }

        }
    }

    private fun getNormalList(list: RealmList<String>): List<String> {
        val result: ArrayList<String> = ArrayList()
        for (row in list) result.add(row)
        return result
    }

    fun getInfo(type: Int): List<String> {
        val allData =
            realm.where(ListOfStringClass::class.java)
                .equalTo("type", getString(type))
                .findFirst()


        return if (allData != null) {
            getNormalList(allData.data)
        } else emptyList()
    }

    fun confirm(isGroup: Boolean, str: String) {
        val mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val mEditor = mPreference.edit()
        mEditor.putBoolean(getString(R.string.isGroupPicked), isGroup)
        mEditor.putString(getString(R.string.savedValueOfUsersPick), str)
        mEditor.apply()
        setResult(Activity.RESULT_OK)

        if (callingActivity == null) {
            startActivity(Intent(this, MainActivity::class.java))
            Toast.makeText(this, "FIRST", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun getJSON() {
        val client = OkHttpClient()
        val url = URL("https://api.npoint.io/51288cb390c242f3e007")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Проблемы подключения к сети",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val builder = GsonBuilder().create()
                val myData = builder.fromJson(body, MyJSONFile::class.java)
                runOnUiThread {
                    realm.executeTransaction { realm ->
                        val v = realm.createObject<VersionClass>()
                        v.version = myData.version
                    }
                    val groupList: RealmList<String> = RealmList()
                    val lecturerList: RealmList<String> = RealmList()

                    realm.executeTransaction { realm ->
                        for (pair in myData.pairs) {
                            val localPair = realm.createObject<PairClass>()
                            localPair.studyroom = pair.studyroom
                            localPair.day = pair.day
                            localPair.even = pair.even
                            localPair.group = pair.group
                            localPair.lecturer = pair.lecturer
                            localPair.number = pair.number
                            localPair.name = pair.name
                            localPair.type = pair.type
                            groupList.add(pair.group)
                            lecturerList.add(pair.lecturer)
                        }
                        val groupToDB = realm.createObject<ListOfStringClass>()
                        groupToDB.data = getUniqueList(groupList.distinct())
                        groupToDB.type = getString(R.string.groups)
                        val lecturerToDB = realm.createObject<ListOfStringClass>()
                        lecturerToDB.data = getUniqueList(lecturerList.distinct())
                        lecturerToDB.type = getString(R.string.lecturers)
                    }
                    setDataVisible()
                }
            }
        })
    }

    private fun getUniqueList(data: List<String>): RealmList<String> {
        val result: RealmList<String> = RealmList()
        for (str in data) result.add(str)
        return result
    }

    private fun setDataVisible() {
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        viewPager?.addOnPageChangeListener(hideKeyBoard())
    }
}

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
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.fragments.PickerFragment
import com.example.schedule.model.MyJSONFile
import com.google.gson.GsonBuilder
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_picker.*
import okhttp3.*
import java.io.IOException
import java.net.URL


class PickerActivity : AppCompatActivity() {

    lateinit var realm: Realm
    private lateinit var adapter: ViewPageAdapter
    private lateinit var groupsFragment: PickerFragment
    private lateinit var lecturersFragment: PickerFragment
    lateinit var database: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        database = DatabaseHelper(applicationContext)
        realm = database.getConnection()

        adapter = ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        groupsFragment = PickerFragment(true)
        lecturersFragment = PickerFragment(false)
        if (callingActivity == null) {
            getJSON()
        }
        adapter.addFragment(groupsFragment, getString(R.string.groups_ru_title))
        adapter.addFragment(lecturersFragment, getString(R.string.lecturer_title_ru))

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


    fun confirm(isGroup: Boolean, str: String) {
        val mPreference = PreferenceManager.getDefaultSharedPreferences(this)
        val mEditor = mPreference.edit()
        mEditor.putBoolean(getString(R.string.isGroupPicked), isGroup)
        mEditor.putString(getString(R.string.savedValueOfUsersPick), str)
        mEditor.apply()
        setResult(Activity.RESULT_OK)

        if (callingActivity == null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    private fun getJSON() {
        val client = OkHttpClient()
        val url = URL(getString(R.string.URL_JSON))

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
                val mJson = builder.fromJson(body, MyJSONFile::class.java)
                runOnUiThread {
                    database.addInformationToDBFromJSON(mJson)
                    database.setVersion(mJson.version)
                    setDataVisible()
                }
            }
        })
    }


    private fun setDataVisible() {
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(hideKeyBoard())
    }

    override fun onDestroy() {
        database.closeConnection()
        super.onDestroy()
    }
}

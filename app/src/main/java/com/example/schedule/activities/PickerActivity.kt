package com.example.schedule.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.example.schedule.R
import com.example.schedule.URLRequests
import com.example.schedule.adapters.PickerRecycleAdapter
import com.example.schedule.adapters.ViewPageAdapter
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.fragments.PickerFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_picker.*


class PickerActivity : AppCompatActivity() {

    lateinit var realm: Realm
    private lateinit var adapter: ViewPageAdapter
    private lateinit var groupsFragment: PickerFragment
    private lateinit var lecturersFragment: PickerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)



        adapter = ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        groupsFragment = PickerFragment(true)
        lecturersFragment = PickerFragment(false)
        if (callingActivity == null) {
            DatabaseHelper.init(this@PickerActivity)
            URLRequests.getJSON(this@PickerActivity)
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


    fun setDataVisible() {
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(hideKeyBoard())
    }
}

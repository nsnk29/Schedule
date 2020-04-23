package com.example.schedule.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import kotlinx.android.synthetic.main.group_picker_fragment.*


class PickerActivity : AppCompatActivity() {

    lateinit var realm: Realm
    private lateinit var viewPageAdapter: ViewPageAdapter
    private lateinit var groupsFragment: PickerFragment
    private lateinit var lecturersFragment: PickerFragment
    private lateinit var onQueryTextListenerGroup: SearchView.OnQueryTextListener
    private lateinit var onQueryTextListenerLecturers: SearchView.OnQueryTextListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        DatabaseHelper.init(this@PickerActivity)
        viewPageAdapter =
            ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        groupsFragment = PickerFragment(true)
        lecturersFragment = PickerFragment(false)
        if (callingActivity == null) {
            URLRequests.getJSON(this@PickerActivity)
        }
        viewPageAdapter.addFragment(groupsFragment, getString(R.string.groups_ru_title))
        viewPageAdapter.addFragment(lecturersFragment, getString(R.string.lecturer_title_ru))

        viewPager.adapter = viewPageAdapter
        tabs.setupWithViewPager(viewPager)
        viewPager?.addOnPageChangeListener(getSearchHelper())
        onQueryTextListenerGroup = getOnQueryTextListener(groupsFragment.pickerRecycleAdapter)
        onQueryTextListenerLecturers =
            getOnQueryTextListener(lecturersFragment.pickerRecycleAdapter)
        main_search_view.setOnQueryTextListener(onQueryTextListenerGroup)
    }


    private fun getSearchHelper(): ViewPager.OnPageChangeListener {
        return object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        main_search_view.setOnQueryTextListener(onQueryTextListenerGroup)
                        groupsFragment.pickerRecycleAdapter.filter.filter(main_search_view.query)
                    }
                    1 -> {
                        main_search_view.setOnQueryTextListener(onQueryTextListenerLecturers)
                        lecturersFragment.pickerRecycleAdapter.filter.filter(main_search_view.query)
                    }
                }
            }
        }
    }

    private fun getOnQueryTextListener(adapter: PickerRecycleAdapter): SearchView.OnQueryTextListener {
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
        groupsFragment.pickerRecycleAdapter.apply {
            dataList = groupsFragment.getRelevantData()
            notifyDataSetChanged()
            filter.filter(main_search_view.query)
        }
        lecturersFragment.pickerRecycleAdapter.apply {
            dataList = lecturersFragment.getRelevantData()
            notifyDataSetChanged()
            filter.filter(main_search_view.query)
        }

        when (tabs.selectedTabPosition) {
            0 -> main_search_view.setOnQueryTextListener(onQueryTextListenerGroup)
            1 -> main_search_view.setOnQueryTextListener(onQueryTextListenerLecturers)
        }

    }

    fun getNewData() {
        URLRequests.getJSON(this, true)
    }

    fun hideLoading() {
        runOnUiThread {
            groupsFragment.refreshLayout.isRefreshing = false
            lecturersFragment.refreshLayout.isRefreshing = false
        }
    }
}

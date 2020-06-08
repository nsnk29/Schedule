package com.example.schedule.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.preference.PreferenceManager
import com.example.schedule.CustomApplication
import com.example.schedule.R
import com.example.schedule.URLRequests
import com.example.schedule.adapters.PickerRecycleAdapter
import com.example.schedule.adapters.ViewPageAdapter
import com.example.schedule.database.DatabaseHelper
import com.example.schedule.interfaces.GetLessonsInterface
import com.example.schedule.model.LessonJsonStructure
import kotlinx.android.synthetic.main.activity_picker.*
import kotlinx.android.synthetic.main.group_picker_fragment.*


class PickerActivity : FragmentActivity(), GetLessonsInterface {

    lateinit var realm: DatabaseHelper
    private lateinit var viewPageAdapter: ViewPageAdapter
    private lateinit var onQueryTextListenerGroup: SearchView.OnQueryTextListener
    private lateinit var onQueryTextListenerLecturers: SearchView.OnQueryTextListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        realm = (applicationContext as CustomApplication).getDatabaseInstance()
        viewPageAdapter =
            ViewPageAdapter(supportFragmentManager, realm)
        viewPager.adapter = viewPageAdapter
        tabs.setupWithViewPager(viewPager)
    }

    override fun onResume() {
        super.onResume()
        if (callingActivity == null) {
            URLRequests.getLessonsJson(this@PickerActivity, this)
        }
    }


    fun setListeners(isGroup: Boolean, adapter: PickerRecycleAdapter) {
        if (isGroup)
            onQueryTextListenerGroup = getOnQueryTextListener(adapter)
        else
            onQueryTextListenerLecturers = getOnQueryTextListener(adapter)
    }


    fun setSearch(pos: Int) {
        main_search_view.setOnQueryTextListener(if (pos == 0) onQueryTextListenerGroup else onQueryTextListenerLecturers)
        viewPageAdapter.getFragment(pos)?.pickerRecycleAdapter?.filter?.filter(
            main_search_view.query
        )
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
        viewPageAdapter.getFragment(0)?.pickerRecycleAdapter?.apply {
            dataList = viewPageAdapter.getFragment(0)?.getRelevantData(realm) ?: emptyList()
            notifyDataSetChanged()
            filter.filter(main_search_view.query)
        }
        viewPageAdapter.getFragment(1)?.pickerRecycleAdapter?.apply {
            dataList = viewPageAdapter.getFragment(1)?.getRelevantData(realm) ?: emptyList()
            notifyDataSetChanged()
            filter.filter(main_search_view.query)
        }
        when (tabs.selectedTabPosition) {
            0 -> main_search_view.setOnQueryTextListener(onQueryTextListenerGroup)
            1 -> main_search_view.setOnQueryTextListener(onQueryTextListenerLecturers)
        }
    }

    fun getNewData() {
        URLRequests.getLessonsJson(this, this, true)
    }

    fun hideLoading() {
        runOnUiThread {
            viewPageAdapter.getFragment(0)?.refreshLayout?.isRefreshing = false
            viewPageAdapter.getFragment(1)?.refreshLayout?.isRefreshing = false
        }
    }

    override fun onLessonsReady(lessonJsonStructure: LessonJsonStructure) {
        runOnUiThread {
            realm.addInformationToDBFromJSON(lessonJsonStructure)
            realm.setVersion(lessonJsonStructure.version!!)
            setDataVisible()
        }
    }
}
package com.example.schedule.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import com.example.schedule.R
import com.example.schedule.adapters.ViewPageAdapter
import com.example.schedule.fragments.GroupPickerFragment
import com.example.schedule.fragments.LectorPickerFragment
import kotlinx.android.synthetic.main.activity_picker.*

class PickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        val adapter = ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        adapter.addFragment(GroupPickerFragment(), "Группы")
        adapter.addFragment(LectorPickerFragment(), "Преподаватели")

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

    }
}

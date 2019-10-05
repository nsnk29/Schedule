package com.example.schedule.activities

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.example.schedule.adapters.ViewPageAdapter
import com.example.schedule.fragments.GroupPickerFragment
import com.example.schedule.fragments.LectorPickerFragment
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_picker.*


class PickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.schedule.R.layout.activity_picker)
        val adapter = ViewPageAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        val realm = Realm.getDefaultInstance()

        adapter.addFragment(GroupPickerFragment(realm), "Группы")
        adapter.addFragment(LectorPickerFragment(), "Преподаватели")

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        viewPager?.addOnPageChangeListener(hideKeyBoard())


    }

    private fun hideKeyBoard(): ViewPager.OnPageChangeListener {
        return object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (viewPager.currentItem == 1) {
                        val imm =
                            viewPager.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(viewPager.windowToken, 0)
                    }
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
}

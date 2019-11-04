package com.example.schedule.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPageAdapter(fm: FragmentManager, behavior: Int) :
    FragmentPagerAdapter(fm, behavior) {

    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val fragmentListTitles: MutableList<String> = ArrayList()

    override fun getCount(): Int {
        return fragmentListTitles.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentListTitles[position]
    }

    fun addFragment(fragment: Fragment, Title: String){
        fragmentList.add(fragment)
        fragmentListTitles.add(Title)

    }


}
package com.example.schedule.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPageAdapter(fm: FragmentManager, behavior: Int) :
    FragmentPagerAdapter(fm, behavior) {

    val fragmentList: MutableList<Fragment> = ArrayList()
    val FragmentListTitles: MutableList<String> = ArrayList()

    override fun getCount(): Int {
        return FragmentListTitles.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return FragmentListTitles[position]
    }

    fun addFragment(fragment: Fragment, Title: String){
        fragmentList.add(fragment)
        FragmentListTitles.add(Title)

    }


}
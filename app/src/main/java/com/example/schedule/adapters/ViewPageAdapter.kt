package com.example.schedule.adapters

import android.util.SparseArray
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.schedule.fragments.PickerFragment

class ViewPageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        private const val NUMBER_OF_FRAGMENTS = 2
    }

    private val mFragments = SparseArray<PickerFragment>()
    override fun getCount(): Int = NUMBER_OF_FRAGMENTS

    override fun getItem(position: Int): PickerFragment =
        if (position == 0) PickerFragment.newInstance(true) else PickerFragment.newInstance(false)


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as PickerFragment
        mFragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        mFragments.remove(position)
        super.destroyItem(container, position, `object`)
    }


    fun getFragment(position: Int): PickerFragment? {
        return if (mFragments.size() == 0) {
            null
        } else mFragments.get(position)
    }

    override fun getPageTitle(position: Int): CharSequence? =
        if (position == 0) "Группы" else "Преподаватели"
}
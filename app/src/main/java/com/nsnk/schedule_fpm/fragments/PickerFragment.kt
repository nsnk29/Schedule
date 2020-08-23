package com.nsnk.schedule_fpm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.nsnk.schedule_fpm.CustomApplication
import com.nsnk.schedule_fpm.R
import com.nsnk.schedule_fpm.activities.PickerActivity
import com.nsnk.schedule_fpm.adapters.PickerRecycleAdapter
import com.nsnk.schedule_fpm.database.DatabaseHelper
import com.nsnk.schedule_fpm.interfaces.OnPickerItemClickListener
import java.util.*


class PickerFragment : Fragment(), OnPickerItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        @JvmStatic
        fun newInstance(isGroup: Boolean, realm: DatabaseHelper): PickerFragment =
            PickerFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isGroup", isGroup)
                }
                initAdapter(realm)
            }
    }

    lateinit var pickerRecycleAdapter: PickerRecycleAdapter
    private var isGroup: Boolean = false
    private lateinit var realm: DatabaseHelper

    fun initAdapter(realm: DatabaseHelper) {
        this.realm = realm
        isGroup = arguments?.getBoolean("isGroup") ?: false
        pickerRecycleAdapter = PickerRecycleAdapter(getRelevantData(this.realm), isGroup, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::pickerRecycleAdapter.isInitialized)
            initAdapter(((activity as PickerActivity).applicationContext as CustomApplication).getDatabaseInstance())
        (activity as PickerActivity).setListeners(isGroup, pickerRecycleAdapter)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.group_picker_fragment, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewID)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(
                view.context,
                RecyclerView.VERTICAL,
                false
            )
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    RecyclerView.VERTICAL
                )
            )
            adapter = pickerRecycleAdapter
        }
        val refreshLayout =
            view.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener(this)
        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as PickerActivity).setSearch(if (isGroup) 0 else 1)
    }


    fun getRelevantData(realm: DatabaseHelper): List<String> {
        val dataArray =
            if (isGroup) realm.getListOfGroupsOrLecturer(R.string.groups)
            else realm.getListOfGroupsOrLecturer(R.string.lecturers)
        return dataArray.sortedBy { it.toLowerCase(Locale("ru", "Russia")) }
    }

    override fun onItemClicked(isGroup: Boolean, name: String) {
        (activity as PickerActivity).confirm(isGroup, name)
    }

    override fun onRefresh() {
        (activity as PickerActivity).getNewData()
    }
}
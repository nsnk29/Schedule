package com.example.schedule.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.activities.PickerActivity
import com.example.schedule.adapters.PickerRecycleAdapter
import kotlinx.android.synthetic.main.group_picker_fragment.view.*


class PickerFragment(var isGroup: Boolean) : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            inflater.inflate(R.layout.group_picker_fragment, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewID)
        rv.layoutManager = LinearLayoutManager(
            view.context,
            RecyclerView.VERTICAL,
            false
        )
        val arrayGroups =
            if (isGroup) (context as PickerActivity).getInfo(R.string.groups)
            else (context as PickerActivity).getInfo(R.string.lecturers)
        val adapter = PickerRecycleAdapter(arrayGroups, isGroup)
        view.search.setOnQueryTextListener(
            (context as PickerActivity).getOnQueryTextListener(
                adapter
            )
        )
        val dividerItemDecoration = DividerItemDecoration(
            rv.context,
            RecyclerView.VERTICAL
        )
        rv.addItemDecoration(dividerItemDecoration)
        rv.adapter = adapter
        return view
    }
}
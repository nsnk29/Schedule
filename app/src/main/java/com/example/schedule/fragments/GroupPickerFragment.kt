package com.example.schedule.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.adapters.PickerRecycleAdapter
import kotlinx.android.synthetic.main.group_picker_fragment.view.*


class GroupPickerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.group_picker_fragment, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewID)
        rv.layoutManager = LinearLayoutManager(
            view.context,
            RecyclerView.VERTICAL,
            false
        )
        val array: ArrayList<String> = ArrayList()
        array.add("46/1")
        array.add("46/2")
        array.add("36/1")
        array.add("36/2")
        val adapter = PickerRecycleAdapter(view.context, array)

        view.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                adapter.filter.filter(query)
                return false
            }

        })

        rv.adapter = adapter
        return view
    }
}
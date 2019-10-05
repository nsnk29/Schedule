package com.example.schedule.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R

import java.util.*
import kotlin.collections.ArrayList


class PickerRecycleAdapter(var context: Context, var array: List<String>) : RecyclerView.Adapter<PickerRecycleAdapter.CaseCell>(), Filterable {

    var arrayFiltred: List<String> = array

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arrayFiltred = if (charString.isEmpty()) {
                    array
                } else {
                    val filteredList = ArrayList<String>()
                    for (row in array) {
                        if (row.toLowerCase(Locale("ru", "Russia")).contains(charString.toLowerCase(Locale("ru", "Russia")))
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arrayFiltred
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                arrayFiltred = filterResults.values as List<String>
                notifyDataSetChanged()
            }
        }
    }


    override fun onBindViewHolder(holder: CaseCell, position: Int) {
        holder.caseCell.text = arrayFiltred[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseCell {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.case_cell, parent, false)
        return CaseCell(view)
    }

    override fun getItemCount(): Int {
        return arrayFiltred.size
    }


    class CaseCell(v: View) : RecyclerView.ViewHolder(v) {
        val caseCell: TextView = itemView.findViewById(R.id.case_cell)

        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_LONG).show()
            }
        }
    }

}


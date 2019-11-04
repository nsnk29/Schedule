package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.activities.PickerActivity
import kotlinx.android.synthetic.main.case_cell.view.*
import java.util.*
import kotlin.collections.ArrayList


class PickerRecycleAdapter(var array: List<String>, private val isGroup: Boolean) :
    RecyclerView.Adapter<PickerRecycleAdapter.CaseCell>(), Filterable {


    var arrayFiltered: List<String> = array

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arrayFiltered = if (charString.isEmpty()) {
                    array
                } else {
                    val filteredList = ArrayList<String>()
                    for (row in array) {
                        if (row.toLowerCase(Locale("ru", "Russia")).startsWith(
                                charString.toLowerCase(
                                    Locale("ru", "Russia")
                                )
                            )
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = arrayFiltered
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                arrayFiltered = filterResults.values as List<String>
                notifyDataSetChanged()
            }
        }
    }


    override fun onBindViewHolder(holder: CaseCell, position: Int) {
        holder.caseCell.text = arrayFiltered[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaseCell {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.case_cell, parent, false)
        return CaseCell(view, isGroup)
    }

    override fun getItemCount(): Int {
        return arrayFiltered.size
    }



    class CaseCell(v: View, isGroup: Boolean) : RecyclerView.ViewHolder(v) {
        val caseCell: TextView = itemView.findViewById(R.id.case_cell)
        private val img: ImageView = itemView.findViewById(R.id.imageView)


        init {
            img.setImageResource(if (isGroup) R.drawable.ic_group else R.drawable.ic_lecturer)
            itemView.setOnClickListener {
                (itemView.context as PickerActivity).confirm(isGroup, itemView.case_cell.text.toString())
            }
        }
    }

}


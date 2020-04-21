package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import kotlinx.android.synthetic.main.group_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class ListItemHolder(itemView: View, isGroup: Boolean) : RecyclerView.ViewHolder(itemView) {
    private val nameTextView: TextView = itemView.name_text_view


    init {
        val drawable = ContextCompat.getDrawable(
            itemView.context,
            if (isGroup) R.drawable.ic_group else R.drawable.ic_lecturer
        )
        nameTextView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    fun bind(name: String, isGroup: Boolean, listener: OnItemClickListener) {
        nameTextView.text = name
        itemView.setOnClickListener { listener.onItemClicked(isGroup, name) }
    }

}

class PickerRecycleAdapter(
    var dataList: List<String>,
    private val isGroup: Boolean,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ListItemHolder>(), Filterable {


    var arrayFiltered: List<String> = dataList

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                arrayFiltered = if (charString.isEmpty()) {
                    dataList
                } else {
                    val filteredList = ArrayList<String>()
                    for (row in dataList) {
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


    override fun onBindViewHolder(holder: ListItemHolder, position: Int) {
        holder.bind(arrayFiltered[position], isGroup, itemClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemHolder =
        ListItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.group_item, parent, false),
            isGroup
        )


    override fun getItemCount(): Int = arrayFiltered.size
}

interface OnItemClickListener {
    fun onItemClicked(isGroup: Boolean, name: String)
}

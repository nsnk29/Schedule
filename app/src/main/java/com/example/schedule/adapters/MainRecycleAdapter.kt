package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R


class MainRecycleAdapter() :
    RecyclerView.Adapter<MainRecycleAdapter.CustomViewHolder2>() {



    override fun getItemCount(): Int {
        return 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder2 {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return CustomViewHolder2(view)
    }


    override fun onBindViewHolder(holder: CustomViewHolder2, position: Int) {


    }

    class CustomViewHolder2(v: View) : RecyclerView.ViewHolder(v) {


        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_LONG).show()
            }
        }
    }
}
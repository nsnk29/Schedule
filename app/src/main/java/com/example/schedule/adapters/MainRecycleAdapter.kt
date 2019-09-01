package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.model.PairClass
import io.realm.Realm
import io.realm.RealmResults


class MainRecycleAdapter(val pairsData: RealmResults<PairClass>) :
    RecyclerView.Adapter<MainRecycleAdapter.CustomViewHolder2>() {



    override fun getItemCount(): Int {
        return pairsData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder2 {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return CustomViewHolder2(view)
    }


    override fun onBindViewHolder(holder: CustomViewHolder2, position: Int) {
        holder.aud.text = pairsData[position]?.aud.toString()
        holder.subject_name.text = pairsData[position]?.subject_name.toString()
        holder.type.text =  getTypeOfPair(pairsData[position]?.type)
        holder.lecturer.text = pairsData[position]?.lecturer.toString()
        holder.pair_time.text = getPairTime(pairsData[position]?.number)

    }

    private fun getTypeOfPair(type: Int?): String{
        return when(type){
            1 -> "Практика"
            0 -> "Лекция"
            else -> "?"
        }
    }

    private fun getPairTime(pos: Int?): String {
        return when(pos) {
            1 -> "08:00\n09:30"
            2 -> "09:40\n11:10"
            3 -> "11:30\n13:00"
            4 -> "13:10\n14:40"
            5 -> "15:00\n16:30"
            6 -> "16:40\n18:10"
            7 -> "18:20\n19:50"
            else -> "00:00\n00:00"
        }
    }

    class CustomViewHolder2(v: View) : RecyclerView.ViewHolder(v) {
        val pair_time: TextView = itemView.findViewById(R.id.pair_time)
        val subject_name: TextView = itemView.findViewById(R.id.subject_name)
        val type: TextView = itemView.findViewById(R.id.type)
        val lecturer: TextView = itemView.findViewById(R.id.lecturer)
        val aud: TextView = itemView.findViewById(R.id.aud)


        init {
            itemView.setOnClickListener {
                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_LONG).show()
            }
        }
    }
}
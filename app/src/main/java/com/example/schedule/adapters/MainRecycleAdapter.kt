package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.model.PairClass
import kotlinx.android.synthetic.main.card_layout.view.*


class MainRecycleAdapter(var pairsData: Array<PairClass>) :
    RecyclerView.Adapter<MainRecycleAdapter.CustomViewHolder2>() {


    override fun getItemCount(): Int {
        return pairsData.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder2 {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return CustomViewHolder2(view)
    }


    override fun onBindViewHolder(holder: CustomViewHolder2, position: Int) {
        if (pairsData[position].name == ""){
            holder.name.visibility = View.GONE
            holder.lecturer.visibility = View.GONE
            holder.studyroom.visibility = View.GONE
            holder.type.visibility = View.GONE
            holder.wrapper.setBackgroundResource(R.color.empty_pairs)
        } else {
            holder.name.visibility = View.VISIBLE
            holder.lecturer.visibility = View.VISIBLE
            holder.studyroom.visibility = View.VISIBLE
            holder.type.visibility = View.VISIBLE

            holder.studyroom.text = pairsData[position].studyroom
            holder.name.text = pairsData[position].name
            when (pairsData[position].type) {
                1 -> {
                    holder.type.text = "Практика"
                    holder.itemView.type.setBackgroundResource(R.drawable.type_of_pair_practice)
                }
                0 -> {
                    holder.type.text = "Лекция"
                    holder.itemView.type.setBackgroundResource(R.drawable.type_of_pair_lecture)
                }
            }
            holder.lecturer.text = pairsData[position].lecturer
            holder.wrapper.setBackgroundResource(0)
        }

        holder.pairTime.text = getPairTime(position + 1)

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
        val pairTime: TextView = itemView.findViewById(R.id.pairTime)
        val name: TextView = itemView.findViewById(R.id.subjectName)
        val type: TextView = itemView.findViewById(R.id.type)
        val lecturer: TextView = itemView.findViewById(R.id.lecturer)
        val studyroom: TextView = itemView.findViewById(R.id.aud)
        val wrapper: ConstraintLayout = itemView.findViewById(R.id.wrapper)



//        init {
//            itemView.setOnClickListener {
//                Toast.makeText(itemView.context, "$layoutPosition", Toast.LENGTH_LONG).show()
//            }
//        }
    }
}
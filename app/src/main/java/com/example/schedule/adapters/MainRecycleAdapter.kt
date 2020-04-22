package com.example.schedule.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.model.PairClass
import kotlinx.android.synthetic.main.card_layout_two_lines.view.*


class MainRecycleAdapter(var pairsData: Array<PairClass>, var context: Context) :
    RecyclerView.Adapter<MainRecycleAdapter.PairViewHolder>() {
    private val lineCount: String?
    var isGroup: Boolean
    private var primaryColor: Int
    private var secondaryColor: Int

    init {
        val mPreference = PreferenceManager.getDefaultSharedPreferences(context)
        lineCount = mPreference.getString("card_layout_preference", "2")
        isGroup = mPreference.getBoolean(context.getString(R.string.isGroupPicked), true)
        primaryColor = ContextCompat.getColor(context, R.color.text_color_primary)
        secondaryColor = ContextCompat.getColor(context, R.color.text_color_secondary)
    }

    override fun getItemCount(): Int {
        return pairsData.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PairViewHolder =
        PairViewHolder(
            when (lineCount) {
                "3" -> LayoutInflater.from(parent.context).inflate(
                    R.layout.card_layout_three_lines,
                    parent,
                    false
                )
                else -> LayoutInflater.from(parent.context).inflate(
                    R.layout.card_layout_two_lines,
                    parent,
                    false
                )
            }
        )


    override fun onBindViewHolder(holder: PairViewHolder, position: Int) {
        holder.setClassTime(getLessonTime(position + 1))
        if (pairsData[position].isEmpty()) {
            holder.setVisibility(View.GONE, secondaryColor)
            return
        }
        holder.setVisibility(View.VISIBLE, primaryColor)
        holder.bind(pairsData[position], isGroup)
    }


    private fun getLessonTime(pos: Int?): String = when (pos) {
        1 -> "08:00\n09:30"
        2 -> "09:40\n11:10"
        3 -> "11:30\n13:00"
        4 -> "13:10\n14:40"
        5 -> "15:00\n16:30"
        6 -> "16:40\n18:10"
        7 -> "18:20\n19:50"
        8 -> "20:00\n21:30"
        else -> "00:00\n00:00"
    }


    class PairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val classTimeTextView: TextView = itemView.classTime
        private val classTitleTextView: TextView = itemView.subjectName
        private val typeTextView: TextView = itemView.type
        private val lecturerTextView: TextView = itemView.lecturer
        private val studyroomTextView: TextView = itemView.aud
        private val wrapper: ConstraintLayout = itemView.wrapper

        private val practiceText: String = itemView.context.getString(R.string.practice)
        private val lectureText: String = itemView.context.getString(R.string.lecture)

        fun setVisibility(visibility: Int, color: Int) {
            lecturerTextView.visibility = visibility
            studyroomTextView.visibility = visibility
            typeTextView.visibility = visibility
            classTitleTextView.visibility = visibility
            classTimeTextView.setTextColor(color)
            if (visibility == View.GONE)
                wrapper.setBackgroundResource(R.color.empty_pairs)
            else
                wrapper.setBackgroundResource(0)
        }

        fun bind(lesson: PairClass, isGroup: Boolean) {
            studyroomTextView.text = lesson.studyroom
            classTitleTextView.text = lesson.title
            when (lesson.type) {
                1 -> {
                    typeTextView.text = practiceText
                    typeTextView.setBackgroundResource(R.drawable.type_of_pair_practice)
                }
                0 -> {
                    typeTextView.text = lectureText
                    typeTextView.setBackgroundResource(R.drawable.type_of_pair_lecture)
                }
            }
            if (isGroup)
                lecturerTextView.text = lesson.lecturer
            else {
                lecturerTextView.text = lesson.group
            }
        }

        fun setClassTime(classTime: String) {
            classTimeTextView.text = classTime
        }
    }
}
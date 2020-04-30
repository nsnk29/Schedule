package com.example.schedule.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.R
import com.example.schedule.model.VersionLogSpannable
import kotlinx.android.synthetic.main.changelog_item.view.*
import java.text.DateFormat.getDateInstance

class UpdateInfoAdapter(
    private val changeLogs: List<VersionLogSpannable>
) :
    RecyclerView.Adapter<UpdateChangelogViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpdateChangelogViewHolder =
        UpdateChangelogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.changelog_item, parent, false)
        )

    override fun getItemCount(): Int = changeLogs.size

    override fun onBindViewHolder(holder: UpdateChangelogViewHolder, position: Int) {
        holder.bind(changeLogs[changeLogs.size - 1 - position])
    }
}


class UpdateChangelogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val updateInfoTextView = itemView.update_info_text_view
    private val changelogTextView = itemView.changelog_text_view

    fun bind(changelog: VersionLogSpannable) {
        val info =
            "Версия: ${changelog.version}\nДата обновления: ${getDateInstance().format(changelog.upload_time * 1000)}"
        updateInfoTextView.text = info
        changelogTextView.text = changelog.description
    }
}
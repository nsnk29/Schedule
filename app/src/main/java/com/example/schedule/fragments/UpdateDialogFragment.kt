package com.example.schedule.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.schedule.ImprovedBulletSpan
import com.example.schedule.R
import com.example.schedule.URLRequests
import com.example.schedule.adapters.UpdateInfoAdapter
import com.example.schedule.model.VersionLog
import com.example.schedule.model.VersionLogSpannable
import kotlinx.android.synthetic.main.update_dialog.view.*


class UpdateDialogFragment : AppCompatDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(sizeInfo: Double, changeLog: ArrayList<VersionLog>): UpdateDialogFragment =
            UpdateDialogFragment().apply {
                arguments = Bundle().apply {
                    putDouble("sizeInfo", sizeInfo)
                    putParcelableArrayList("changeLog", changeLog)
                }
            }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val sizeInfo: Double
        val changeLog: ArrayList<VersionLog>
        if (arguments != null) {
            sizeInfo = requireArguments().getDouble("sizeInfo")
            changeLog =
                requireArguments().getParcelableArrayList<VersionLog>("changeLog") as ArrayList<VersionLog>
        } else {
            sizeInfo = 0.0
            changeLog = ArrayList()
        }

        val builder = AlertDialog.Builder(activity)
        val view = View.inflate(context, R.layout.update_dialog, null).apply {
            with(changelog_recycler) {
                layoutManager = LinearLayoutManager(context)
                adapter = UpdateInfoAdapter(List(changeLog.size) {
                    VersionLogSpannable(
                        changeLog[it].version,
                        getChangelogFromString(changeLog[it].description.split("\n")),
                        changeLog[it].upload_time
                    )
                })
                addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
                setHasFixedSize(true)
            }
            val size = "Размер обновления: $sizeInfo Мб."
            update_size_text_view.text = size
        }
        with(builder) {
            setView(view)
            setTitle(R.string.new_version)
            setIcon(R.drawable.ic_update_theme_color)
            setPositiveButton(R.string.need_to_download) { _, _ ->
                URLRequests.lastDownloadController?.checkStoragePermission()
            }
            setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    private fun getChangelogFromString(changes: List<String>): SpannableStringBuilder {
        val color = activity?.getColor(R.color.practice_color) ?: 0
        val result = SpannableStringBuilder()
        if (changes.size == 1 && changes[0].isBlank())
            return SpannableStringBuilder(SpannableString(getString(R.string.update_empty_changelog)).apply {
                setSpan(
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) BulletSpan(
                        20,
                        color,
                        10
                    ) else ImprovedBulletSpan(20, color, 10),
                    0,
                    getString(R.string.update_empty_changelog).length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            })
        for (i in changes.indices) {
            val spannable = SpannableString(changes[i] + if (i < changes.size - 1) "\n" else "")
            spannable.setSpan(
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) BulletSpan(
                    20,
                    color,
                    10
                ) else ImprovedBulletSpan(20, color, 10),
                0,
                spannable.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )
            result.append(spannable)
        }
        return result
    }
}
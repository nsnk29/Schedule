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
import com.example.schedule.DownloadController
import com.example.schedule.ImprovedBulletSpan
import com.example.schedule.R
import kotlinx.android.synthetic.main.update_dialog.view.*


class UpdateDialogFragment(
    private val info: String,
    private val changeLog: String,
    private val downloadController: DownloadController
) : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val view = View.inflate(context, R.layout.update_dialog, null).apply {
            update_info_text_view.text = info
            update_changelog_text_view.text = getChangelog(
                changeLog.split("\r\n"),
                activity?.getColor(R.color.practice_color) ?: 0
            )
        }
        with(builder) {
            setView(view)
            setTitle(R.string.new_version)
            setIcon(R.drawable.ic_update_theme_color)
            setPositiveButton(R.string.need_to_download) { _, _ ->
                downloadController.checkStoragePermission()
            }
            setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
        }
        return builder.create()
    }

    private fun getChangelog(changes: List<String>, color: Int): SpannableStringBuilder {
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
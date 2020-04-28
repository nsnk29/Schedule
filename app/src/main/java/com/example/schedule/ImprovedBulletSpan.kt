package com.example.schedule

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class ImprovedBulletSpan(
    private val gapWidth: Int = STANDARD_GAP_WIDTH,
    val color: Int = STANDARD_COLOR,
    private val bulletRadius: Int = STANDARD_BULLET_RADIUS
) : LeadingMarginSpan {

    companion object {
        private const val STANDARD_BULLET_RADIUS = 4
        private const val STANDARD_GAP_WIDTH = 2
        private const val STANDARD_COLOR = 0
    }


    override fun getLeadingMargin(first: Boolean): Int {
        return 2 * bulletRadius + gapWidth
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean,
        layout: Layout?
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            val oldColor = paint.color
            paint.color = color
            paint.style = Paint.Style.FILL
            val yPosition = (top + bottom) / 2f
            val xPosition = (x + dir * bulletRadius).toFloat()
            canvas.drawCircle(xPosition, yPosition, bulletRadius.toFloat(), paint)
            paint.color = oldColor
            paint.style = style
        }
    }
}
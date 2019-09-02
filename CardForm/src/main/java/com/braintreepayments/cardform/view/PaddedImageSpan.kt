package com.braintreepayments.cardform.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan

import com.braintreepayments.cardform.utils.ViewUtils

class PaddedImageSpan(context: Context, val resourceId: Int) : ImageSpan(context, resourceId) {

    var isDisabled: Boolean = false
    private val mPadding: Int = ViewUtils.dp2px(context, 8f)

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int,
                         fm: Paint.FontMetricsInt?): Int =
        super.getSize(paint, text, start, end, fm) + 2 * mPadding

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int,
                      y: Int, bottom: Int, paint: Paint) {
        super.draw(canvas, text, start, end, x + mPadding, top, y, bottom, paint)
    }

    override fun getDrawable(): Drawable {
        val drawable = super.getDrawable()

        if (isDisabled) {
            drawable.mutate().alpha = 80
        }

        return drawable
    }
}

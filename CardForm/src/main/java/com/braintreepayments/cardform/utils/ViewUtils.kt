package com.braintreepayments.cardform.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import com.braintreepayments.cardform.R
import kotlin.math.roundToInt

object ViewUtils {

    fun dp2px(context: Context, dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
            context.resources.displayMetrics).roundToInt()

    fun isDarkBackground(activity: Activity): Boolean {
        var color = activity.resources.getColor(R.color.bt_white)
        try {
            activity.window.decorView.rootView.background
                ?.takeIf { it is ColorDrawable }
                ?.let { it as ColorDrawable }
                ?.let { color = it.color }

        } catch (ignored: Exception) {}

        val luminance = 0.2126 * Color.red(color) + 0.7152 * Color.green(color) +
            0.0722 * Color.blue(color)

        return luminance < 128
    }
}

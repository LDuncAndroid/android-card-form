package com.braintreepayments.cardform.utils

import android.app.Activity
import android.util.TypedValue

object ColorUtils {

    fun getColor(activity: Activity, attr: String, fallbackColor: Int): Int {
        val color = TypedValue()
        try {
            val colorId = activity.resources.getIdentifier(attr, "attr", activity.packageName)
            if (activity.theme.resolveAttribute(colorId, color, true)) {
                return color.data
            }
        } catch (ignored: Exception) {}

        return activity.resources.getColor(fallbackColor)
    }
}

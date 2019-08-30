package com.braintreepayments.cardform.utils

import android.app.Activity

import com.braintreepayments.cardform.R

enum class ExpirationDateDialogTheme(
    private val mItemTextColor: Int,
    private val mItemInverseTextColor: Int,
    private val mItemDisabledTextColor: Int) {

    LIGHT(R.color.bt_black_87, R.color.bt_white_87, R.color.bt_black_38),
    DARK(R.color.bt_white_87, R.color.bt_black_87, R.color.bt_white_38);

    var itemTextColor: Int = 0
        private set
    var itemInvertedTextColor: Int = 0
        private set
    var itemDisabledTextColor: Int = 0
        private set
    var selectedItemBackground: Int = 0
        private set

    companion object {

        fun detectTheme(activity: Activity): ExpirationDateDialogTheme {
            val theme = if (ViewUtils.isDarkBackground(activity)) LIGHT else DARK

            theme.itemTextColor = activity.resources.getColor(theme.mItemTextColor)
            theme.itemInvertedTextColor = ColorUtils.getColor(activity,
                "textColorPrimaryInverse", theme.mItemInverseTextColor)
            theme.itemDisabledTextColor = activity.resources
                .getColor(theme.mItemDisabledTextColor)
            theme.selectedItemBackground = ColorUtils.getColor(activity, "colorAccent",
                R.color.bt_blue)

            return theme
        }
    }
}


package com.braintreepayments.cardform.utils

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.braintreepayments.cardform.R
import java.util.*

class ExpirationDateItemAdapter @JvmOverloads constructor(
    context: Context,
    resource: Int = R.layout.bt_expiration_date_item,
    textViewResourceId: Int = 0,
    private val theme: ExpirationDateDialogTheme,
    objects: List<String>
) : ArrayAdapter<String>(context, resource, textViewResourceId, objects) {

    private val mSelectedItemBackground: ShapeDrawable
    private var mOnItemClickListener: AdapterView.OnItemClickListener? = null
    private var mSelectedPosition = -1
    private var mDisabledPositions: List<Int> = ArrayList()

    init {
        val radius = context.resources.getDimension(R.dimen.bt_expiration_date_item_selected_background_radius)
        val radii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
        mSelectedItemBackground = ShapeDrawable(RoundRectShape(radii, null, null))
        mSelectedItemBackground.paint.color = theme.selectedItemBackground
    }

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener) {
        mOnItemClickListener = listener
    }

    fun setSelected(position: Int) {
        mSelectedPosition = position
        notifyDataSetChanged()
    }

    fun setDisabled(disabledPositions: List<Int>) {
        mDisabledPositions = disabledPositions
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.isEnabled = true
        if (mSelectedPosition == position) {
            view.setBackgroundDrawable(mSelectedItemBackground)
            view.setTextColor(theme.itemInvertedTextColor)
        } else {
            view.setBackgroundResource(android.R.color.transparent)

            when (mDisabledPositions.contains(position)) {
                true -> {
                    view.setTextColor(theme.itemDisabledTextColor)
                    view.isEnabled = false
                }
                false -> view.setTextColor(theme.itemTextColor)
            }
        }

        view.setOnClickListener { v ->
            mSelectedPosition = position
            notifyDataSetChanged()
            VibrationHelper.vibrate(context, 10)

            mOnItemClickListener?.onItemClick(null, v, position, position.toLong())
        }

        return view
    }
}

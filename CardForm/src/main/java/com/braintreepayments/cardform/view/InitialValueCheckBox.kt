package com.braintreepayments.cardform.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet

import androidx.appcompat.widget.AppCompatCheckBox

/**
 * Sets an initial CheckBox checked state that is overwritten when restoring this view.
 */
class InitialValueCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    private var mRestored: Boolean = false

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        val bundle = Bundle()
        bundle.putParcelable(EXTRA_SUPER_STATE, superState)
        bundle.putBoolean(EXTRA_CHECKED_VALUE, isChecked)

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle

        super.onRestoreInstanceState(bundle.getParcelable(EXTRA_SUPER_STATE))

        isChecked = bundle.getBoolean(EXTRA_CHECKED_VALUE)

        mRestored = true
    }

    /**
     * Sets the initial value for the CheckBox checked state.
     *
     * @param checked the CheckBox checked state.
     */
    fun setInitiallyChecked(checked: Boolean) {
        if (!mRestored) {
            isChecked = checked
        }
    }

    companion object {
        private const val EXTRA_SUPER_STATE = "com.braintreepayments.cardform.view.InitialValueCheckBox.EXTRA_SUPER_STATE"
        private const val EXTRA_CHECKED_VALUE = "com.braintreepayments.cardform.view.InitialValueCheckBox.EXTRA_CHECKED_VALUE"
    }
}

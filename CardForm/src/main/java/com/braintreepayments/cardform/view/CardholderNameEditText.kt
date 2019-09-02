package com.braintreepayments.cardform.view

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet

import com.braintreepayments.cardform.R

/**
 * Input for cardholder name. Validated for presence only.
 */
class CardholderNameEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ErrorEditText(context, attrs, defStyleAttr) {

    init {
        inputType = InputType.TYPE_CLASS_TEXT
        val filters = arrayOf<InputFilter>(LengthFilter(255))
        setFilters(filters)
    }

    override fun isValid(): Boolean = isOptional || text.toString().trim { it <= ' ' }.isNotEmpty()

    override fun getErrorMessage(): String? =
        context.getString(R.string.bt_cardholder_name_required)
}

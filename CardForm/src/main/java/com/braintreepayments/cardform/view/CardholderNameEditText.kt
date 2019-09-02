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
class CardholderNameEditText : ErrorEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        inputType = InputType.TYPE_CLASS_TEXT
        val filters = arrayOf<InputFilter>(LengthFilter(255))
        setFilters(filters)
    }

    override fun isValid(): Boolean = isOptional || text?.toString().orEmpty().trim { it <= ' ' }.isNotEmpty()

    override fun getErrorMessage(): String? =
        context.getString(R.string.bt_cardholder_name_required)
}

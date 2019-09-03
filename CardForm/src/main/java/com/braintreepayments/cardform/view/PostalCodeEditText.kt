package com.braintreepayments.cardform.view

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet

import com.braintreepayments.cardform.R

/**
 * Input for postal codes. Validated for presence only due to the wide variation of postal code
 * formats worldwide.
 */
class PostalCodeEditText : ErrorEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        inputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
        val filters = arrayOf<InputFilter>(LengthFilter(16))
        setFilters(filters)
    }

    override fun isValid(): Boolean = isOptional || !text?.toString().isNullOrEmpty()

    override fun getErrorMessage(): String? = context.getString(R.string.bt_postal_code_required)
}

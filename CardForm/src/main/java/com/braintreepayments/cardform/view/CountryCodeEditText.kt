package com.braintreepayments.cardform.view

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet

import com.braintreepayments.cardform.R

/**
 * Input for country code. Validated for presence only due to the wide variation of country code formats worldwide.
 */
class CountryCodeEditText : ErrorEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    /**
     * @return the numeric country code entered by the user
     */
    val countryCode: String
        get() = text.toString().replace("[^\\d]".toRegex(), "")


    init {
        inputType = InputType.TYPE_CLASS_PHONE
        val filters = arrayOf<InputFilter>(LengthFilter(4))
        setFilters(filters)
    }

    override fun isValid(): Boolean = isOptional || text.toString().isNotEmpty()

    override fun getErrorMessage(): String? = context.getString(R.string.bt_country_code_required)
}

package com.braintreepayments.cardform.view

import android.content.Context
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet

import com.braintreepayments.cardform.R

/**
 * Input for mobile number. Validated for presence only due to the wide variation of mobile number formats worldwide.
 */
class MobileNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ErrorEditText(context, attrs, defStyleAttr) {

    /**
     * @return the unformatted mobile number entered by the user
     */
    val mobileNumber: String
        get() = PhoneNumberUtils.stripSeparators(text!!.toString())

    init {
        if (!isInEditMode) {
            inputType = InputType.TYPE_CLASS_PHONE
            val filters = arrayOf<InputFilter>(LengthFilter(14))
            setFilters(filters)
            addTextChangedListener(PhoneNumberFormattingTextWatcher())
        }
    }

    override fun isValid(): Boolean = isOptional || text.toString().length >= 8

    override fun getErrorMessage(): String? = context.getString(R.string.bt_mobile_number_required)
}

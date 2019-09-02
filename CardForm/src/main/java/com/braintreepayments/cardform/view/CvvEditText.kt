package com.braintreepayments.cardform.view

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import com.braintreepayments.cardform.R
import com.braintreepayments.cardform.utils.CardType

/**
 * An [android.widget.EditText] that displays a CVV hint for a given Card type when focused.
 */
class CvvEditText : ErrorEditText, TextWatcher {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var mCardType: CardType? = null

    private val securityCodeLength: Int
        get() = mCardType?.securityCodeLength ?: DEFAULT_MAX_LENGTH

    init {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(LengthFilter(DEFAULT_MAX_LENGTH))
        addTextChangedListener(this)
    }

    /**
     * Sets the card type associated with the security code type.
     * [CardType.AMEX] has a different icon and length than other card types.
     * Typically handled through [CardEditText.OnCardTypeChangedListener.onCardTypeChanged].
     *
     * @param cardType Type of card represented by the current value of card number input.
     */
    fun setCardType(cardType: CardType) {
        mCardType = cardType

        val filters = arrayOf<InputFilter>(LengthFilter(cardType.securityCodeLength))
        setFilters(filters)

        contentDescription = context.getString(cardType.securityCodeName)
        setFieldHint(cardType.securityCodeName)

        invalidate()
    }

    /**
     * @param mask if `true`, this field will be masked.
     */
    fun setMask(mask: Boolean) {
        inputType = if (mask) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_NUMBER
        }
    }

    override fun afterTextChanged(editable: Editable) {
        mCardType
            ?.takeIf { securityCodeLength == editable.length && selectionStart == editable.length }
            ?.run {
                validate()

                if (isValid()) focusNextView()
            }
    }

    override fun isValid(): Boolean = isOptional || text?.toString().orEmpty().length == securityCodeLength

    override fun getErrorMessage(): String? {
        val securityCodeName = mCardType
            ?.let { context.getString(it.securityCodeName) }
            ?: context.getString(R.string.bt_cvv)

        return if (text.isNullOrEmpty()) {
            context.getString(R.string.bt_cvv_required, securityCodeName)
        } else {
            context.getString(R.string.bt_cvv_invalid, securityCodeName)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    companion object {
        private const val DEFAULT_MAX_LENGTH = 3
    }
}

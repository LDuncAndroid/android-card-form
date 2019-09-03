package com.braintreepayments.cardform.view

import android.content.Context
import android.graphics.Rect
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.method.TransformationMethod
import android.util.AttributeSet
import androidx.core.widget.TextViewCompat
import com.braintreepayments.cardform.R
import com.braintreepayments.cardform.utils.CardNumberTransformation
import com.braintreepayments.cardform.utils.CardType

/**
 * An [android.widget.EditText] that displays Card icons based on the number entered.
 */
class CardEditText : ErrorEditText, TextWatcher {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var mDisplayCardIcon = true
    private var mMask = false
    private var mOnCardTypeChangedListener: OnCardTypeChangedListener? = null
    private var mSavedTransformationMethod: TransformationMethod? = null

    /**
     * @return The [CardType] currently entered in the [android.widget.EditText]
     */
    var cardType: CardType? = null
        private set

    interface OnCardTypeChangedListener {
        fun onCardTypeChanged(cardType: CardType)
    }

    init {
        inputType = InputType.TYPE_CLASS_NUMBER
        setCardIcon(R.drawable.bt_ic_unknown)
        addTextChangedListener(this)
        updateCardType()
        mSavedTransformationMethod = transformationMethod
    }

    /**
     * Enable or disable showing card type icons as part of the [CardEditText].
     * Defaults to `true`.
     *
     * @param display `true` to display card type icons, `false` to never display card
     * type icons.
     */
    fun displayCardTypeIcon(display: Boolean) {
        mDisplayCardIcon = display

        if (!mDisplayCardIcon) setCardIcon(-1)
    }

    /**
     * @param mask if `true`, all but the last four digits of the card number will be masked when
     * focus leaves the card field. Uses [CardNumberTransformation], transforming the number from
     * something like "4111111111111111" to "•••• 1111".
     */
    fun setMask(mask: Boolean) {
        mMask = mask
    }

    public override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (focused) {
            unmaskNumber()

            text?.toString()
                ?.takeIf { it.isNotBlank() }
                ?.let { setSelection(it.length) }

        } else if (mMask && isValid()) {
            maskNumber()
        }
    }

    /**
     * Receive a callback when the [CardType] changes
     * @param listener to be called when the [CardType] changes
     */
    fun setOnCardTypeChangedListener(listener: OnCardTypeChangedListener) {
        mOnCardTypeChangedListener = listener
    }

    override fun afterTextChanged(editable: Editable) {
        editable.getSpans(0, editable.length, SpaceSpan::class.java)
            .forEach { editable.removeSpan(it) }

        updateCardType()
        cardType?.also {
            setCardIcon(it.frontResource)

            addSpans(editable, it.spaceIndices)

            if (it.maxCardLength == selectionStart) {
                validate()

                when (isValid()) {
                    true -> focusNextView()
                    false -> unmaskNumber()
                }
            } else if (!hasFocus() && mMask) {
                maskNumber()
            }
        }
    }

    override fun isValid(): Boolean = isOptional || (cardType?.validate(text?.toString().orEmpty()) == true)

    override fun getErrorMessage(): String? =
        when (text.isNullOrEmpty()) {
            true -> context.getString(R.string.bt_card_number_required)
            false -> context.getString(R.string.bt_card_number_invalid)
        }

    private fun maskNumber() {
        if (transformationMethod !is CardNumberTransformation) {
            mSavedTransformationMethod = transformationMethod

            transformationMethod = CardNumberTransformation()
        }
    }

    private fun unmaskNumber() {
        if (transformationMethod !== mSavedTransformationMethod) {
            transformationMethod = mSavedTransformationMethod
        }
    }

    private fun updateCardType() =
        text.let { it?.toString().orEmpty() }
            .let { CardType.forCardNumber(it) }
            .takeIf { cardType != it }
            ?.also { cardType = it }
            ?.also {
                val filters = arrayOf<InputFilter>(LengthFilter(it.maxCardLength))
                setFilters(filters)
                invalidate()

                mOnCardTypeChangedListener?.onCardTypeChanged(it)
            }


    private fun addSpans(editable: Editable, spaceIndices: IntArray) =
        spaceIndices
            .asSequence()
            .filter { it <= editable.length }
            .forEach { editable.setSpan(SpaceSpan(), it - 1, it, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }

    private fun setCardIcon(icon: Int) =
        if (!mDisplayCardIcon || text.isNullOrEmpty()) {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, 0, 0, 0, 0)
        } else {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, 0, 0, icon, 0)
        }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
}

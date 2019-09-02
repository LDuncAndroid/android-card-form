package com.braintreepayments.cardform.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.widget.TextView
import com.braintreepayments.cardform.utils.CardType
import java.util.*

/**
 * Display a set of icons for a list of supported card types.
 */
@SuppressLint("AppCompatCustomView")
class SupportedCardTypesView : TextView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val mSupportedCardTypes = mutableListOf<CardType>()

    /**
     * Sets the supported [CardType]s on the view to display the card icons.
     *
     * @param cardTypes The [CardType]s to display
     */
    fun setSupportedCardTypes(vararg cardTypes: CardType) {
        mSupportedCardTypes.clear()
        mSupportedCardTypes.addAll(listOfNotNull(*cardTypes))

        setSelected(*cardTypes)
    }

    /**
     * Selects the intersection between the [CardType]s passed into
     * [.setSupportedCardTypes] and [CardType]s passed into
     * this method as visually enabled.
     *
     * The remaining supported card types will become visually disabled.
     *
     * [.setSupportedCardTypes] must be called prior to using this method.
     *
     * @param cardTypes The [CardType]s to set as visually enabled.
     */
    fun setSelected(vararg cardTypes: CardType) {
        val spannableString = SpannableString(String(CharArray(mSupportedCardTypes.size)))
        var span: PaddedImageSpan
        for (i in mSupportedCardTypes.indices) {
            span = PaddedImageSpan(context, mSupportedCardTypes[i].frontResource)
            span.isDisabled = !listOfNotNull(*cardTypes).contains(mSupportedCardTypes[i])
            spannableString.setSpan(span, i, i + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        text = spannableString
    }
}

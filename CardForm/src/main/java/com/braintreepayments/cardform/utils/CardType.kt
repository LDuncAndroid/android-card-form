package com.braintreepayments.cardform.utils

import android.text.TextUtils
import com.braintreepayments.cardform.R
import java.util.regex.Pattern

/**
 * Card types and related formatting and validation rules.
 */
enum class CardType(
    regex: String,
    /**
     * @return The android resource id for the front card image, highlighting card number format.
     */
    val frontResource: Int,
    /**
     * @return minimum length of a card for this [com.braintreepayments.cardform.utils.CardType]
     */
    val minCardLength: Int,
    /**
     * @return max length of a card for this [com.braintreepayments.cardform.utils.CardType]
     */
    val maxCardLength: Int,
    /**
     * @return The length of the current card's security code.
     */
    val securityCodeLength: Int,
    /**
     * @return The android resource id for the security code name for this card type.
     */
    val securityCodeName: Int, relaxedPrefixPattern: String?) {

    VISA("^4\\d*",
        R.drawable.bt_ic_visa,
        16, 16,
        3, R.string.bt_cvv, null),
    MASTERCARD("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
        R.drawable.bt_ic_mastercard,
        16, 16,
        3, R.string.bt_cvc, null),
    DISCOVER("^(6011|65|64[4-9]|622)\\d*",
        R.drawable.bt_ic_discover,
        16, 16,
        3, R.string.bt_cid, null),
    AMEX("^3[47]\\d*",
        R.drawable.bt_ic_amex,
        15, 15,
        4, R.string.bt_cid, null),
    DINERS_CLUB("^(36|38|30[0-5])\\d*",
        R.drawable.bt_ic_diners_club,
        14, 14,
        3, R.string.bt_cvv, null),
    JCB("^35\\d*",
        R.drawable.bt_ic_jcb,
        16, 16,
        3, R.string.bt_cvv, null),
    MAESTRO("^(5018|5020|5038|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*",
        R.drawable.bt_ic_maestro,
        12, 19,
        3, R.string.bt_cvc,
        "^6\\d*"),
    UNIONPAY("^62\\d*",
        R.drawable.bt_ic_unionpay,
        16, 19,
        3, R.string.bt_cvn, null),
    HIPER("^637(095|568|599|609|612)\\d*",
        R.drawable.bt_ic_hiper,
        16, 16,
        3, R.string.bt_cvc, null),
    HIPERCARD("^606282\\d*",
        R.drawable.bt_ic_hipercard,
        16, 16,
        3, R.string.bt_cvc, null),
    UNKNOWN("\\d+",
        R.drawable.bt_ic_unknown,
        12, 19,
        3, R.string.bt_cvv, null),
    EMPTY("^$",
        R.drawable.bt_ic_unknown,
        12, 19,
        3, R.string.bt_cvv, null);

    /**
     * @return The regex matching this card type.
     */
    val pattern: Pattern = Pattern.compile(regex)

    /**
     * @return The relaxed prefix regex matching this card type. To be used in determining
     * card type if no pattern matches.
     */
    val relaxedPrefixPattern: Pattern? =
        relaxedPrefixPattern?.let { Pattern.compile(relaxedPrefixPattern) }

    /**
     * @return the locations where spaces should be inserted when formatting the card in a user
     * friendly way. Only for display purposes.
     */
    val spaceIndices: IntArray
        get() = if (this == AMEX) AMEX_SPACE_INDICES else DEFAULT_SPACE_INDICES


    /**
     * @param cardNumber The card number to validate.
     * @return `true` if this card number is locally valid.
     */
    fun validate(cardNumber: String): Boolean {
        val isEmpty = TextUtils.isEmpty(cardNumber)
        val isNotDigitsOnly = !TextUtils.isDigitsOnly(cardNumber)
        val isInaccurateLength =
            cardNumber.length < minCardLength || cardNumber.length > maxCardLength
        val isNotPatternMatch = !pattern.matcher(cardNumber).matches()
            && relaxedPrefixPattern?.matcher(cardNumber)?.let { !it.matches() } ?: false

        return when {
            isEmpty -> false
            isNotDigitsOnly -> false
            isInaccurateLength -> false
            isNotPatternMatch -> false
            else -> isLuhnValid(cardNumber)
        }
    }

    companion object {

        private val AMEX_SPACE_INDICES = intArrayOf(4, 10)
        private val DEFAULT_SPACE_INDICES = intArrayOf(4, 8, 12)

        /**
         * Returns the card type matching this account, or
         * [com.braintreepayments.cardform.utils.CardType.UNKNOWN] for no match.
         *
         * A partial account type may be given, with the caveat that it may not have enough digits
         * to match.
         */
        fun forCardNumber(cardNumber: String): CardType {
            val patternMatch = forCardNumberPattern(cardNumber)
            if (patternMatch != EMPTY && patternMatch != UNKNOWN) {
                return patternMatch
            }

            val relaxedPrefixPatternMatch = forCardNumberRelaxedPrefixPattern(cardNumber)
            if (relaxedPrefixPatternMatch != EMPTY && relaxedPrefixPatternMatch != UNKNOWN) {
                return relaxedPrefixPatternMatch
            }

            return if (cardNumber.isNotEmpty()) {
                UNKNOWN
            } else EMPTY
        }

        private fun forCardNumberPattern(cardNumber: String): CardType =
            values().firstOrNull { it.pattern.matcher(cardNumber).matches() } ?: EMPTY

        private fun forCardNumberRelaxedPrefixPattern(cardNumber: String): CardType =
            values()
                .firstOrNull { it.relaxedPrefixPattern?.matcher(cardNumber)?.matches() ?: false }
                ?: EMPTY

        /**
         * Performs the Luhn check on the given card number.
         *
         * @param cardNumber a String consisting of numeric digits (only).
         * @return `true` if the sequence passes the checksum
         * @throws IllegalArgumentException if `cardNumber` contained a non-digit
         * (where [Character.isDefined] is `false`).
         * @see [Luhn Algorithm](http://en.wikipedia.org/wiki/Luhn_algorithm)
         */
        fun isLuhnValid(cardNumber: String): Boolean {
            val reversed = StringBuffer(cardNumber).reverse().toString()
            val len = reversed.length
            var oddSum = 0
            var evenSum = 0
            for (i in 0 until len) {
                val c = reversed[i]
                require(Character.isDigit(c)) { String.format("Not a digit: '%s'", c) }
                val digit = Character.digit(c, 10)
                if (i % 2 == 0) {
                    oddSum += digit
                } else {
                    evenSum += digit / 5 + 2 * digit % 10
                }
            }
            return (oddSum + evenSum) % 10 == 0
        }
    }
}
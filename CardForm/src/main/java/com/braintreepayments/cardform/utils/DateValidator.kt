package com.braintreepayments.cardform.utils

import android.text.TextUtils
import java.util.*

/**
 * Class provided as a convenience to [com.braintreepayments.cardform.view.ExpirationDateEditText]
 * to make testing easier.
 *
 * Used in tests to inject a custom [java.util.Calendar] to stabilize dates. Normal usage will just
 * delegate to the actual date.
 */
class DateValidator(private val mCalendar: Calendar) {

    /**
     * [java.util.Calendar.MONTH] is 0-prefixed. Add `1` to align it with visualized expiration
     * dates.
     */
    private val currentMonth: Int
        get() = mCalendar.get(Calendar.MONTH) + 1

    /**
     * [java.util.Calendar.YEAR] is the full, 4-digit year. Take the trailing two digits to align
     * it with visualized expiration dates.
     */
    private val currentTwoDigitYear: Int
        get() = mCalendar.get(Calendar.YEAR) % 100

    fun isValidHelper(monthString: String, yearString: String): Boolean {
        if (TextUtils.isEmpty(monthString)) {
            return false
        }

        if (TextUtils.isEmpty(yearString)) {
            return false
        }

        if (!TextUtils.isDigitsOnly(monthString) || !TextUtils.isDigitsOnly(yearString)) {
            return false
        }

        val month = Integer.parseInt(monthString)
        if (month < 1 || month > 12) {
            return false
        }

        val currentYear = currentTwoDigitYear
        val year = when (yearString.length) {
            2 -> Integer.parseInt(yearString)
            4 -> Integer.parseInt(yearString.substring(2))
            else -> return false
        }

        if (year == currentYear && month < currentMonth) {
            return false
        }

        if (year < currentYear) {
            // account for century-overlapping in 2-digit year representations
            val adjustedYear = year + 100
            if (adjustedYear - currentYear > MAXIMUM_VALID_YEAR_DIFFERENCE) {
                return false
            }
        }

        return year <= currentYear + MAXIMUM_VALID_YEAR_DIFFERENCE

    }

    companion object {

        /**
         * Maximum amount of years in advance that a credit card expiration date should be trusted
         * to be valid. This is mostly used if the current date is towards the end of the century
         * and the expiration date is at the start of the following one.
         *
         * Ex. Current year is 2099, Expiration date is "01/01". The YY is "less than" the current
         * year, but since the difference is less than `MAXIMUM_VALID_YEAR_DIFFERENCE`, it should
         * still be trusted to be valid client-side.
         */
        const val MAXIMUM_VALID_YEAR_DIFFERENCE = 20

        private val INSTANCE = DateValidator(Calendar.getInstance())

        /**
         * Helper for determining whether a date is a valid credit card expiry date.
         * @param month Two-digit month
         * @param year Two or four digit year
         * @return Whether the date is a valid credit card expiry date.
         */
        fun isValid(month: String, year: String): Boolean {
            return INSTANCE.isValidHelper(month, year)
        }
    }
}

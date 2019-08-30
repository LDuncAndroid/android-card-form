package com.braintreepayments.cardform

/**
 * Listener to receive a callback when the card form becomes valid or invalid
 */
interface OnCardFormValidListener {

    /**
     * Called when the card form becomes valid or invalid
     * @param valid indicates wither the card form is currently valid or invalid
     */
    fun onCardFormValid(valid: Boolean)
}

package com.braintreepayments.cardform

/**
 * Listener to receive a callback when the card form should be submitted.
 * This is triggered from a keyboard by a [android.view.inputmethod.EditorInfo.IME_ACTION_GO]
 * event
 */
interface OnCardFormSubmitListener {

    /**
     * Called when the card form requests that it be submitted. Triggered from a keyboard by a
     * [android.view.inputmethod.EditorInfo.IME_ACTION_GO] event
     */
    fun onCardFormSubmit()
}

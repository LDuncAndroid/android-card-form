package com.braintreepayments.cardform.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import com.braintreepayments.cardform.*
import com.braintreepayments.cardform.utils.CardType
import com.braintreepayments.cardform.utils.ViewUtils
import com.braintreepayments.cardform.view.CardEditText.OnCardTypeChangedListener
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import kotlinx.android.synthetic.main.bt_card_form_fields.view.*

class CardForm : LinearLayout, OnCardTypeChangedListener, OnFocusChangeListener, OnClickListener,
    OnEditorActionListener, TextWatcher {

    private var mCardScanningFragment: CardScanningFragment? = null

    private val mVisibleEditTexts: MutableList<ErrorEditText> = mutableListOf()

    private var mCardNumberRequired: Boolean = false
    private var mExpirationRequired: Boolean = false
    private var mCvvRequired: Boolean = false
    private var mCardholderNameStatus = FIELD_DISABLED
    private var mPostalCodeRequired: Boolean = false
    private var mMobileNumberRequired: Boolean = false
    private var mActionLabel: String? = null
    private var mSaveCardCheckBoxVisible: Boolean = false
    private var mSaveCardCheckBoxChecked: Boolean = false

    private var mValid = false

    private var mOnCardFormValidListener: OnCardFormValidListener? = null
    private var mOnCardFormSubmitListener: OnCardFormSubmitListener? = null
    private var mOnCardFormFieldFocusedListener: OnCardFormFieldFocusedListener? = null
    private var mOnCardTypeChangedListener: OnCardTypeChangedListener? = null


    /**
     * @return [CardholderNameEditText] view in the card form
     */
    fun getCardholderNameEditText(): CardholderNameEditText = bt_card_form_cardholder_name

    /**
     * @return [CardEditText] view in the card form
     */
    fun getCardEditText(): CardEditText = bt_card_form_card_number

    /**
     * @return [ExpirationDateEditText] view in the card form
     */
    fun getExpirationDateEditText(): ExpirationDateEditText = bt_card_form_expiration

    /**
     * @return [CvvEditText] view in the card form
     */
    fun getCvvEditText(): CvvEditText = bt_card_form_cvv

    /**
     * @return [PostalCodeEditText] view in the card form
     */
    fun getPostalCodeEditText(): PostalCodeEditText = bt_card_form_postal_code

    /**
     * @return [CountryCodeEditText] view in the card form
     */
    fun getCountryCodeEditText(): CountryCodeEditText = bt_card_form_country_code

    /**
     * @return [MobileNumberEditText] view in the card form
     */
    fun getMobileNumberEditText(): MobileNumberEditText = bt_card_form_mobile_number

    /**
     * Check if card scanning is available.
     *
     * Card scanning requires the card.io dependency and camera support.
     *
     * @return `true` if available, `false` otherwise.
     */
    val isCardScanningAvailable: Boolean
        get() = try {
            CardIOActivity.canReadCardWithCamera()
        } catch (e: NoClassDefFoundError) {
            false
        }

    /**
     * @return `true` if all require fields are valid, otherwise `false`
     */
    val isValid: Boolean
        get() {
            var valid = true
            if (mCardholderNameStatus == FIELD_REQUIRED) {
                valid = valid && bt_card_form_cardholder_name.isValid()
            }
            if (mCardNumberRequired) {
                valid = valid && bt_card_form_card_number.isValid()
            }
            if (mExpirationRequired) {
                valid = valid && bt_card_form_expiration.isValid()
            }
            if (mCvvRequired) {
                valid = valid && bt_card_form_cvv.isValid()
            }
            if (mPostalCodeRequired) {
                valid = valid && bt_card_form_postal_code.isValid()
            }
            if (mMobileNumberRequired) {
                valid = valid && bt_card_form_country_code.isValid() && bt_card_form_mobile_number.isValid()
            }
            return valid
        }

    /**
     * @return the text in the cardholder name field
     */
    val cardholderName: String
        get() = bt_card_form_cardholder_name.text?.toString().orEmpty()

    /**
     * @return the text in the card number field
     */
    val cardNumber: String
        get() = bt_card_form_card_number.text?.toString().orEmpty()

    /**
     * @return the 2-digit month, formatted with a leading zero if necessary from the expiration
     * field. If no month has been specified, an empty string is returned.
     */
    val expirationMonth: String
        get() = bt_card_form_expiration.month

    /**
     * @return the 2- or 4-digit year depending on user input from the expiration field.
     * If no year has been specified, an empty string is returned.
     */
    val expirationYear: String
        get() = bt_card_form_expiration.year

    /**
     * @return the text in the cvv field
     */
    val cvv: String
        get() = bt_card_form_cvv.text?.toString().orEmpty()

    /**
     * @return the text in the postal code field
     */
    val postalCode: String
        get() = bt_card_form_postal_code.text?.toString().orEmpty()

    /**
     * @return the text in the country code field
     */
    val countryCode: String
        get() = bt_card_form_country_code.countryCode

    /**
     * @return the unformatted text in the mobile number field
     */
    val mobileNumber: String
        get() = bt_card_form_mobile_number.mobileNumber

    /**
     * @return whether or not the save card CheckBox is checked
     */
    val isSaveCardCheckBoxChecked: Boolean
        get() = bt_card_form_save_card_checkbox.isChecked

    /**
     * The statuses a field can be.
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(FIELD_DISABLED, FIELD_OPTIONAL, FIELD_REQUIRED)
    internal annotation class FieldStatus {}

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : super(context, attrs, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
        : super(context, attrs, defStyleAttr, defStyleRes)


    init {
        visibility = View.GONE
        orientation = VERTICAL

        View.inflate(context, R.layout.bt_card_form_fields, this)

        setListeners(bt_card_form_cardholder_name)
        setListeners(bt_card_form_card_number)
        setListeners(bt_card_form_expiration)
        setListeners(bt_card_form_cvv)
        setListeners(bt_card_form_postal_code)
        setListeners(bt_card_form_mobile_number)

        bt_card_form_card_number.setOnCardTypeChangedListener(this)
    }

    /**
     * @param required `true` to show and require a credit card number, `false` otherwise. Defaults to `false`.
     * @return [CardForm] for method chaining
     */
    fun cardRequired(required: Boolean): CardForm {
        mCardNumberRequired = required
        return this
    }

    /**
     * @param required `true` to show and require an expiration date, `false` otherwise. Defaults to `false`.
     * @return [CardForm] for method chaining
     */
    fun expirationRequired(required: Boolean): CardForm {
        mExpirationRequired = required
        return this
    }

    /**
     * @param required `true` to show and require a cvv, `false` otherwise. Defaults to `false`.
     * @return [CardForm] for method chaining
     */
    fun cvvRequired(required: Boolean): CardForm {
        mCvvRequired = required
        return this
    }

    /**
     * @param cardHolderNameStatus can be one of the [FieldStatus] options.
     * - [CardForm.FIELD_DISABLED] to hide this field. This is the default option.
     * - [CardForm.FIELD_OPTIONAL] to show this field but make it an optional field.
     * - [CardForm.FIELD_REQUIRED] to show this field and make it required to validate the card form.
     *
     * @return [CardForm] for method chaining
     */
    fun cardholderName(@FieldStatus cardHolderNameStatus: Int): CardForm {
        mCardholderNameStatus = cardHolderNameStatus
        return this
    }

    /**
     * @param required `true` to show and require a postal code, `false` otherwise. Defaults to `false`.
     * @return [CardForm] for method chaining
     */
    fun postalCodeRequired(required: Boolean): CardForm {
        mPostalCodeRequired = required
        return this
    }

    /**
     * @param required `true` to show and require a mobile number, `false` otherwise. Defaults to `false`.
     * @return [CardForm] for method chaining
     */
    fun mobileNumberRequired(required: Boolean): CardForm {
        mMobileNumberRequired = required
        return this
    }

    /**
     * @param actionLabel the [java.lang.String] to display to the user to submit the form from the keyboard
     * @return [CardForm] for method chaining
     */
    fun actionLabel(actionLabel: String): CardForm {
        mActionLabel = actionLabel
        return this
    }

    /**
     * @param mobileNumberExplanation the [java.lang.String] to display below the mobile number input
     * @return [CardForm] for method chaining
     */
    fun mobileNumberExplanation(mobileNumberExplanation: String): CardForm {
        bt_card_form_mobile_number_explanation.text = mobileNumberExplanation
        return this
    }

    /**
     * @param mask if `true`, card number input will be masked.
     */
    fun maskCardNumber(mask: Boolean): CardForm {
        bt_card_form_card_number.setMask(mask)
        return this
    }

    /**
     * @param mask if `true`, CVV input will be masked.
     */
    fun maskCvv(mask: Boolean): CardForm {
        bt_card_form_cvv.setMask(mask)
        return this
    }

    /**
     * @param visible Determines if the save card CheckBox should be shown. Defaults to hidden / `false`
     * @return [CardForm] for method chaining
     */
    fun saveCardCheckBoxVisible(visible: Boolean): CardForm {
        mSaveCardCheckBoxVisible = visible
        return this
    }

    /**
     * @param checked The default value for the Save Card CheckBox.
     * @return [CardForm] for method chaining
     */
    fun saveCardCheckBoxChecked(checked: Boolean): CardForm {
        mSaveCardCheckBoxChecked = checked
        return this
    }


    /**
     * Sets up the card form for display to the user using the values provided in [CardForm.cardRequired],
     * [CardForm.expirationRequired], ect. If [CardForm.setup] is not called,
     * the form will not be visible.
     *
     * @param activity Used to set [android.view.WindowManager.LayoutParams.FLAG_SECURE] to prevent screenshots
     */
    fun setup(activity: AppCompatActivity) {
        mCardScanningFragment = activity
            .supportFragmentManager
            .findFragmentByTag(CardScanningFragment.TAG) as CardScanningFragment?

        mCardScanningFragment?.setCardForm(this)

        activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE)

        val cardHolderNameVisible = mCardholderNameStatus != FIELD_DISABLED
        val isDarkBackground = ViewUtils.isDarkBackground(activity)
        bt_card_form_cardholder_name_icon.setImageResource(if (isDarkBackground) R.drawable.bt_ic_cardholder_name_dark else R.drawable.bt_ic_cardholder_name)
        bt_card_form_card_number_icon.setImageResource(if (isDarkBackground) R.drawable.bt_ic_card_dark else R.drawable.bt_ic_card)
        bt_card_form_postal_code_icon.setImageResource(if (isDarkBackground) R.drawable.bt_ic_postal_code_dark else R.drawable.bt_ic_postal_code)
        bt_card_form_mobile_number_icon.setImageResource(if (isDarkBackground) R.drawable.bt_ic_mobile_number_dark else R.drawable.bt_ic_mobile_number)

        bt_card_form_expiration.useDialogForExpirationDateEntry(activity, true)

        setViewVisibility(bt_card_form_cardholder_name_icon, cardHolderNameVisible)
        setFieldVisibility(bt_card_form_cardholder_name, cardHolderNameVisible)
        setViewVisibility(bt_card_form_card_number_icon, mCardNumberRequired)
        setFieldVisibility(bt_card_form_card_number, mCardNumberRequired)
        setFieldVisibility(bt_card_form_expiration, mExpirationRequired)
        setFieldVisibility(bt_card_form_cvv, mCvvRequired)
        setViewVisibility(bt_card_form_postal_code_icon, mPostalCodeRequired)
        setFieldVisibility(bt_card_form_postal_code, mPostalCodeRequired)
        setViewVisibility(bt_card_form_mobile_number_icon, mMobileNumberRequired)
        setFieldVisibility(bt_card_form_country_code, mMobileNumberRequired)
        setFieldVisibility(bt_card_form_mobile_number, mMobileNumberRequired)
        setViewVisibility(bt_card_form_mobile_number_explanation, mMobileNumberRequired)
        setViewVisibility(bt_card_form_save_card_checkbox, mSaveCardCheckBoxVisible)

        mVisibleEditTexts.forEachIndexed { index, errorEditText ->
            if (index == mVisibleEditTexts.size - 1) {
                errorEditText.imeOptions = EditorInfo.IME_ACTION_GO
                errorEditText.setImeActionLabel(mActionLabel, EditorInfo.IME_ACTION_GO)
                errorEditText.setOnEditorActionListener(this)
            } else {
                errorEditText.imeOptions = EditorInfo.IME_ACTION_NEXT
                errorEditText.setImeActionLabel(null, EditorInfo.IME_ACTION_NONE)
                errorEditText.setOnEditorActionListener(null)
            }
        }

        bt_card_form_save_card_checkbox.setInitiallyChecked(mSaveCardCheckBoxChecked)

        visibility = View.VISIBLE
    }

    /**
     * Sets the icon to the left of the card number entry field, overriding the default icon.
     *
     * @param res The drawable resource for the card number icon
     */
    fun setCardNumberIcon(@DrawableRes res: Int) {
        bt_card_form_card_number_icon.setImageResource(res)
    }

    /**
     * Sets the icon to the left of the postal code entry field, overriding the default icon.
     *
     * @param res The drawable resource for the postal code icon.
     */
    fun setPostalCodeIcon(@DrawableRes res: Int) {
        bt_card_form_postal_code_icon.setImageResource(res)
    }

    /**
     * Sets the icon to the left of the mobile number entry field, overriding the default icon.
     *
     * If `null` is passed, the mobile number's icon will be hidden.
     *
     * @param res The drawable resource for the mobile number icon.
     */
    fun setMobileNumberIcon(@DrawableRes res: Int) {
        bt_card_form_mobile_number_icon.setImageResource(res)
    }

    /**
     * Launches card.io card scanning is [isCardScanningAvailable] is `true`.
     *
     * @param activity
     */
    fun scanCard(activity: AppCompatActivity) {
        if (isCardScanningAvailable && mCardScanningFragment == null) {
            mCardScanningFragment = CardScanningFragment.requestScan(activity, this)
        }
    }

    /**
     * Use [.handleCardIOResponse] instead.
     */
    @SuppressLint("DefaultLocale")
    @Deprecated("New function includes resultCode", replaceWith = ReplaceWith("handleCardIOResponse(Int, Intent)"))
    fun handleCardIOResponse(data: Intent) = handleCardIOResponse(Integer.MIN_VALUE, data)

    @SuppressLint("DefaultLocale")
    fun handleCardIOResponse(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED || resultCode == Activity.RESULT_OK) {
            mCardScanningFragment = null
        }

        data?.takeIf { it.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT) }
            ?.let {
                val scanResult = it.getParcelableExtra<CreditCard>(CardIOActivity.EXTRA_SCAN_RESULT)

                if (mCardNumberRequired) {
                    bt_card_form_card_number.setText(scanResult.cardNumber)
                    bt_card_form_card_number.focusNextView()
                }

                if (scanResult.isExpiryValid && mExpirationRequired) {
                    bt_card_form_expiration.setText(String.format("%02d%d", scanResult.expiryMonth, scanResult.expiryYear))
                    bt_card_form_expiration.focusNextView()
                }
            }
    }

    private fun setListeners(editText: EditText) {
        editText.onFocusChangeListener = this
        editText.setOnClickListener(this)
        editText.addTextChangedListener(this)
    }

    private fun setViewVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun setFieldVisibility(editText: ErrorEditText, visible: Boolean) {
        setViewVisibility(editText, visible)
        editText.textInputLayoutParent?.let { setViewVisibility(it, visible) }

        when (visible) {
            true -> mVisibleEditTexts.add(editText)
            false -> mVisibleEditTexts.remove(editText)
        }
    }

    /**
     * Set the listener to receive a callback when the card form becomes valid or invalid
     * @param listener to receive the callback
     */
    fun setOnCardFormValidListener(listener: OnCardFormValidListener) {
        mOnCardFormValidListener = listener
    }

    /**
     * Set the listener to receive a callback when the card form should be submitted.
     * Triggered from a keyboard by a [android.view.inputmethod.EditorInfo.IME_ACTION_GO] event
     *
     * @param listener to receive the callback
     */
    fun setOnCardFormSubmitListener(listener: OnCardFormSubmitListener) {
        mOnCardFormSubmitListener = listener
    }

    /**
     * Set the listener to receive a callback when a field is focused
     *
     * @param listener to receive the callback
     */
    fun setOnFormFieldFocusedListener(listener: OnCardFormFieldFocusedListener) {
        mOnCardFormFieldFocusedListener = listener
    }

    /**
     * Set the listener to receive a callback when the [com.braintreepayments.cardform.utils.CardType] changes.
     *
     * @param listener to receive the callback
     */
    fun setOnCardTypeChangedListener(listener: OnCardTypeChangedListener) {
        mOnCardTypeChangedListener = listener
    }

    /**
     * Set [android.widget.EditText] fields as enabled or disabled
     *
     * @param enabled `true` to enable all required fields, `false` to disable all required fields
     */
    override fun setEnabled(enabled: Boolean) {
        bt_card_form_cardholder_name.isEnabled = enabled
        bt_card_form_card_number.isEnabled = enabled
        bt_card_form_expiration.isEnabled = enabled
        bt_card_form_cvv.isEnabled = enabled
        bt_card_form_postal_code.isEnabled = enabled
        bt_card_form_mobile_number.isEnabled = enabled
    }

    /**
     * Validate all required fields and mark invalid fields with an error indicator
     */
    fun validate() {
        if (mCardholderNameStatus == FIELD_REQUIRED) {
            bt_card_form_cardholder_name.validate()
        }
        if (mCardNumberRequired) {
            bt_card_form_card_number.validate()
        }
        if (mExpirationRequired) {
            bt_card_form_expiration.validate()
        }
        if (mCvvRequired) {
            bt_card_form_cvv.validate()
        }
        if (mPostalCodeRequired) {
            bt_card_form_postal_code.validate()
        }
        if (mMobileNumberRequired) {
            bt_card_form_country_code.validate()
            bt_card_form_mobile_number.validate()
        }
    }

    /**
     * Set visual indicator on name to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setCardholderNameError(errorMessage: String) {
        if (mCardholderNameStatus == FIELD_REQUIRED) {
            bt_card_form_cardholder_name.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused && !bt_card_form_expiration.isFocused && !bt_card_form_cvv.isFocused) {
                requestEditTextFocus(bt_card_form_cardholder_name)
            }
        }
    }

    /**
     * Set visual indicator on card number to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setCardNumberError(errorMessage: String) {
        if (mCardNumberRequired) {
            bt_card_form_card_number.setError(errorMessage)
            requestEditTextFocus(bt_card_form_card_number)
        }
    }

    /**
     * Set visual indicator on expiration to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setExpirationError(errorMessage: String) {
        if (mExpirationRequired) {
            bt_card_form_expiration.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused) {
                requestEditTextFocus(bt_card_form_expiration)
            }
        }
    }

    /**
     * Set visual indicator on cvv to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setCvvError(errorMessage: String) {
        if (mCvvRequired) {
            bt_card_form_cvv.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused && !bt_card_form_expiration.isFocused) {
                requestEditTextFocus(bt_card_form_cvv)
            }
        }
    }

    /**
     * Set visual indicator on postal code to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setPostalCodeError(errorMessage: String) {
        if (mPostalCodeRequired) {
            bt_card_form_postal_code.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused && !bt_card_form_expiration.isFocused && !bt_card_form_cvv.isFocused && !bt_card_form_cardholder_name.isFocused) {
                requestEditTextFocus(bt_card_form_postal_code)
            }
        }
    }

    /**
     * Set visual indicator on country code to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setCountryCodeError(errorMessage: String) {
        if (mMobileNumberRequired) {
            bt_card_form_country_code.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused && !bt_card_form_expiration.isFocused && !bt_card_form_cvv.isFocused && !bt_card_form_cardholder_name.isFocused && !bt_card_form_postal_code.isFocused) {
                requestEditTextFocus(bt_card_form_country_code)
            }
        }
    }

    /**
     * Set visual indicator on mobile number field to indicate error
     *
     * @param errorMessage the error message to display
     */
    fun setMobileNumberError(errorMessage: String) {
        if (mMobileNumberRequired) {
            bt_card_form_mobile_number.setError(errorMessage)
            if (!bt_card_form_card_number.isFocused && !bt_card_form_expiration.isFocused && !bt_card_form_cvv.isFocused && !bt_card_form_cardholder_name.isFocused && !bt_card_form_postal_code.isFocused && !bt_card_form_country_code.isFocused) {
                requestEditTextFocus(bt_card_form_mobile_number)
            }
        }
    }

    private fun requestEditTextFocus(editText: EditText) {
        editText.requestFocus()
        context.getSystemService(Context.INPUT_METHOD_SERVICE)
            ?.let { it as InputMethodManager }
            ?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * Attempt to close the soft keyboard. Will have no effect if the keyboard is not open.
     */
    fun closeSoftKeyboard() = bt_card_form_card_number.closeSoftKeyboard()


    override fun onCardTypeChanged(cardType: CardType) {
        bt_card_form_cvv.setCardType(cardType)

        mOnCardTypeChangedListener?.onCardTypeChanged(cardType)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            mOnCardFormFieldFocusedListener?.onCardFormFieldFocused(v)
        }
    }

    override fun onClick(v: View) {
        mOnCardFormFieldFocusedListener?.onCardFormFieldFocused(v)
    }

    override fun afterTextChanged(s: Editable) {
        val valid = isValid
        if (mValid != valid) {
            mValid = valid
            mOnCardFormValidListener?.onCardFormValid(valid)
        }
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean =
        mOnCardFormSubmitListener
            ?.takeIf { actionId == EditorInfo.IME_ACTION_GO }
            ?.also { it.onCardFormSubmit() }
            ?.let { true }
            ?: false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

    companion object {

        /**
         * Hides the field.
         */
        const val FIELD_DISABLED = 0

        /**
         * Shows the field, and makes the field optional.
         */
        const val FIELD_OPTIONAL = 1

        /**
         * Shows the field, and require the field value to be non empty when validating the card form.
         */
        const val FIELD_REQUIRED = 2
    }
}

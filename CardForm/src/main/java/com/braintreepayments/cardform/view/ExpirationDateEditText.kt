package com.braintreepayments.cardform.view

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.*
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import com.braintreepayments.cardform.R
import com.braintreepayments.cardform.utils.DateValidator

/**
 * An [android.widget.EditText] for entering dates, used for card expiration dates.
 * Will automatically format input as it is entered.
 */
class ExpirationDateEditText : ErrorEditText, TextWatcher, View.OnClickListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var mChangeWasAddition: Boolean = false
    private var mClickListener: OnClickListener? = null
    private var mUseExpirationDateDialog = false
    private var mExpirationDateDialog: ExpirationDateDialog? = null

    /**
     * @return the 2-digit month, formatted with a leading zero if necessary.
     * If no month has beenspecified, an empty string is returned.
     */
    val month: String
        get() = if (string.length < 2) "" else string.substring(0, 2)

    /**
     * @return the 2- or 4-digit year depending on user input.
     * If no year has been specified, an empty string is returned.
     */
    val year: String
        get() = if (string.length == 4 || string.length == 6) string.substring(2) else ""

    /**
     * Convenience method to get the input text as a [String].
     */
    private val string: String
        get() = text?.toString().orEmpty()

    init {
        inputType = InputType.TYPE_CLASS_NUMBER
        val filters = arrayOf<InputFilter>(LengthFilter(6))
        setFilters(filters)
        addTextChangedListener(this)
        setShowKeyboardOnFocus(!mUseExpirationDateDialog)
        isCursorVisible = !mUseExpirationDateDialog
        super.setOnClickListener(this)
    }

    /**
     * Used to enable or disable entry of the expiration date using [ExpirationDateDialog].
     * Defaults to false.
     *
     * @param activity used as the parent activity for the dialog
     * @param useDialog `false` to use a numeric keyboard to enter the expiration date,
     * `true` to use a custom dialog to enter the expiration date. Defaults to `true`.
     */
    fun useDialogForExpirationDateEntry(activity: Activity, useDialog: Boolean) {
        mExpirationDateDialog = ExpirationDateDialog.create(activity, this)
        mUseExpirationDateDialog = useDialog
        setShowKeyboardOnFocus(!mUseExpirationDateDialog)
        isCursorVisible = !mUseExpirationDateDialog
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun onClick(v: View) {
        if (mUseExpirationDateDialog) {
            closeSoftKeyboard()
            mExpirationDateDialog?.show()
        }

        mClickListener?.onClick(v)
    }

    public override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        mExpirationDateDialog?.let {
            if (focused && mUseExpirationDateDialog) {
                closeSoftKeyboard()
                it.show()
            } else if (mUseExpirationDateDialog) {
                it.dismiss()
            }
        }
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mExpirationDateDialog?.takeIf { it.isShowing }?.dismiss()
    }

    /**
     * @return whether or not the input is a valid card expiration date.
     */
    override fun isValid(): Boolean = isOptional || DateValidator.isValid(month, year)

    override fun getErrorMessage(): String? =
        when (text.isNullOrEmpty()) {
            true -> context.getString(R.string.bt_expiration_required)
            false -> context.getString(R.string.bt_expiration_invalid)
        }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        mChangeWasAddition = lengthAfter > lengthBefore
    }

    override fun afterTextChanged(editable: Editable) {
        if (mChangeWasAddition) {
            if (editable.length == 1 && Character.getNumericValue(editable[0]) >= 2) {
                prependLeadingZero(editable)
            }
        }

        editable.getSpans(0, editable.length, SlashSpan::class.java).forEach { editable.removeSpan(it) }

        addDateSlash(editable)

        if ((selectionStart == 4 && !editable.toString().endsWith("20") || selectionStart == 6) && isValid()) {
            focusNextView()
        }
    }

    private fun setShowKeyboardOnFocus(showKeyboardOnFocus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = showKeyboardOnFocus
        } else {
            try {
                // API 16-21
                val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(this, showKeyboardOnFocus)
            } catch (e: Exception) {
                try {
                    // API 15
                    val method = EditText::class.java.getMethod("setSoftInputShownOnFocus", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(this, showKeyboardOnFocus)
                } catch (e1: Exception) {
                    mUseExpirationDateDialog = false
                }

            }

        }
    }

    private fun prependLeadingZero(editable: Editable) {
        val firstChar = editable[0]
        editable.replace(0, 1, "0").append(firstChar)
    }

    private fun addDateSlash(editable: Editable) {
        val index = 2
        val length = editable.length
        if (index <= length) {
            editable.setSpan(SlashSpan(), index - 1, index,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
}

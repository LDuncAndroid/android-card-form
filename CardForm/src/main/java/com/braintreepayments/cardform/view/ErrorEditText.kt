package com.braintreepayments.cardform.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.braintreepayments.cardform.R
import com.braintreepayments.cardform.utils.VibrationHelper
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Parent [android.widget.EditText] for storing and displaying error states.
 */
open class ErrorEditText : TextInputEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var mErrorAnimator: Animation? = null

    /**
     * The current error state of the [android.widget.EditText]
     */
    var isError: Boolean = false
        private set

    /**
     * Optional fields are always valid and show no error message.
     */
    var isOptional: Boolean = false

    /**
     * Override this method validation logic
     *
     * @return `true`
     */
    open fun isValid(): Boolean = true

    /**
     * Override this method to display error messages
     *
     * @return [String] error message to display.
     */
    open fun getErrorMessage(): String? = null

    /**
     * @return the [TextInputLayout] parent if present, otherwise null.
     */
    val textInputLayoutParent: TextInputLayout?
        get() = if (parent?.parent is TextInputLayout) parent.parent as TextInputLayout else null

    init {
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.bt_error_animation)
        isError = false
        setupRTL()
    }

    public override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (lengthBefore != lengthAfter) {
            setError(null)
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused && !isValid() && !TextUtils.isEmpty(text)) {
            setError(getErrorMessage())
        }
    }

    /**
     * Sets the hint on the [TextInputLayout] if this view is a child of a [TextInputLayout],
     * otherwise sets the hint on this [android.widget.EditText].
     *
     * @param hint The string resource to use as the hint.
     */
    fun setFieldHint(hint: Int) = setFieldHint(context.getString(hint))

    /**
     * Sets the hint on the [TextInputLayout] if this view is a child of a [TextInputLayout],
     * otherwise sets the hint on this [android.widget.EditText].
     *
     * @param hint The string value to use as the hint.
     */
    fun setFieldHint(hint: String) = textInputLayoutParent?.let { it.hint = hint } ?: setHint(hint)

    /**
     * Request focus for the next view.
     */
    @SuppressLint("WrongConstant")
    fun focusNextView(): View? {
        if (imeActionId == EditorInfo.IME_ACTION_GO) {
            return null
        }

        val next = try {
            focusSearch(View.FOCUS_FORWARD)
        } catch (e: IllegalArgumentException) {
            // View.FOCUS_FORWARD results in a crash in some versions of Android
            // https://github.com/braintree/braintree_android/issues/20
            focusSearch(View.FOCUS_DOWN)
        }

        return if (next?.requestFocus() == true) next else null
    }

    /**
     * Controls the error state of this [ErrorEditText] and sets a visual indication that the
     * [ErrorEditText] contains an error.
     *
     * @param errorMessage the error message to display to the user. `null` will remove any error message displayed.
     */
    fun setError(errorMessage: String?) {
        isError = !errorMessage.isNullOrEmpty()

        textInputLayoutParent
            ?.let {
                it.isErrorEnabled = isError
                it.error = errorMessage
            }

        if (mErrorAnimator != null && isError) {
            startAnimation(mErrorAnimator)
            VibrationHelper.vibrate(context, 10)
        }
    }

    /**
     * Check if the [ErrorEditText] is valid and set the correct error state and visual
     * indication on it.
     */
    fun validate() = if (isValid() || isOptional) setError(null) else setError(getErrorMessage())

    /**
     * Attempt to close the soft keyboard. Will have no effect if the keyboard is not open.
     */
    fun closeSoftKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setupRTL() {
        if (SDK_INT >= JELLY_BEAN_MR1) {
            if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                textDirection = View.TEXT_DIRECTION_LTR
                gravity = Gravity.END
            }
        }
    }
}

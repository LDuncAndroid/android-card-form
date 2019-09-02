package com.braintreepayments.cardform.view

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.braintreepayments.cardform.R
import com.braintreepayments.cardform.utils.DateValidator
import com.braintreepayments.cardform.utils.ExpirationDateDialogTheme
import com.braintreepayments.cardform.utils.ExpirationDateItemAdapter
import kotlinx.android.synthetic.main.bt_expiration_date_sheet.*
import java.util.*

class ExpirationDateDialog : Dialog, DialogInterface.OnShowListener {

    private val mYears = ArrayList<String>()

    private var mAnimationDelay: Int = 0
    private var mEditText: ExpirationDateEditText? = null
    private lateinit var mTheme: ExpirationDateDialogTheme
    private var mHasSelectedMonth: Boolean = false
    private var mHasSelectedYear: Boolean = false
    private var mSelectedMonth = -1
    private var mSelectedYear = -1

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bt_expiration_date_sheet)

        mAnimationDelay = context.resources.getInteger(android.R.integer.config_shortAnimTime)

        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setOnShowListener(this)

        (0 until DateValidator.MAXIMUM_VALID_YEAR_DIFFERENCE).forEach { i ->
            mYears.add((CURRENT_YEAR + i).toString())
        }

        val monthAdapter = ExpirationDateItemAdapter(context, theme = mTheme, objects = MONTHS)
        val yearAdapter = ExpirationDateItemAdapter(context, theme = mTheme, objects = mYears)

        bt_expiration_month_grid_view.adapter = monthAdapter
        monthAdapter.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            mHasSelectedMonth = true
            mSelectedMonth = position
            setExpirationDate()

            if (Integer.parseInt(MONTHS[position]) < CURRENT_MONTH) {
                yearAdapter.setDisabled(listOf(0))
            } else {
                yearAdapter.setDisabled(ArrayList())
            }
        })

        bt_expiration_year_grid_view.adapter = yearAdapter
        yearAdapter.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            mHasSelectedYear = true
            mSelectedYear = position
            setExpirationDate()

            when (Integer.parseInt(mYears[position])) {
                CURRENT_YEAR -> MONTHS.indices.filter { Integer.parseInt(MONTHS[it]) < CURRENT_MONTH }
                else -> ArrayList()
            }.let { monthAdapter.setDisabled(it) }
        })

        mSelectedMonth = MONTHS.indexOf(mEditText?.month)
        if (mSelectedMonth >= 0) monthAdapter.setSelected(mSelectedMonth)

        mSelectedYear = mYears.indexOf(mEditText?.year)
        if (mSelectedYear >= 0) yearAdapter.setSelected(mSelectedYear)
    }

    override fun show() {
        Handler().postDelayed({
            if (mEditText?.isFocused == true && ownerActivity?.isFinishing == false) {
                super.show()
            }
        }, mAnimationDelay.toLong())
    }

    override fun onShow(dialog: DialogInterface) {
        if (mSelectedYear > 0) {
            bt_expiration_year_grid_view.smoothScrollToPosition(mSelectedYear)
        }

        mHasSelectedMonth = false
        mHasSelectedYear = false
    }

    private fun setExpirationDate() {
        var expirationDate: String = if (mSelectedMonth == -1) "  " else MONTHS[mSelectedMonth]

        expirationDate += if (mSelectedYear == -1) "    " else mYears[mSelectedYear]

        mEditText?.setText(expirationDate)

        if (mHasSelectedMonth && mHasSelectedYear) {
            val focusedView = mEditText?.focusNextView()
            if (focusedView != null) {
                Handler().postDelayed({
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .showSoftInput(focusedView, 0)
                }, mAnimationDelay.toLong())
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val closeOnTouch = (event.action == MotionEvent.ACTION_DOWN && isOutOfBounds(event)
            && window?.peekDecorView() != null)

        if (isShowing && closeOnTouch) {
            val rootView = ownerActivity?.window?.decorView?.rootView
            val selectedView = if (rootView is ViewGroup) {
                findViewAt(rootView, event.rawX.toInt(), event.rawY.toInt())
            } else null

            if (selectedView != null && selectedView !== mEditText) {
                dismiss()

                if (selectedView is EditText) {
                    selectedView.requestFocus()
                    Handler().postDelayed({
                        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .showSoftInput(selectedView, 0)
                    }, mAnimationDelay.toLong())
                } else if (selectedView is Button) {
                    selectedView.callOnClick()
                }
            } else if (selectedView == null) {
                dismiss()
            }

            return true
        }

        return false
    }

    private fun findViewAt(viewGroup: ViewGroup, x: Int, y: Int): View? {
        (0 until viewGroup.childCount)
            .map { viewGroup.getChildAt(it) }
            .forEach { child ->
                if (child is ViewGroup) {
                    findViewAt(child, x, y)?.takeIf { it.isShown }.let { return it }
                } else {
                    val location = IntArray(2)
                    child.getLocationOnScreen(location)
                    val rect = Rect(location[0], location[1], location[0] + child.width, location[1] + child.height)
                    if (rect.contains(x, y)) {
                        return child
                    }
                }
            }

        return null
    }

    /**
     * Based on Window#isOutOfBounds
     */
    private fun isOutOfBounds(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()
        val slop = ViewConfiguration.get(context).scaledWindowTouchSlop

        return window?.decorView
            ?.let {
                (x < -slop || y < -slop
                    || x > it.width + slop
                    || y > it.height + slop)
            } ?: false
    }

    companion object {

        private val MONTHS = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
        private val CURRENT_MONTH = Calendar.getInstance().get(Calendar.MONTH) + 1 // months are 0 indexed
        private val CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR)

        fun create(activity: Activity, editText: ExpirationDateEditText): ExpirationDateDialog {
            val theme = ExpirationDateDialogTheme.detectTheme(activity)
            val dialog = if (theme === ExpirationDateDialogTheme.LIGHT) {
                ExpirationDateDialog(activity, R.style.bt_expiration_date_dialog_light)
            } else {
                ExpirationDateDialog(activity, R.style.bt_expiration_date_dialog_dark)
            }

            dialog.ownerActivity = activity
            dialog.mTheme = theme
            dialog.mEditText = editText

            return dialog
        }
    }
}

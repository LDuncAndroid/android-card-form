package com.braintreepayments.cardform

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.braintreepayments.cardform.utils.ColorUtils
import com.braintreepayments.cardform.view.CardForm
import io.card.payment.CardIOActivity

class CardScanningFragment : Fragment() {

    private var mCardForm: CardForm? = null

    fun setCardForm(cardForm: CardForm) {
        mCardForm = cardForm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        if (savedInstanceState?.getBoolean("resuming") == true) {
            return
        }

        val scanIntent = Intent(activity, CardIOActivity::class.java)
            .putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true)
            .putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false)
            .putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true)
            .putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true)
            .putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, true)
            .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false)
            .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
            .putExtra(CardIOActivity.EXTRA_GUIDE_COLOR,
                ColorUtils.getColor(requireActivity(), "colorAccent", R.color.bt_blue))

        startActivityForResult(scanIntent, CARD_IO_REQUEST_CODE)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("resuming", false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CARD_IO_REQUEST_CODE) {
            mCardForm?.handleCardIOResponse(resultCode, data)

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commit()
        }
    }

    companion object {

        private const val CARD_IO_REQUEST_CODE = 12398
        const val TAG = "com.braintreepayments.cardform.CardScanningFragment"

        fun requestScan(activity: AppCompatActivity, cardForm: CardForm): CardScanningFragment {
            var fragment = activity.supportFragmentManager
                .findFragmentByTag(TAG) as CardScanningFragment?

            if (fragment != null) {
                activity.supportFragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit()
            }

            fragment = CardScanningFragment()
            fragment.mCardForm = cardForm

            activity.supportFragmentManager
                .beginTransaction()
                .add(fragment, TAG)
                .commit()

            return fragment
        }
    }
}

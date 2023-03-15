package com.asfoundation.wallet.verification.ui.credit_card.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.error_verification_layout.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationErrorFragment : BasePageViewFragment(), VerificationErrorView {

  @Inject
  lateinit var presenter: VerificationErrorPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.error_verification_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(errorType: VerificationCodeResult.ErrorType, amount: String,
                              symbol: String) {
    when (errorType) {
      VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS -> {
        error_message.visibility = View.GONE
        error_title.visibility = View.GONE
        contact_us.visibility = View.INVISIBLE
        layout_support_logo.visibility = View.INVISIBLE
        layout_support_icn.visibility = View.INVISIBLE

        error_message_2.visibility = View.VISIBLE
        error_title_2.visibility = View.VISIBLE
        try_again.visibility = View.GONE
        attempts_group.visibility = View.VISIBLE

        val amountWithCurrency =
            "$symbol${formatter.formatCurrency(amount, WalletCurrency.FIAT)}"
        error_title_2.text =
            getString(R.string.card_verification_no_attempts_title, amountWithCurrency)
      }
      VerificationCodeResult.ErrorType.WRONG_CODE,
      VerificationCodeResult.ErrorType.OTHER -> {
        error_message.visibility = View.VISIBLE
        error_title.visibility = View.VISIBLE
        contact_us.visibility = View.VISIBLE
        layout_support_logo.visibility = View.VISIBLE
        layout_support_icn.visibility = View.VISIBLE

        error_message_2.visibility = View.GONE
        error_title_2.visibility = View.GONE
        try_again.visibility = View.VISIBLE
        attempts_group.visibility = View.GONE
      }
    }
  }

  override fun getMaybeLaterClicks() = RxView.clicks(maybe_later)

  override fun getTryAgainClicks() = RxView.clicks(try_again)

  override fun getTryAgainAttemptsClicks() = RxView.clicks(try_again_attempts)

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  companion object {

    internal const val ERROR_TYPE = "error_type"
    internal const val AMOUNT = "amount"
    internal const val SYMBOL = "symbol"

    @JvmStatic
    fun newInstance(errorType: VerificationCodeResult.ErrorType, verificationAmount: String?,
                    symbol: String?): VerificationErrorFragment {
      return VerificationErrorFragment().apply {
        arguments = Bundle().apply {
          putInt(ERROR_TYPE, errorType.ordinal)
          putString(AMOUNT, verificationAmount)
          putString(SYMBOL, symbol)
        }
      }
    }
  }
}
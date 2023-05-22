package com.asfoundation.wallet.verification.ui.credit_card.error

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.databinding.ErrorVerificationLayoutBinding
import com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VerificationErrorFragment : com.wallet.appcoins.core.legacy_base.legacy.BasePageViewFragment(null), VerificationErrorView {

  @Inject
  lateinit var presenter: VerificationErrorPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private val views by viewBinding(ErrorVerificationLayoutBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View = ErrorVerificationLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun initializeView(errorType: VerificationCodeResult.ErrorType, amount: String,
                              symbol: String) {
    when (errorType) {
      VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS -> {
        views.errorMessage.visibility = View.GONE
        views.errorTitle.visibility = View.GONE
        views.contactUs.visibility = View.INVISIBLE
        views.layoutSupportLogo.visibility = View.INVISIBLE
        views.layoutSupportIcn.visibility = View.INVISIBLE

        views.errorMessage2.visibility = View.VISIBLE
        views.errorTitle2.visibility = View.VISIBLE
        views.tryAgain.visibility = View.GONE
        views.attemptsGroup.visibility = View.VISIBLE

        val amountWithCurrency =
            "$symbol${formatter.formatCurrency(amount, WalletCurrency.FIAT)}"
        views.errorTitle2.text =
            getString(R.string.card_verification_no_attempts_title, amountWithCurrency)
      }
      VerificationCodeResult.ErrorType.WRONG_CODE,
      VerificationCodeResult.ErrorType.OTHER -> {
        views.errorMessage.visibility = View.VISIBLE
        views.errorTitle.visibility = View.VISIBLE
        views.contactUs.visibility = View.VISIBLE
        views.layoutSupportLogo.visibility = View.VISIBLE
        views.layoutSupportIcn.visibility = View.VISIBLE

        views.errorMessage2.visibility = View.GONE
        views.errorTitle2.visibility = View.GONE
        views.tryAgain.visibility = View.VISIBLE
        views.attemptsGroup.visibility = View.GONE
      }
    }
  }

  override fun getMaybeLaterClicks() = RxView.clicks(views.maybeLater)

  override fun getTryAgainClicks() = RxView.clicks(views.tryAgain)

  override fun getTryAgainAttemptsClicks() = RxView.clicks(views.tryAgainAttempts)

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
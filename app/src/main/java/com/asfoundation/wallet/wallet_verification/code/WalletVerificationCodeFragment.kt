package com.asfoundation.wallet.wallet_verification.code

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.wallet_verification.WalletVerificationActivityView
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_verification_code.*
import javax.inject.Inject

class WalletVerificationCodeFragment : DaggerFragment(), WalletVerificationCodeView {

  @Inject
  lateinit var presenter: WalletVerificationCodePresenter

  @Inject
  lateinit var data: VerificationCodeData

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private lateinit var activityView: WalletVerificationActivityView

  companion object {

    internal const val CURRENCY_KEY = "currency"
    internal const val AMOUNT_KEY = "amount"
    internal const val DIGITS_KEY = "digits"
    internal const val FORMAT_KEY = "format"
    internal const val PERIOD_KEY = "period"
    internal const val DATE_KEY = "period"


    @JvmStatic
    fun newInstance(currency: String, value: String, digits: Int, format: String,
                    period: String, date: Long): WalletVerificationCodeFragment {
      return WalletVerificationCodeFragment().apply {
        arguments = Bundle().apply {
          putString(CURRENCY_KEY, currency)
          putString(AMOUNT_KEY, value)
          putString(FORMAT_KEY, format)
          putString(PERIOD_KEY, period)
          putLong(DATE_KEY, date)
          putInt(DIGITS_KEY, digits)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is WalletVerificationActivityView) {
      throw IllegalStateException(
          "Wallet Verification Code must be attached to Wallet Verification Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verification_code, container, false)
  }


  override fun showLoading() {
    TODO("Not yet implemented")
  }

  override fun hideLoading() {
    TODO("Not yet implemented")
  }

  override fun showSuccess() {
    TODO("Not yet implemented")
  }

  override fun showGenericError() {
    TODO("Not yet implemented")
  }

  override fun showNetworkError() {
    TODO("Not yet implemented")
  }

  override fun showSpecificError(stringRes: Int) {
    TODO("Not yet implemented")
  }

  override fun updateUi() {
    val amount = formatter.formatCurrency(data.amount, WalletCurrency.FIAT)
    val amountWithCurrency = "${data.currency} $amount"

    /* trans_date_value.text = data.date
     description_value.text = data.format
     amount_value.text = amountWithCurrency
     code_disclaimer.text = getString(R.string.card_verification_code_enter_body, amountWithCurrency)*/
  }

  override fun getMaybeLaterClicks() = RxView.clicks(maybe_later)

  override fun getConfirmClicks(): Observable<String> = RxView.clicks(confirm)
      .map { code.text.toString() }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

}
package com.asfoundation.wallet.verification.ui.credit_card.code

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.appcoins.wallet.core.utils.common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.common.Duration
import com.appcoins.wallet.core.utils.common.KeyboardUtils
import com.appcoins.wallet.core.utils.common.WalletCurrency
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.error_top_up_layout.*
import kotlinx.android.synthetic.main.fragment_verification_code.*
import kotlinx.android.synthetic.main.layout_verify_example.view.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationCodeFragment : BasePageViewFragment(), VerificationCodeView {

  @Inject
  lateinit var presenter: VerificationCodePresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private lateinit var activityView: VerificationCreditCardActivityView

  companion object {

    internal const val LOADED_KEY = "loaded"
    internal const val CURRENCY_KEY = "currency"
    internal const val SYMBOL_KEY = "symbol"
    internal const val AMOUNT_KEY = "amount"
    internal const val DIGITS_KEY = "digits"
    internal const val FORMAT_KEY = "format"
    internal const val PERIOD_KEY = "period"
    internal const val DATE_KEY = "date"
    private const val CODE_KEY = "code"

    @JvmStatic
    fun newInstance(currency: String, symbol: String, value: String, digits: Int, format: String,
                    period: String, date: Long): VerificationCodeFragment {
      return VerificationCodeFragment().apply {
        arguments = Bundle().apply {
          putBoolean(LOADED_KEY, true)
          putString(CURRENCY_KEY, currency)
          putString(SYMBOL_KEY, symbol)
          putString(AMOUNT_KEY, value)
          putString(FORMAT_KEY, format)
          putString(PERIOD_KEY, period)
          putLong(DATE_KEY, date)
          putInt(DIGITS_KEY, digits)
        }
      }
    }

    @JvmStatic
    fun newInstance(): VerificationCodeFragment {
      return VerificationCodeFragment().apply {
        arguments = Bundle().apply {
          putBoolean(LOADED_KEY, false)
        }
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is VerificationCreditCardActivityView) {
      throw IllegalStateException(
          "Wallet Verification Code must be attached to Wallet Verification Activity")
    }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verification_code, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.apply { putString(CODE_KEY, code?.let { code.text.toString() } ?: "") }
  }


  override fun setupUi(currency: String, symbol: String, amount: String, digits: Int,
                       format: String, period: String, date: Long, isWalletVerified: Boolean,
                       savedInstance: Bundle?) {
    val amountFormat = formatter.formatCurrency(amount, WalletCurrency.FIAT)
    val amountWithCurrency = "${symbol}$amountFormat"
    val amountWithCurrencyAndSign = "${symbol}-$amountFormat"

    val dateFormat = convertToDate(date)
    val duration = Duration.parse(period)

    val periodInDays = duration.toDays()
    val periodInHours = duration.toHours()

    val periodFormat = String.format(getString(R.string.card_verification_code_example_code),
        periodInDays.toString())
    val codeTitle = String.format(getString(R.string.card_verification_code_enter_title),
        digits.toString())
    val codeDisclaimer = if (periodInDays > 0) {
      resources.getQuantityString(R.plurals.card_verification_code_enter_days_body,
          periodInDays.toInt(), amountWithCurrency, periodInDays.toString())
    } else {
      resources.getQuantityString(R.plurals.card_verification_code_enter_hours_body,
          periodInHours.toInt(), amountWithCurrency, periodInHours.toString())
    }

    code.setEms(digits)
    code.filters = arrayOf(InputFilter.LengthFilter(digits))

    savedInstance?.let {
      val codeString = it.getString(CODE_KEY, "")
      code.setText(codeString)
      confirm.isEnabled = codeString?.length == digits
    }

    layout_example.trans_date_value.text = dateFormat
    layout_example.description_value.text = format
    layout_example.amount_value.text = amountWithCurrencyAndSign
    layout_example.arrow_desc.text = periodFormat
    code_title.text = codeTitle
    code_disclaimer.text = codeDisclaimer
    success_message.text =
        if (isWalletVerified) getString(R.string.verification_settings_card_verified_title)
        else getString(R.string.verification_settings_verified_title)

    success_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) = Unit
      override fun onAnimationEnd(animation: Animator?) = presenter.onAnimationEnd()
      override fun onAnimationCancel(animation: Animator?) = Unit
      override fun onAnimationStart(animation: Animator?) = Unit
    })
    code.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        hideWrongCodeError()
        confirm.isEnabled = s?.length == digits
      }
    })
  }

  override fun showWrongCodeError() {
    code_title.visibility = View.VISIBLE
    code.visibility = View.VISIBLE
    code_disclaimer.visibility = View.VISIBLE
    wrong_code_error.visibility = View.VISIBLE

    code.setBackgroundResource(R.drawable.background_edittext_error)
  }

  fun hideWrongCodeError() {
    wrong_code_error.visibility = View.GONE
    code.setBackgroundResource(R.drawable.background_edittext)
  }

  override fun showLoading() {
    code_title.visibility = View.INVISIBLE
    code.visibility = View.INVISIBLE
    wrong_code_error.visibility = View.INVISIBLE
    code_disclaimer.visibility = View.INVISIBLE
    change_card_button.visibility = View.INVISIBLE
    progress_bar.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    content_container.visibility = View.VISIBLE
    code_title.visibility = View.VISIBLE
    code.visibility = View.VISIBLE
    code_disclaimer.visibility = View.VISIBLE
    change_card_button.visibility = View.VISIBLE
    progress_bar.visibility = View.GONE
    activityView.hideLoading()
  }

  override fun showVerificationCode() {
    content_container.visibility = View.VISIBLE
    no_network.visibility = View.GONE
  }

  override fun showSuccess() {
    no_network.visibility = View.GONE
    content_container.visibility = View.GONE
    progress_bar.visibility = View.GONE

    success_animation.visibility = View.VISIBLE
    success_message.visibility = View.VISIBLE
    success_animation.playAnimation()
  }

  override fun showNetworkError() {
    unlockRotation()
    progress_bar.visibility = View.GONE
    content_container.visibility = View.GONE
    no_network.visibility = View.VISIBLE
  }

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  override fun lockRotation() {
    activityView.lockRotation()
  }

  override fun unlockRotation() {
    activityView.unlockRotation()
  }

  override fun getMaybeLaterClicks() = RxView.clicks(maybe_later)

  override fun getConfirmClicks(): Observable<String> = RxView.clicks(confirm)
      .map { code.text.toString() }

  override fun getChangeCardClicks() = RxView.clicks(change_card_button)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))
  }

  override fun retryClick() = RxView.clicks(retry_button)

  private fun convertToDate(ts: Long): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
        .apply { timeInMillis = ts }
    return DateFormat.format("dd/MM/yyyy", cal.time)
        .toString()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
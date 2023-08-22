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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.jvm_common.Duration
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentVerificationCodeBinding
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationCodeFragment : BasePageViewFragment(), VerificationCodeView {

  @Inject
  lateinit var presenter: VerificationCodePresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  private lateinit var activityView: VerificationCreditCardActivityView

  private val views by viewBinding(FragmentVerificationCodeBinding::bind)

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
                            savedInstanceState: Bundle?): View = FragmentVerificationCodeBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.apply { putString(CODE_KEY, views.code.toString()) }
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

    views.code.setEms(digits)
    views.code.filters = arrayOf(InputFilter.LengthFilter(digits))

    savedInstance?.let {
      val codeString = it.getString(CODE_KEY, "")
      views.code.setText(codeString)
      views.confirm.isEnabled = codeString?.length == digits
    }

    views.layoutExample.transDateValue.text = dateFormat
    views.layoutExample.descriptionValue.text = format
    views.layoutExample.amountValue.text = amountWithCurrencyAndSign
    views.layoutExample.arrowDesc.text = periodFormat
    views.codeTitle.text = codeTitle
    views.codeDisclaimer.text = codeDisclaimer
    views.successMessage.text =
        if (isWalletVerified) getString(R.string.verification_settings_card_verified_title)
        else getString(R.string.verification_settings_verified_title)

    views.successAnimation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator) = Unit
      override fun onAnimationEnd(animation: Animator) = presenter.onAnimationEnd()
      override fun onAnimationCancel(animation: Animator) = Unit
      override fun onAnimationStart(animation: Animator) = Unit
    })
    views.code.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(s: Editable?) = Unit
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        hideWrongCodeError()
        views.confirm.isEnabled = s?.length == digits
      }
    })
  }

  override fun showWrongCodeError() {
    views.codeTitle.visibility = View.VISIBLE
    views.code.visibility = View.VISIBLE
    views.codeDisclaimer.visibility = View.VISIBLE
    views.wrongCodeError.visibility = View.VISIBLE

    views.code.setBackgroundResource(R.drawable.background_edittext_error)
  }

  fun hideWrongCodeError() {
    views.wrongCodeError.visibility = View.GONE
    views.code.setBackgroundResource(R.drawable.background_edittext)
  }

  override fun showLoading() {
    views.codeTitle.visibility = View.INVISIBLE
    views.code.visibility = View.INVISIBLE
    views.wrongCodeError.visibility = View.INVISIBLE
    views.codeDisclaimer.visibility = View.INVISIBLE
    views.changeCardButton.visibility = View.INVISIBLE
    views.progressBar.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    views.contentContainer.visibility = View.VISIBLE
    views.codeTitle.visibility = View.VISIBLE
    views.code.visibility = View.VISIBLE
    views.codeDisclaimer.visibility = View.VISIBLE
    views.changeCardButton.visibility = View.VISIBLE
    views.progressBar.visibility = View.GONE
    activityView.hideLoading()
  }

  override fun showVerificationCode() {
    views.contentContainer.visibility = View.VISIBLE
    views.noNetwork.root.visibility = View.GONE
  }

  override fun showSuccess() {
    views.noNetwork.root.visibility = View.GONE
    views.contentContainer.visibility = View.GONE
    views.progressBar.visibility = View.GONE

    views.successAnimation.visibility = View.VISIBLE
    views.successMessage.visibility = View.VISIBLE
    views.successAnimation.playAnimation()
  }

  override fun showNetworkError() {
    unlockRotation()
    views.progressBar.visibility = View.GONE
    views.contentContainer.visibility = View.GONE
    views.noNetwork.root.visibility = View.VISIBLE
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

  override fun getMaybeLaterClicks() = RxView.clicks(views.maybeLater)

  override fun getConfirmClicks(): Observable<String> = RxView.clicks(views.confirm)
      .map { views.code.text.toString() }

  override fun getChangeCardClicks() = RxView.clicks(views.changeCardButton)

  override fun retryClick() = RxView.clicks(views.noNetwork.retryButton)

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
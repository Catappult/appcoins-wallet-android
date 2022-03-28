package com.asfoundation.wallet.verification.ui.credit_card.intro

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.core.api.Environment
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.adyen.VerificationPaymentModel
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.verification.ui.credit_card.VerificationCreditCardActivityView
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.error_top_up_layout.*
import kotlinx.android.synthetic.main.error_top_up_layout.view.*
import kotlinx.android.synthetic.main.fragment_verification_intro.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationIntroFragment : BasePageViewFragment(), VerificationIntroView {

  companion object {
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"

    @JvmStatic
    fun newInstance() = VerificationIntroFragment()
  }

  @Inject
  lateinit var presenter: VerificationIntroPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var adyenEnvironment: Environment

  private lateinit var activityView: VerificationCreditCardActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private lateinit var adyenCardImageLayout: RoundCornerImageView
  private lateinit var adyenSaveDetailsSwitch: SwitchCompat

  private var isStored = false
  private var paymentDataSubject: BehaviorSubject<AdyenCardWrapper> = BehaviorSubject.create()

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is VerificationCreditCardActivityView) {
      throw IllegalStateException(
        "Wallet Verification Intro must be attached to Wallet Verification Activity"
      )
    }
    activityView = context
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_verification_intro, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSavedInstance(outState)
    if (this::adyenCardNumberLayout.isInitialized) {
      outState.apply {
        putString(CARD_NUMBER_KEY, adyenCardNumberLayout.editText?.text.toString())
        putString(EXPIRY_DATE_KEY, adyenExpiryDateLayout.editText?.text.toString())
        putString(CVV_KEY, adyenSecurityCodeLayout.editText?.text.toString())
        putBoolean(SAVE_DETAILS_KEY, adyenSaveDetailsSwitch.isChecked)
      }
    }
  }

  private fun setupUi() {
    setupAdyenLayouts()
    setupCardConfiguration()
  }

  private fun setupAdyenLayouts() {
    adyenCardNumberLayout = adyen_card_form.findViewById(R.id.textInputLayout_cardNumber)
    adyenExpiryDateLayout = adyen_card_form.findViewById(R.id.textInputLayout_expiryDate)
    adyenSecurityCodeLayout = adyen_card_form.findViewById(R.id.textInputLayout_securityCode)
    adyenCardImageLayout = adyen_card_form.findViewById(R.id.cardBrandLogo_imageView_primary)
    adyenSaveDetailsSwitch = adyen_card_form.findViewById(R.id.switch_storePaymentMethod)

    adyenCardNumberLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenExpiryDateLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenSecurityCodeLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

    adyenSaveDetailsSwitch.run {

      val params: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
      params.topMargin = 2

      layoutParams = params
      isChecked = true
      textSize = 14f
      text = getString(R.string.dialog_credit_card_remember)
    }

    val height = resources.getDimensionPixelSize(R.dimen.adyen_text_input_layout_height)

    adyenCardNumberLayout.minimumHeight = height
    adyenExpiryDateLayout.minimumHeight = height
    adyenSecurityCodeLayout.minimumHeight = height
    adyenCardNumberLayout.errorIconDrawable = null
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration
      .Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()
  }

  @SuppressLint("StringFormatInvalid")
  override fun updateUi(verificationIntroModel: VerificationIntroModel) {
    val amount = formatter.formatCurrency(
      verificationIntroModel.verificationInfoModel.value,
      WalletCurrency.FIAT
    )
    description.text = getString(
      R.string.card_verification_charde_disclaimer,
      "${verificationIntroModel.verificationInfoModel.symbol}$amount"
    )
  }

  override fun finishCardConfiguration(
    paymentInfoModel: PaymentInfoModel, forget: Boolean, savedInstance: Bundle?
  ) {
    this.isStored = paymentInfoModel.isStored

    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentInfoModel, forget, savedInstance)
    setStoredPaymentInformation(isStored)
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    if (isStored) {
      adyenCardNumberLayout.visibility = View.GONE
      adyenExpiryDateLayout.visibility = View.GONE
      adyenCardImageLayout.visibility = View.GONE
      change_card_button.visibility = View.VISIBLE
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyenCardNumberLayout.visibility = View.VISIBLE
      adyenExpiryDateLayout.visibility = View.VISIBLE
      adyenCardImageLayout.visibility = View.VISIBLE
      change_card_button.visibility = View.GONE
    }
  }

  private fun prepareCardComponent(
    paymentInfoModel: PaymentInfoModel,
    forget: Boolean,
    savedInstanceState: Bundle?
  ) {
    val cardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this) {
      adyenSecurityCodeLayout.error = null
      if (it != null && it.isValid) {
        submit.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          paymentDataSubject.onNext(
            AdyenCardWrapper(
              paymentMethod, adyenSaveDetailsSwitch.isChecked, hasCvc,
              paymentInfoModel.supportedShopperInteractions
            )
          )
        }
      } else {
        submit.isEnabled = false
      }
    }
    if (forget) {
      clearFields()
    } else {
      getFieldValues(savedInstanceState)
    }
  }

  private fun getFieldValues(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      adyenCardNumberLayout.editText?.setText(it.getString(CARD_NUMBER_KEY, ""))
      adyenExpiryDateLayout.editText?.setText(it.getString(EXPIRY_DATE_KEY, ""))
      adyenSecurityCodeLayout.editText?.setText(it.getString(CVV_KEY, ""))
      adyenSaveDetailsSwitch.isChecked = it.getBoolean(SAVE_DETAILS_KEY, false)
      it.clear()
    }
  }

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text = adyenCardNumberLayout.editText?.text
      adyen_card_form_pre_selected_number?.visibility = View.VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardImageLayout.drawable)
    } else {
      adyen_card_form_pre_selected_number?.visibility = View.GONE
      payment_method_ic?.visibility = View.GONE
    }
  }

  private fun clearFields() {
    adyenCardNumberLayout.editText?.text = null
    adyenCardNumberLayout.editText?.isEnabled = true
    adyenExpiryDateLayout.editText?.text = null
    adyenExpiryDateLayout.editText?.isEnabled = true
    adyenSecurityCodeLayout.editText?.text = null
    adyenCardNumberLayout.requestFocus()
    adyenSecurityCodeLayout.error = null
  }


  override fun getCancelClicks() = RxView.clicks(cancel)

  override fun getSubmitClicks() = RxView.clicks(submit)

  override fun forgetCardClick(): Observable<Any> = RxView.clicks(change_card_button)

  override fun getTryAgainClicks() = RxView.clicks(try_again)

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))
  }

  override fun showLoading() {
    no_network.visibility = View.GONE
    fragment_adyen_error?.visibility = View.GONE
    content_container.visibility = View.GONE
    progress_bar.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    progress_bar.visibility = View.GONE
    content_container.visibility = View.VISIBLE
    activityView.hideLoading()
  }

  override fun showError(errorType: VerificationPaymentModel.ErrorType?) {
    if (errorType == VerificationPaymentModel.ErrorType.TOO_MANY_ATTEMPTS) {
      showSpecificError(R.string.verification_error_attempts_reached)
    } else {
      showSpecificError(R.string.unknown_error)
    }
  }

  override fun showGenericError() = showSpecificError(R.string.unknown_error)

  override fun showNetworkError() {
    unlockRotation()
    progress_bar.visibility = View.GONE
    content_container.visibility = View.GONE
    no_network.visibility = View.VISIBLE
  }

  override fun showSpecificError(stringRes: Int) {
    unlockRotation()
    progress_bar.visibility = View.GONE
    content_container.visibility = View.GONE

    val message = getString(stringRes)
    fragment_adyen_error?.error_message?.text = message
    fragment_adyen_error?.visibility = View.VISIBLE
  }

  override fun showCvvError() {
    unlockRotation()
    progress_bar.visibility = View.GONE
    submit.isEnabled = false
    if (isStored) {
      change_card_button.visibility = View.VISIBLE
    } else {
      change_card_button.visibility = View.INVISIBLE
    }
    content_container.visibility = View.VISIBLE
    adyenSecurityCodeLayout.error = getString(R.string.purchase_card_error_CVV)
  }

  override fun retrievePaymentData() = paymentDataSubject

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  override fun lockRotation() = activityView.lockRotation()

  override fun unlockRotation() = activityView.unlockRotation()

  override fun cancel() = activityView.cancel()

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}
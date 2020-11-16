package com.asfoundation.wallet.wallet_verification.intro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.Observer
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.ui.view.RoundCornerImageView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.wallet_verification.WalletVerificationActivityView
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.fragment_verification_intro.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import javax.inject.Inject

class WalletVerificationIntroFragment : DaggerFragment(), WalletVerificationIntroView {

  companion object {
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"

    @JvmStatic
    fun newInstance() = WalletVerificationIntroFragment()
  }

  @Inject
  lateinit var presenter: WalletVerificationIntroPresenter

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var adyenEnvironment: Environment

  private lateinit var activityView: WalletVerificationActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private lateinit var adyenCardImageLayout: RoundCornerImageView
  private lateinit var adyenSaveDetailsSwitch: SwitchCompat

  private var isStored = false
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)

    require(context is WalletVerificationActivityView) {
      throw IllegalStateException(
          "Wallet Verification Intro must be attached to Wallet Verification Activity")
    }
    activityView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_verification_intro, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present(savedInstanceState)
  }

  private fun setupUi() {
    setupAdyenLayouts()
    setupCardConfiguration()
  }

  private fun setupAdyenLayouts() {
    adyenCardNumberLayout = adyen_card_form.findViewById(R.id.textInputLayout_cardNumber)
    adyenExpiryDateLayout = adyen_card_form.findViewById(R.id.textInputLayout_expiryDate)
    adyenSecurityCodeLayout = adyen_card_form.findViewById(R.id.textInputLayout_securityCode)
    adyenCardImageLayout = adyen_card_form.findViewById(R.id.cardBrandLogo_imageView)
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
  }

  private fun setupCardConfiguration() {
    val cardConfigurationBuilder =
        CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)

    cardConfiguration = cardConfigurationBuilder.let {
      it.setEnvironment(adyenEnvironment)
      it.build()
    }
  }

  override fun updateUi(verificationIntroModel: VerificationIntroModel) {
    val amount = formatter.formatCurrency(verificationIntroModel.verificationInfoModel.value,
        WalletCurrency.FIAT)
    description.text = getString(R.string.card_verification_charde_disclaimer,
        "${verificationIntroModel.verificationInfoModel.currency} $amount")
  }

  override fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean, forget: Boolean, savedInstance: Bundle?) {
    this.isStored = isStored

    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentMethod, isStored, savedInstance)
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
      paymentMethodEntity: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      forget: Boolean,
      savedInstanceState: Bundle?) {
    if (forget) viewModelStore.clear()
    val cardComponent = CardComponent.PROVIDER.get(this, paymentMethodEntity, cardConfiguration)
    if (forget) clearFields()
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this, Observer {
      adyenSecurityCodeLayout.error = null
      if (it != null && it.isValid) {
        submit.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          val supportedShopperInteractions =
              if (paymentMethodEntity is StoredPaymentMethod) paymentMethodEntity.supportedShopperInteractions else emptyList()
          paymentDataSubject?.onNext(
              AdyenCardWrapper(paymentMethod, adyenSaveDetailsSwitch.isChecked, hasCvc,
                  supportedShopperInteractions))
        }
      } else {
        submit.isEnabled = false
      }
    })
    if (!forget) {
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

  override fun showLoading() {

  }

  override fun showGenericError() {

  }

  override fun cancel() {
    activityView.cancel()
  }

  override fun onDestroyView() {
    paymentDataSubject = null
    presenter.stop()
    super.onDestroyView()
  }

}
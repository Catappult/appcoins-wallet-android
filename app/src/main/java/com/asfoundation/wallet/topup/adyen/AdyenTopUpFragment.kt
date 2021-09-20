package com.asfoundation.wallet.topup.adyen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.ui.view.RoundCornerImageView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.appcoins.wallet.bdsbilling.Billing
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.*
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.address.BillingAddressTopUpFragment.Companion.BILLING_ADDRESS_MODEL
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.error_top_up_layout.*
import kotlinx.android.synthetic.main.error_top_up_layout.view.*
import kotlinx.android.synthetic.main.fragment_adyen_top_up.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import kotlinx.android.synthetic.main.support_error_layout.layout_support_icn
import kotlinx.android.synthetic.main.support_error_layout.layout_support_logo
import kotlinx.android.synthetic.main.view_purchase_bonus.view.*
import java.math.BigDecimal
import javax.inject.Inject

class AdyenTopUpFragment : DaggerFragment(), AdyenTopUpView {

  @Inject
  internal lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  internal lateinit var billing: Billing

  @Inject
  lateinit var adyenPaymentInteractor: AdyenPaymentInteractor

  @Inject
  lateinit var adyenEnvironment: Environment

  @Inject
  lateinit var topUpAnalytics: TopUpAnalytics

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var servicesErrorMapper: ServicesErrorCodeMapper

  @Inject
  lateinit var logger: Logger

  private lateinit var topUpView: TopUpActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private lateinit var navigator: TopUpNavigator
  private lateinit var presenter: AdyenTopUpPresenter

  private var adyenCardImageLayout: RoundCornerImageView? = null
  private var adyenSaveDetailsSwitch: SwitchCompat? = null
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<AdyenComponentResponseModel>? = null
  private var adyen3DSErrorSubject: PublishSubject<String>? = null
  private var billingAddressInput: PublishSubject<Boolean>? = null
  private var isStored = false
  private var billingAddressModel: BillingAddressModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    billingAddressInput = PublishSubject.create()

    presenter =
        AdyenTopUpPresenter(this, appPackage, AndroidSchedulers.mainThread(), Schedulers.io(),
            CompositeDisposable(), RedirectComponent.getReturnUrl(context!!), paymentType,
            data.transactionType, data.fiatValue, data.fiatCurrencyCode, data.appcValue,
            data.selectedCurrencyType, navigator, inAppPurchaseInteractor.billingMessagesMapper,
            adyenPaymentInteractor, data.bonusValue, data.fiatCurrencySymbol,
            AdyenErrorCodeMapper(), servicesErrorMapper, data.gamificationLevel, topUpAnalytics,
            formatter, logger)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
        context is TopUpActivityView) { "Payment Auth fragment must be attached to TopUp activity" }
    topUpView = context
    navigator = TopUpNavigator(requireFragmentManager(), (activity as UriNavigator?)!!, topUpView)

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_adyen_top_up, container, false)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    if (this::adyenCardNumberLayout.isInitialized) {
      outState.apply {
        putString(CARD_NUMBER_KEY, adyenCardNumberLayout.editText?.text.toString())
        putString(EXPIRY_DATE_KEY, adyenExpiryDateLayout.editText?.text.toString())
        putString(CVV_KEY, adyenSecurityCodeLayout.editText?.text.toString())
        putBoolean(SAVE_DETAILS_KEY, adyenSaveDetailsSwitch?.isChecked ?: false)
      }
    }
    presenter.onSaveInstanceState(outState)
  }

  override fun onResume() {
    super.onResume()
    hideKeyboard()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == BILLING_ADDRESS_REQUEST_CODE && resultCode == BILLING_ADDRESS_SUCCESS_CODE) {
      val billingAddressModel =
          data!!.getSerializableExtra(BILLING_ADDRESS_MODEL) as BillingAddressModel
      this.billingAddressModel = billingAddressModel
      billingAddressInput?.onNext(true)
    }
  }

  override fun setup3DSComponent() {
    adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(this)
    adyen3DS2Component.observe(this, Observer {
      paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
    })
    adyen3DS2Component.observeErrors(this, Observer {
      adyen3DSErrorSubject?.onNext(it.errorMessage)
    })
  }

  override fun showValues(value: String, currency: String) {
    main_value.visibility = VISIBLE
    val formattedValue = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    if (data.selectedCurrencyType == FIAT_CURRENCY) {
      main_value.setText(value)
      main_currency_code.text = currency
      converted_value.text = "$formattedValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      main_value.setText(formattedValue)
      main_currency_code.text = WalletCurrency.CREDITS.symbol
      converted_value.text = "$value $currency"
    }
  }

  override fun showLoading() {
    loading.visibility = VISIBLE
    credit_card_info_container.visibility = INVISIBLE
    button.isEnabled = false
  }

  override fun hideLoading() {
    button.visibility = VISIBLE
    loading.visibility = GONE
    button.isEnabled = false
    credit_card_info_container.visibility = VISIBLE
  }

  override fun showNetworkError() {
    topUpView.unlockRotation()
    loading.visibility = GONE
    no_network.visibility = VISIBLE
    retry_button.visibility = VISIBLE
    retry_animation.visibility = GONE
    top_up_container.visibility = GONE
  }

  override fun showInvalidCardError() {
    showSpecificError(R.string.purchase_error_invalid_credit_card)
  }

  override fun showSecurityValidationError() {
    showSpecificError(R.string.purchase_error_card_security_validation)
  }

  override fun showTimeoutError() {
    showSpecificError(R.string.purchase_error_transaction_timeout)
  }

  override fun showAlreadyProcessedError() {
    showSpecificError(R.string.purchase_error_card_already_in_progress)
  }

  override fun showPaymentError() {
    showSpecificError(R.string.purchase_error_payment_rejected)
  }

  override fun showRetryAnimation() {
    retry_button.visibility = INVISIBLE
    retry_animation.visibility = VISIBLE
  }

  override fun hideErrorViews() {
    no_network.visibility = GONE
    top_up_container.visibility = VISIBLE
    main_currency_code.visibility = VISIBLE
    main_value.visibility = VISIBLE
    top_separator_topup.visibility = VISIBLE
    converted_value.visibility = VISIBLE
    button.visibility = VISIBLE

    if (isStored) {
      change_card_button.visibility = VISIBLE
    } else {
      change_card_button.visibility = GONE
    }

    credit_card_info_container.visibility = VISIBLE
    fragment_adyen_error?.visibility = GONE

    topUpView.unlockRotation()
  }

  override fun showSpecificError(@StringRes stringRes: Int) {
    topUpView.unlockRotation()
    viewModelStore.clear()
    loading.visibility = GONE
    top_up_container.visibility = GONE

    val message = getString(stringRes)
    fragment_adyen_error?.error_message?.text = message
    fragment_adyen_error?.visibility = VISIBLE
  }

  override fun showVerificationError() {
    showSpecificError(R.string.purchase_error_verify_wallet)
    error_title?.visibility = VISIBLE
    error_verify_wallet_button?.visibility = VISIBLE
  }

  override fun showCvvError() {
    topUpView.unlockRotation()
    loading.visibility = GONE
    button.isEnabled = false
    if (isStored) {
      change_card_button.visibility = VISIBLE
    } else {
      change_card_button.visibility = INVISIBLE
    }
    credit_card_info_container.visibility = VISIBLE

    adyenSecurityCodeLayout.error = getString(R.string.purchase_card_error_CVV)
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getTryAgainClicks() = RxView.clicks(try_again)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(layout_support_logo), RxView.clicks(layout_support_icn))
  }

  override fun topUpButtonClicked() = RxView.clicks(button)

  override fun getVerificationClicks() =
      RxView.clicks(error_verify_wallet_button)

  override fun billingAddressInput(): Observable<Boolean> {
    return billingAddressInput!!
  }

  override fun retrieveBillingAddressData() = billingAddressModel

  override fun navigateToBillingAddress(fiatAmount: String, fiatCurrency: String) {
    topUpView.unlockRotation()
    topUpView.navigateToBillingAddress(data, fiatAmount, fiatCurrency, this,
        adyenSaveDetailsSwitch?.isChecked ?: true, isStored)
    // To allow back behaviour when address input is needed
    hideLoading()
    button.isEnabled = true
  }

  override fun finishCardConfiguration(
      paymentMethod: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      isStored: Boolean, forget: Boolean, savedInstanceState: Bundle?) {
    this.isStored = isStored
    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentMethod, forget, savedInstanceState)
    setStoredPaymentInformation(isStored)
  }

  override fun lockRotation() = topUpView.lockOrientation()

  private fun prepareCardComponent(
      paymentMethodEntity: com.adyen.checkout.base.model.paymentmethods.PaymentMethod,
      forget: Boolean,
      savedInstanceState: Bundle?) {
    if (forget) viewModelStore.clear()
    val cardComponent =
        CardComponent.PROVIDER.get(this, paymentMethodEntity, cardConfiguration)
    if (forget) clearFields()
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this, androidx.lifecycle.Observer {
      if (it != null && it.isValid) {
        button.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          val supportedShopperInteractions =
              if (paymentMethodEntity is StoredPaymentMethod) paymentMethodEntity.supportedShopperInteractions else emptyList()
          paymentDataSubject?.onNext(
              AdyenCardWrapper(paymentMethod, adyenSaveDetailsSwitch?.isChecked ?: false, hasCvc,
                  supportedShopperInteractions))
        }
      } else {
        button.isEnabled = false
      }
    })
    if (!forget) {
      getFieldValues(savedInstanceState)
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    if (isStored) {
      adyenCardNumberLayout.visibility = GONE
      adyenExpiryDateLayout.visibility = GONE
      adyenCardImageLayout?.visibility = GONE
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    } else {
      adyenCardNumberLayout.visibility = VISIBLE
      adyenExpiryDateLayout.visibility = VISIBLE
      adyenCardImageLayout?.visibility = VISIBLE
      change_card_button?.visibility = GONE
      change_card_button_pre_selected?.visibility = GONE
    }
  }

  override fun setupRedirectComponent() {
    redirectComponent = RedirectComponent.PROVIDER.get(this)
    redirectComponent.observe(this, Observer {
      paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
    })
  }

  override fun handle3DSAction(action: Action) {
    adyen3DS2Component.handleAction(activity!!, action)
  }

  override fun onAdyen3DSError(): Observable<String> = adyen3DSErrorSubject!!

  override fun showBonus(bonus: BigDecimal, currency: String) {
    buildBonusString(bonus, currency)
    bonus_layout.visibility = VISIBLE
    bonus_msg.visibility = VISIBLE
  }

  override fun showVerification() = topUpView.showVerification()

  private fun buildBonusString(bonus: BigDecimal, bonusCurrency: String) {
    val scaledBonus = bonus.max(BigDecimal("0.01"))
    val currency = "~$bonusCurrency".takeIf { bonus < BigDecimal("0.01") } ?: bonusCurrency
    bonus_layout.bonus_header_1.text = getString(R.string.topup_bonus_header_part_1)
    bonus_layout.bonus_value.text = getString(R.string.topup_bonus_header_part_2,
        currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT))
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun getPaymentDetails() = paymentDetailsSubject!!

  override fun forgetCardClick(): Observable<Any> {
    return if (change_card_button != null) RxView.clicks(change_card_button)
    else RxView.clicks(change_card_button_pre_selected)
  }

  override fun submitUriResult(uri: Uri) = redirectComponent.handleRedirectResponse(uri)

  override fun updateTopUpButton(valid: Boolean) {
    button.isEnabled = valid
  }

  override fun cancelPayment() = topUpView.cancelPayment()

  override fun setFinishingPurchase(newState: Boolean) = topUpView.setFinishingPurchase(newState)

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text =
          adyenCardNumberLayout.editText?.text
      adyen_card_form_pre_selected_number?.visibility = VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardImageLayout?.drawable)
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyen_card_form_pre_selected_number?.visibility = GONE
      payment_method_ic?.visibility = GONE
    }
  }

  private fun getFieldValues(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      adyenCardNumberLayout.editText?.setText(it.getString(CARD_NUMBER_KEY, ""))
      adyenExpiryDateLayout.editText?.setText(it.getString(EXPIRY_DATE_KEY, ""))
      adyenSecurityCodeLayout.editText?.setText(it.getString(CVV_KEY, ""))
      adyenSaveDetailsSwitch?.isChecked = it.getBoolean(SAVE_DETAILS_KEY, false)
      it.clear()
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

  override fun setupUi() {
    credit_card_info_container.visibility = INVISIBLE
    button.isEnabled = false

    if (paymentType == PaymentType.CARD.name) {
      button.setText(R.string.topup_home_button)

      setupAdyenLayouts()
      setupCardConfiguration()
    }

    topUpView.showToolbar()
    main_value.visibility = INVISIBLE
    button.visibility = VISIBLE
  }

  private fun setupCardConfiguration() {
    val cardConfigurationBuilder =
        CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)

    cardConfiguration = cardConfigurationBuilder.let {
      it.setEnvironment(adyenEnvironment)
      it.build()
    }
  }

  private fun setupAdyenLayouts() {
    adyenCardNumberLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_cardNumber)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_cardNumber)
    adyenExpiryDateLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_expiryDate)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_expiryDate)
    adyenSecurityCodeLayout =
        adyen_card_form_pre_selected?.findViewById(R.id.textInputLayout_securityCode)
            ?: adyen_card_form.findViewById(R.id.textInputLayout_securityCode)
    adyenCardImageLayout = adyen_card_form_pre_selected?.findViewById(R.id.cardBrandLogo_imageView)
        ?: adyen_card_form?.findViewById(R.id.cardBrandLogo_imageView)
    adyenSaveDetailsSwitch =
        adyen_card_form_pre_selected?.findViewById(R.id.switch_storePaymentMethod)
            ?: adyen_card_form?.findViewById(R.id.switch_storePaymentMethod)

    adyenSaveDetailsSwitch?.run {

      val params: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
      params.topMargin = 4
      params.bottomMargin = 0

      layoutParams = params
      isChecked = true
      textSize = 14f
      text = getString(R.string.dialog_credit_card_remember)
      setPadding(0, 0, 0, 0)
    }

    val height = resources.getDimensionPixelSize(R.dimen.adyen_text_input_layout_height)

    val view: View = adyen_card_form_pre_selected ?: adyen_card_form
    val layoutParams: ConstraintLayout.LayoutParams =
        view.layoutParams as ConstraintLayout.LayoutParams
    layoutParams.bottomMargin = 0
    layoutParams.marginStart = 0
    layoutParams.marginEnd = 0
    layoutParams.topMargin = 0
    view.layoutParams = layoutParams
    view.setPadding(0, 0, 24, 0)

    adyenCardNumberLayout.minimumHeight = height
    adyenCardNumberLayout.errorIconDrawable = null
    adyenExpiryDateLayout.minimumHeight = height
    adyenSecurityCodeLayout.minimumHeight = height
  }

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(it) }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    hideKeyboard()
    billingAddressInput = null
    paymentDataSubject = null
    paymentDetailsSubject = null
    adyen3DSErrorSubject = null
    super.onDestroy()
  }

  private val appPackage: String by lazy {
    if (activity != null) {
      activity!!.packageName
    } else {
      throw IllegalArgumentException("previous app package name not found")
    }
  }

  private val data: TopUpPaymentData by lazy {
    if (arguments!!.containsKey(PAYMENT_DATA)) {
      arguments!!.getSerializable(PAYMENT_DATA) as TopUpPaymentData
    } else {
      throw IllegalArgumentException("previous payment data not found")
    }
  }

  private val paymentType: String by lazy {
    if (arguments!!.containsKey(PAYMENT_TYPE)) {
      arguments!!.getString(PAYMENT_TYPE)!!
    } else {
      throw IllegalArgumentException("Payment Type not found")
    }
  }

  companion object {

    private const val PAYMENT_TYPE = "paymentType"
    private const val PAYMENT_DATA = "data"
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"

    fun newInstance(paymentType: PaymentType, data: TopUpPaymentData): AdyenTopUpFragment {
      val bundle = Bundle()
      val fragment = AdyenTopUpFragment()
      bundle.apply {
        putString(PAYMENT_TYPE, paymentType.name)
        putSerializable(PAYMENT_DATA, data)
        fragment.arguments = this
      }
      return fragment
    }
  }
}
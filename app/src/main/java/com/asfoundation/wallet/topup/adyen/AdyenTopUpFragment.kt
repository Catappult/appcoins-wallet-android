package com.asfoundation.wallet.topup.adyen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.FragmentAdyenTopUpBinding
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.adyen.*
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.topup.TopUpActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.topup.TopUpActivityView
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.topup.TopUpData.Companion.FIAT_CURRENCY
import com.asfoundation.wallet.topup.TopUpPaymentData
import com.asfoundation.wallet.topup.address.BillingAddressTopUpFragment.Companion.BILLING_ADDRESS_MODEL
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class AdyenTopUpFragment : BasePageViewFragment(), AdyenTopUpView {

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

  @Inject
  lateinit var navigator: TopUpNavigator

  private lateinit var topUpView: TopUpActivityView
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectConfiguration: RedirectConfiguration
  private lateinit var adyen3DS2Configuration: Adyen3DS2Configuration
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private lateinit var presenter: AdyenTopUpPresenter
  private lateinit var adyenCardView: AdyenCardView

  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<AdyenComponentResponseModel>? = null
  private var adyen3DSErrorSubject: PublishSubject<String>? = null
  private var billingAddressInput: PublishSubject<Boolean>? = null
  private var isStored = false
  private var billingAddressModel: BillingAddressModel? = null

  private var _binding: FragmentAdyenTopUpBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  // adyen_credit_card_pre_selected.xml


  // error_top_up_layout.xml
  private val error_message get() = binding.fragmentAdyenError.errorMessage
  private val error_verify_wallet_button get() = binding.fragmentAdyenError.errorVerifyWalletButton
  private val error_title get() = binding.fragmentAdyenError.errorTitle
  private val topup_error_animation get() = binding.fragmentAdyenError.topupErrorAnimation
  private val try_again get() = binding.fragmentAdyenError.tryAgain
  private val layout_support_icn get() = binding.fragmentAdyenError.layoutSupportIcn
  private val layout_support_logo get() = binding.fragmentAdyenError.layoutSupportLogo


  // fragment_adyen_top_up.xml
  private val top_up_container get() = binding.topUpContainer
  private val main_currency_code get() = binding.mainCurrencyCode
  private val main_value get() = binding.mainValue
  private val converted_value get() = binding.convertedValue
  private val top_separator_topup get() = binding.topSeparatorTopup
  private val credit_card_info_container get() = binding.creditCardInfoContainer
  private val change_card_button get() = binding.changeCardButton
  private val loading get() = binding.loading
  private val bonus_msg get() = binding.bonusMsg
  private val button get() = binding.button
  private val adyen_card_form get() = binding.adyenCardForm.root
  private val bonus_layout get() = binding.bonusLayout.root
  private val no_network get() = binding.noNetwork.root
  private val fragment_adyen_error get() = binding.fragmentAdyenError.root

  // no_network_retry_only_layout.xml
  private val retry_button get() = binding.noNetwork.retryButton
  private val retry_animation get() = binding.noNetwork.retryAnimation

  // selected_payment_method_cc.xml
  private val adyen_card_form_pre_selected get() = binding.adyenCardForm.adyenCardFormPreSelected
  private val payment_method_ic get() = binding.adyenCardForm.paymentMethodIc
  private val adyen_card_form_pre_selected_number get() = binding.adyenCardForm.adyenCardFormPreSelectedNumber

  // support_error_layout.xml


  // view_purchase_bonus.xml
  private val bonus_header_1 get() = binding.bonusLayout.bonusHeader1
  private val bonus_value get() = binding.bonusLayout.bonusValue

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    billingAddressInput = PublishSubject.create()

    presenter =
      AdyenTopUpPresenter(
        this, appPackage, AndroidSchedulers.mainThread(), Schedulers.io(),
        CompositeDisposable(), RedirectComponent.getReturnUrl(requireContext()), paymentType,
        data.transactionType, data.fiatValue, data.fiatCurrencyCode, data.appcValue,
        data.selectedCurrencyType, navigator, inAppPurchaseInteractor.billingMessagesMapper,
        adyenPaymentInteractor, data.bonusValue, data.fiatCurrencySymbol,
        AdyenErrorCodeMapper(), servicesErrorMapper, data.gamificationLevel, topUpAnalytics,
        formatter, logger
      )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(
      context is TopUpActivityView
    ) { "Payment Auth fragment must be attached to TopUp activity" }
    topUpView = context

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentAdyenTopUpBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
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
    activity?.application?.let {
      adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(this, it, adyen3DS2Configuration)
      adyen3DS2Component.observe(this) {
        paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
      }
      adyen3DS2Component.observeErrors(this) {
        adyen3DSErrorSubject?.onNext(it.errorMessage)
      }
    }
  }

  @SuppressLint("SetTextI18n")
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

  override fun showOutdatedCardError() {
    showSpecificError(R.string.purchase_card_error_re_insert)
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
    error_message?.text = message
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

    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
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
    topUpView.navigateToBillingAddress(
      data, fiatAmount, fiatCurrency, this, adyenCardView.cardSave, isStored
    )
    // To allow back behaviour when address input is needed
    hideLoading()
    button.isEnabled = true
  }

  override fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    this.isStored = paymentInfoModel.isStored
    prepareCardComponent(paymentInfoModel, forget)
    handleLayoutVisibility(isStored)
    setStoredPaymentInformation(isStored)
  }

  override fun lockRotation() = topUpView.lockOrientation()

  private fun prepareCardComponent(
    paymentInfoModel: PaymentInfoModel,
    forget: Boolean
  ) {
    if (forget) {
      adyenCardView.clear(this)
      unregisterProvider(Adyen3DS2Component::class.java.canonicalName)
      setup3DSComponent()
      unregisterProvider(RedirectComponent::class.java.canonicalName)
      setupRedirectComponent()
    }
    val cardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    adyen_card_form_pre_selected?.attach(cardComponent, this)
    cardComponent.observe(this) {
      if (it != null && it.isValid) {
        button.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          paymentDataSubject?.onNext(
            AdyenCardWrapper(
              paymentMethod,
              adyenCardView.cardSave,
              hasCvc,
              paymentInfoModel.supportedShopperInteractions
            )
          )
        }
      } else {
        button.isEnabled = false
      }
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    change_card_button?.visibility = if (isStored) VISIBLE else GONE
    //change_card_button_pre_selected?.visibility = if (isStored) VISIBLE else GONE
  }

  override fun setupRedirectComponent() {
    activity?.application?.let {
      redirectComponent = RedirectComponent.PROVIDER.get(this, it, redirectConfiguration)
      redirectComponent.observe(this) {
        paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
      }
    }
  }

  override fun handle3DSAction(action: Action) {
    adyen3DS2Component.handleAction(requireActivity(), action)
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
    bonus_header_1.text = getString(R.string.topup_bonus_header_part_1)
    bonus_value.text = getString(
      R.string.topup_bonus_header_part_2,
      currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    )
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun getPaymentDetails() = paymentDetailsSubject!!

  override fun forgetCardClick(): Observable<Any> {
    return RxView.clicks(change_card_button)
    //return if (change_card_button != null) RxView.clicks(change_card_button)
    //else RxView.clicks(change_card_button_pre_selected)
  }

  // TODO: Refactor this to pass the whole Intent.
  // TODO: Currently this relies on the fact that Adyen 4.4.0 internally uses only Intent.getData().
  override fun submitUriResult(uri: Uri) = redirectComponent.handleIntent(Intent("", uri))

  override fun updateTopUpButton(valid: Boolean) {
    button.isEnabled = valid
  }

  override fun cancelPayment() = topUpView.cancelPayment()

  override fun setFinishingPurchase(newState: Boolean) = topUpView.setFinishingPurchase(newState)

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text = adyenCardView.cardNumber
      adyen_card_form_pre_selected_number?.visibility = VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardView.cardImage)
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyen_card_form_pre_selected_number?.visibility = GONE
      payment_method_ic?.visibility = GONE
    }
  }

  override fun setupUi() {
    credit_card_info_container.visibility = INVISIBLE
    button.isEnabled = false

    if (paymentType == PaymentType.CARD.name) {
      button.setText(getString(R.string.topup_home_button))
      adyenCardView = AdyenCardView(adyen_card_form_pre_selected ?: adyen_card_form)
      setupCardConfiguration()
    }
    setupRedirectConfiguration()
    setupAdyen3DS2ConfigurationBuilder()
    main_value.visibility = INVISIBLE
    button.visibility = VISIBLE
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration
      .Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()
  }

  private fun setupRedirectConfiguration() {
    redirectConfiguration = RedirectConfiguration
      .Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()
  }

  private fun setupAdyen3DS2ConfigurationBuilder() {
    adyen3DS2Configuration = Adyen3DS2Configuration
      .Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment)
      .build()
  }

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(it) }
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
    _binding = null
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
      requireActivity().packageName
    } else {
      throw IllegalArgumentException("previous app package name not found")
    }
  }

  private val data: TopUpPaymentData by lazy {
    if (requireArguments().containsKey(PAYMENT_DATA)) {
      requireArguments().getSerializable(PAYMENT_DATA) as TopUpPaymentData
    } else {
      throw IllegalArgumentException("previous payment data not found")
    }
  }

  private val paymentType: String by lazy {
    if (requireArguments().containsKey(PAYMENT_TYPE)) {
      requireArguments().getString(PAYMENT_TYPE)!!
    } else {
      throw IllegalArgumentException("Payment Type not found")
    }
  }

  companion object {

    private const val PAYMENT_TYPE = "paymentType"
    private const val PAYMENT_DATA = "data"

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
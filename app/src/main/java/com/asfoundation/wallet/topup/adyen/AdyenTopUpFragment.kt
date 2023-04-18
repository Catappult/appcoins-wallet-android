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
import by.kirich1409.viewbindingdelegate.viewBinding
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

  private val binding by viewBinding(FragmentAdyenTopUpBinding::bind)

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
  ): View = FragmentAdyenTopUpBinding.inflate(inflater).root

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
    binding.mainValue.visibility = VISIBLE
    val formattedValue = formatter.formatCurrency(data.appcValue, WalletCurrency.CREDITS)
    if (data.selectedCurrencyType == FIAT_CURRENCY) {
      binding.mainValue.setText(value)
      binding.mainCurrencyCode.text = currency
      binding.convertedValue.text = "$formattedValue ${WalletCurrency.CREDITS.symbol}"
    } else {
      binding.mainValue.setText(formattedValue)
      binding.mainCurrencyCode.text = WalletCurrency.CREDITS.symbol
      binding.convertedValue.text = "$value $currency"
    }
  }

  override fun showLoading() {
    binding.loading.visibility = VISIBLE
    binding.creditCardInfoContainer.visibility = INVISIBLE
    binding.button.isEnabled = false
  }

  override fun hideLoading() {
    binding.button.visibility = VISIBLE
    binding.loading.visibility = GONE
    binding.button.isEnabled = false
    binding.creditCardInfoContainer.visibility = VISIBLE
  }

  override fun showNetworkError() {
    topUpView.unlockRotation()
    binding.loading.visibility = GONE
    binding.noNetwork.root.visibility = VISIBLE
    binding.noNetwork.retryButton.visibility = VISIBLE
    binding.noNetwork.retryAnimation.visibility = GONE
    binding.topUpContainer.visibility = GONE
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
    binding.noNetwork.retryButton.visibility = INVISIBLE
    binding.noNetwork.retryAnimation.visibility = VISIBLE
  }

  override fun hideErrorViews() {
    binding.noNetwork.root.visibility = GONE
    binding.topUpContainer.visibility = VISIBLE
    binding.mainCurrencyCode.visibility = VISIBLE
    binding.mainValue.visibility = VISIBLE
    binding.topSeparatorTopup.visibility = VISIBLE
    binding.convertedValue.visibility = VISIBLE
    binding.button.visibility = VISIBLE

    if (isStored) {
      binding.changeCardButton.visibility = VISIBLE
    } else {
      binding.changeCardButton.visibility = GONE
    }

    binding.creditCardInfoContainer.visibility = VISIBLE
    binding.fragmentAdyenError.root.visibility = GONE

    topUpView.unlockRotation()
  }

  override fun showSpecificError(@StringRes stringRes: Int) {
    topUpView.unlockRotation()
    viewModelStore.clear()
    binding.loading.visibility = GONE
    binding.topUpContainer.visibility = GONE

    val message = getString(stringRes)
    binding.fragmentAdyenError.errorMessage.text = message
    binding.fragmentAdyenError.root.visibility = VISIBLE
  }

  override fun showVerificationError() {
    showSpecificError(R.string.purchase_error_verify_wallet)
    binding.fragmentAdyenError.errorTitle.visibility = VISIBLE
    binding.fragmentAdyenError.errorVerifyWalletButton.visibility = VISIBLE
  }

  override fun showCvvError() {
    topUpView.unlockRotation()
    binding.loading.visibility = GONE
    binding.button.isEnabled = false
    if (isStored) {
      binding.changeCardButton.visibility = VISIBLE
    } else {
      binding.changeCardButton.visibility = INVISIBLE
    }
    binding.creditCardInfoContainer.visibility = VISIBLE

    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }

  override fun retryClick() = RxView.clicks(binding.noNetwork.retryButton)

  override fun getTryAgainClicks() = RxView.clicks(binding.fragmentAdyenError.tryAgain)

  override fun getSupportClicks(): Observable<Any> {
    return Observable.merge(RxView.clicks(binding.fragmentAdyenError.layoutSupportLogo), RxView.clicks(binding.fragmentAdyenError.layoutSupportIcn))
  }

  override fun topUpButtonClicked() = RxView.clicks(binding.button)

  override fun getVerificationClicks() =
    RxView.clicks(binding.fragmentAdyenError.errorVerifyWalletButton)

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
    binding.button.isEnabled = true
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
    binding.adyenCardForm.adyenCardFormPreSelected.attach(cardComponent, this)
    cardComponent.observe(this) {
      if (it != null && it.isValid) {
        binding.button.isEnabled = true
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
        binding.button.isEnabled = false
      }
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    binding.changeCardButton.visibility = if (isStored) VISIBLE else GONE
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
    binding.bonusLayout.root.visibility = VISIBLE
    binding.bonusMsg.visibility = VISIBLE
  }

  override fun showVerification() = topUpView.showVerification()

  private fun buildBonusString(bonus: BigDecimal, bonusCurrency: String) {
    val scaledBonus = bonus.max(BigDecimal("0.01"))
    val currency = "~$bonusCurrency".takeIf { bonus < BigDecimal("0.01") } ?: bonusCurrency
    binding.bonusLayout.bonusHeader1.text = getString(R.string.topup_bonus_header_part_1)
    binding.bonusLayout.bonusValue.text = getString(
      R.string.topup_bonus_header_part_2,
      currency + formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    )
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun getPaymentDetails() = paymentDetailsSubject!!

  override fun forgetCardClick(): Observable<Any> {
    return RxView.clicks(binding.changeCardButton)
    //return if (change_card_button != null) RxView.clicks(change_card_button)
    //else RxView.clicks(change_card_button_pre_selected)
  }

  // TODO: Refactor this to pass the whole Intent.
  // TODO: Currently this relies on the fact that Adyen 4.4.0 internally uses only Intent.getData().
  override fun submitUriResult(uri: Uri) = redirectComponent.handleIntent(Intent("", uri))

  override fun updateTopUpButton(valid: Boolean) {
    binding.button.isEnabled = valid
  }

  override fun cancelPayment() = topUpView.cancelPayment()

  override fun setFinishingPurchase(newState: Boolean) = topUpView.setFinishingPurchase(newState)

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      binding.adyenCardForm.adyenCardFormPreSelectedNumber.text = adyenCardView.cardNumber
      binding.adyenCardForm.adyenCardFormPreSelectedNumber.visibility = VISIBLE
      binding.adyenCardForm.paymentMethodIc.setImageDrawable(adyenCardView.cardImage)
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      binding.adyenCardForm.adyenCardFormPreSelectedNumber.visibility = GONE
      binding.adyenCardForm.paymentMethodIc.visibility = GONE
    }
  }

  override fun setupUi() {
    binding.creditCardInfoContainer.visibility = INVISIBLE
    binding.button.isEnabled = false

    if (paymentType == PaymentType.CARD.name) {
      binding.button.setText(getString(R.string.topup_home_button))
      adyenCardView = AdyenCardView(binding.adyenCardForm.adyenCardFormPreSelected ?: binding.adyenCardForm.root)
      setupCardConfiguration()
    }
    setupRedirectConfiguration()
    setupAdyen3DS2ConfigurationBuilder()
    binding.mainValue.visibility = INVISIBLE
    binding.button.visibility = VISIBLE
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
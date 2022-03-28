package com.asfoundation.wallet.billing.adyen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.commons.Logger
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.billing.address.BillingAddressFragment.Companion.BILLING_ADDRESS_MODEL
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.KeyboardUtils
import com.asfoundation.wallet.util.Period
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.adyen_credit_card_layout.*
import kotlinx.android.synthetic.main.adyen_credit_card_layout.fragment_credit_card_authorization_progress_bar
import kotlinx.android.synthetic.main.adyen_credit_card_pre_selected.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_adyen_error.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.fragment_iab_transaction_completed.*
import kotlinx.android.synthetic.main.iab_error_layout.*
import kotlinx.android.synthetic.main.payment_methods_header.*
import kotlinx.android.synthetic.main.selected_payment_method_cc.*
import kotlinx.android.synthetic.main.support_error_layout.*
import kotlinx.android.synthetic.main.support_error_layout.view.*
import kotlinx.android.synthetic.main.view_purchase_bonus.*
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AdyenPaymentFragment : BasePageViewFragment(), AdyenPaymentView {

  @Inject
  lateinit var inAppPurchaseInteractor: InAppPurchaseInteractor

  @Inject
  lateinit var billing: Billing

  @Inject
  lateinit var analytics: BillingAnalytics

  @Inject
  lateinit var adyenPaymentInteractor: AdyenPaymentInteractor

  @Inject
  lateinit var skillsPaymentInteractor: SkillsPaymentInteractor

  @Inject
  lateinit var adyenEnvironment: Environment

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var servicesErrorMapper: ServicesErrorCodeMapper

  @Inject
  lateinit var logger: Logger
  private lateinit var iabView: IabView
  private lateinit var presenter: AdyenPaymentPresenter
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectConfiguration: RedirectConfiguration
  private lateinit var adyen3DS2Configuration: Adyen3DS2Configuration
  private lateinit var compositeDisposable: CompositeDisposable
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<AdyenComponentResponseModel>? = null
  private var adyen3DSErrorSubject: PublishSubject<String>? = null
  private lateinit var adyenCardNumberLayout: TextInputLayout
  private lateinit var adyenExpiryDateLayout: TextInputLayout
  private lateinit var adyenSecurityCodeLayout: TextInputLayout
  private var adyenCardImageLayout: RoundCornerImageView? = null
  private var adyenSaveDetailsSwitch: SwitchCompat? = null
  private var isStored = false
  private var billingAddressInput: PublishSubject<Boolean>? = null
  private var billingAddressModel: BillingAddressModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    billingAddressInput = PublishSubject.create()
    val navigator = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    compositeDisposable = CompositeDisposable()
    presenter = AdyenPaymentPresenter(
      this, compositeDisposable, AndroidSchedulers.mainThread(),
      Schedulers.io(), RedirectComponent.getReturnUrl(requireContext()), analytics, domain,
      origin,
      adyenPaymentInteractor, skillsPaymentInteractor,
      inAppPurchaseInteractor.parseTransaction(transactionData, true), navigator, paymentType,
      transactionType, amount, currency, skills, isPreSelected, AdyenErrorCodeMapper(),
      servicesErrorMapper, gamificationLevel, formatter, logger
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return if (isPreSelected) {
      inflater.inflate(R.layout.adyen_credit_card_pre_selected, container, false)
    } else {
      inflater.inflate(R.layout.adyen_credit_card_layout, container, false)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    presenter.present(savedInstanceState)
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

  private fun setupUi() {
    setupAdyenLayouts()
    setupTransactionCompleteAnimation()
    handleBuyButtonText()
    if (paymentType == PaymentType.CARD.name) setupCardConfiguration()
    setupRedirectConfiguration()
    setupAdyen3DS2ConfigurationBuilder()

    handlePreSelectedView()
    handleBonusAnimation()

    showProduct()
  }

  override fun finishCardConfiguration(
    paymentInfoModel: PaymentInfoModel,
    forget: Boolean,
    savedInstance: Bundle?
  ) {
    this.isStored = paymentInfoModel.isStored
    buy_button.visibility = VISIBLE
    cancel_button.visibility = VISIBLE

    handleLayoutVisibility(isStored)
    prepareCardComponent(paymentInfoModel, forget, savedInstance)
    setStoredPaymentInformation(isStored)
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.apply {
      putString(CARD_NUMBER_KEY, adyenCardNumberLayout.editText?.text.toString())
      putString(EXPIRY_DATE_KEY, adyenExpiryDateLayout.editText?.text.toString())
      putString(CVV_KEY, adyenSecurityCodeLayout.editText?.text.toString())
      putBoolean(SAVE_DETAILS_KEY, adyenSaveDetailsSwitch?.isChecked ?: false)
    }
    presenter.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "adyen payment fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == BILLING_ADDRESS_REQUEST_CODE && resultCode == BILLING_ADDRESS_SUCCESS_CODE) {
      main_view_pre_selected?.visibility = VISIBLE
      main_view?.visibility = VISIBLE
      val billingAddressModel =
        data!!.getSerializableExtra(BILLING_ADDRESS_MODEL) as BillingAddressModel
      this.billingAddressModel = billingAddressModel
      billingAddressInput?.onNext(true)
    } else {
      showMoreMethods()
    }
  }

  override fun billingAddressInput(): Observable<Boolean> {
    return billingAddressInput!!
  }

  override fun retrieveBillingAddressData() = billingAddressModel

  override fun getAnimationDuration() = lottie_transaction_success.duration

  override fun showProduct() {
    try {
      app_icon?.setImageDrawable(requireContext().packageManager.getApplicationIcon(domain))
      app_name?.text = getApplicationName(domain)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    app_sku_description?.text = skuDescription
    val appcValue = formatter.formatPaymentCurrency(appcAmount, WalletCurrency.APPCOINS)
    appc_price.text = appcValue.plus(" " + WalletCurrency.APPCOINS.symbol)
  }

  override fun showLoading() {
    fragment_credit_card_authorization_progress_bar.visibility = VISIBLE
    if (isPreSelected) {
      payment_methods?.visibility = View.INVISIBLE
    } else {
      if (bonus.isNotEmpty()) {
        bonus_layout.visibility = View.INVISIBLE
        bonus_msg.visibility = View.INVISIBLE
      }
      adyen_card_form.visibility = View.INVISIBLE
      change_card_button.visibility = View.INVISIBLE
      cancel_button.visibility = View.INVISIBLE
      buy_button.visibility = View.INVISIBLE
      fiat_price_skeleton.visibility = GONE
      appc_price_skeleton.visibility = GONE
    }
  }

  override fun hideLoadingAndShowView() {
    fragment_credit_card_authorization_progress_bar?.visibility = GONE
    if (isPreSelected) {
      payment_methods?.visibility = VISIBLE
    } else {
      showBonus()
      adyen_card_form.visibility = VISIBLE
      cancel_button.visibility = VISIBLE
    }
  }

  override fun showNetworkError() {
    showSpecificError(R.string.notification_no_network_poa)
  }

  override fun backEvent(): Observable<Any> {
    return RxView.clicks(cancel_button)
      .mergeWith(iabView.backButtonPress())
  }

  override fun showSuccess(renewal: Date?) {
    iab_activity_transaction_completed.visibility = VISIBLE
    fragment_credit_card_authorization_progress_bar?.visibility = GONE
    if (isSubscription && renewal != null) {
      next_payment_date.visibility = VISIBLE
      setBonusMessage(renewal)
    }
    if (isPreSelected) {
      main_view?.visibility = GONE
      main_view_pre_selected?.visibility = GONE
    } else {
      fragment_credit_card_authorization_progress_bar.visibility = GONE
      credit_card_info.visibility = GONE
      lottie_transaction_success.visibility = VISIBLE
      fragment_adyen_error?.visibility = GONE
      fragment_adyen_error_pre_selected?.visibility = GONE
    }
  }

  override fun showGenericError() {
    showSpecificError(R.string.unknown_error)
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

  override fun showVerification(isWalletVerified: Boolean) =
    iabView.showVerification(isWalletVerified)

  override fun showBillingAddress(value: BigDecimal, currency: String) {
    main_view?.visibility = GONE
    main_view_pre_selected?.visibility = GONE
    iabView.showBillingAddress(
      value, currency, bonus, appcAmount, this,
      adyenSaveDetailsSwitch?.isChecked ?: true, isStored
    )
  }


  override fun showSpecificError(@StringRes stringRes: Int) {
    fragment_credit_card_authorization_progress_bar?.visibility = GONE
    cancel_button?.visibility = GONE
    buy_button?.visibility = GONE
    payment_methods?.visibility = VISIBLE
    bonus_layout_pre_selected?.visibility = GONE
    bonus_msg_pre_selected?.visibility = GONE
    bonus_layout?.visibility = GONE
    bonus_msg?.visibility = GONE
    more_payment_methods?.visibility = GONE
    adyen_card_form?.visibility = GONE
    layout_pre_selected?.visibility = GONE
    change_card_button?.visibility = GONE
    change_card_button_pre_selected?.visibility = GONE

    error_buttons?.visibility = VISIBLE
    dialog_buy_buttons_error?.visibility = VISIBLE

    val message = getString(stringRes)

    fragment_adyen_error?.error_message?.text = message
    fragment_adyen_error_pre_selected?.error_message?.text = message
    fragment_adyen_error?.visibility = VISIBLE
    fragment_adyen_error_pre_selected?.visibility = VISIBLE
  }

  override fun showVerificationError(isWalletVerified: Boolean) {
    if (isWalletVerified) {
      showSpecificError(R.string.purchase_error_verify_card)
      error_verify_wallet_button?.visibility = GONE
      error_verify_card_button?.visibility = VISIBLE
    } else {
      showSpecificError(R.string.purchase_error_verify_wallet)
      error_verify_wallet_button?.visibility = VISIBLE
      error_verify_card_button?.visibility = GONE
    }
  }

  override fun showCvvError() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (isStored) {
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    }
    buy_button?.visibility = VISIBLE
    buy_button?.isEnabled = false
    adyenSecurityCodeLayout.error = getString(R.string.purchase_card_error_CVV)
  }

  override fun getMorePaymentMethodsClicks() = RxView.clicks(more_payment_methods)

  override fun showMoreMethods() {
    main_view?.let { KeyboardUtils.hideKeyboard(it) }
    main_view_pre_selected?.let { KeyboardUtils.hideKeyboard(it) }
    iabView.unlockRotation()
    iabView.showPaymentMethodsView()
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

  override fun forgetCardClick(): Observable<Any> {
    return if (change_card_button != null) RxView.clicks(change_card_button)
    else RxView.clicks(change_card_button_pre_selected)
  }

  @SuppressLint("SetTextI18n")
  override fun showProductPrice(amount: String, currencyCode: String) {
    var fiatText = "$amount $currencyCode"
    if (isSubscription) {
      val period = Period.parse(frequency!!)
      period?.mapToSubsFrequency(requireContext(), fiatText)
        ?.let { fiatText = it }
      appc_price.text = "~${appc_price.text}"
    }
    fiat_price.text = fiatText
    fiat_price_skeleton.visibility = GONE
    appc_price_skeleton.visibility = GONE
    fiat_price.visibility = VISIBLE
    appc_price.visibility = VISIBLE
  }

  override fun adyenErrorBackClicks() = RxView.clicks(error_back)

  override fun adyenErrorCancelClicks() = RxView.clicks(error_cancel)

  override fun errorDismisses() = RxView.clicks(error_dismiss)

  override fun buyButtonClicked() = RxView.clicks(buy_button)

  override fun close(bundle: Bundle?) = iabView.close(bundle)

  // TODO: Refactor this to pass the whole Intent.
  // TODO: Currently this relies on the fact that Adyen 4.4.0 internally uses only Intent.getData().
  override fun submitUriResult(uri: Uri) = redirectComponent.handleIntent(Intent("", uri))

  override fun getPaymentDetails(): Observable<AdyenComponentResponseModel> =
    paymentDetailsSubject!!

  override fun getAdyenSupportLogoClicks() = RxView.clicks(layout_support_logo)

  override fun getAdyenSupportIconClicks() = RxView.clicks(layout_support_icn)

  override fun getVerificationClicks() = Observable.merge(
    RxView.clicks(error_verify_wallet_button)
      .map { false }, RxView.clicks(error_verify_card_button)
      .map { true })

  override fun lockRotation() = iabView.lockRotation()

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
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
    adyenCardImageLayout =
      adyen_card_form_pre_selected?.findViewById(R.id.cardBrandLogo_imageView_primary)
        ?: adyen_card_form?.findViewById(R.id.cardBrandLogo_imageView_primary)
    adyenSaveDetailsSwitch =
      adyen_card_form_pre_selected?.findViewById(R.id.switch_storePaymentMethod)
        ?: adyen_card_form?.findViewById(R.id.switch_storePaymentMethod)

    adyenCardNumberLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenExpiryDateLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
    adyenSecurityCodeLayout.editText?.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI

    adyenSaveDetailsSwitch?.run {

      val params: LinearLayout.LayoutParams = this.layoutParams as LinearLayout.LayoutParams
      params.topMargin = 2

      layoutParams = params
      isChecked = true
      textSize = 14f
      text = getString(R.string.dialog_credit_card_remember)
    }

    val height = resources.getDimensionPixelSize(R.dimen.adyen_text_input_layout_height)

    adyenCardNumberLayout.minimumHeight = height
    adyenCardNumberLayout.errorIconDrawable = null
    adyenExpiryDateLayout.minimumHeight = height
    adyenSecurityCodeLayout.minimumHeight = height
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

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence {
    val packageManager = requireContext().packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(lottie_transaction_success)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    lottie_transaction_success.setTextDelegate(textDelegate)
    lottie_transaction_success.setFontAssetDelegate(object : FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private fun showBonus() {
    if (bonus.isNotEmpty()) {
      bonus_layout?.visibility = VISIBLE
      bonus_layout_pre_selected?.visibility = VISIBLE
      bonus_msg?.visibility = VISIBLE
      bonus_msg_pre_selected?.visibility = VISIBLE
      bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonus)
      frequency?.let {
        bonus_msg?.text = getString(R.string.subscriptions_bonus_body)
        bonus_msg_pre_selected?.text = getString(R.string.subscriptions_bonus_body)
      }
    } else {
      bonus_layout?.visibility = GONE
      bonus_layout_pre_selected?.visibility = GONE
      bonus_msg?.visibility = GONE
      bonus_msg_pre_selected?.visibility = GONE
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    if (isStored) {
      adyenCardNumberLayout.visibility = GONE
      adyenExpiryDateLayout.visibility = GONE
      adyenCardImageLayout?.visibility = GONE
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
      view?.let { KeyboardUtils.showKeyboard(it) }
    } else {
      adyenCardNumberLayout.visibility = VISIBLE
      adyenExpiryDateLayout.visibility = VISIBLE
      adyenCardImageLayout?.visibility = VISIBLE
      change_card_button?.visibility = GONE
      change_card_button_pre_selected?.visibility = GONE
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
        buy_button?.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          paymentDataSubject?.onNext(
            AdyenCardWrapper(
              paymentMethod, adyenSaveDetailsSwitch?.isChecked ?: false, hasCvc,
              paymentInfoModel.supportedShopperInteractions
            )
          )
        }
      } else {
        buy_button?.isEnabled = false
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
      adyenSaveDetailsSwitch?.isChecked = it.getBoolean(SAVE_DETAILS_KEY, false)
      it.clear()
    }
  }

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number?.text = adyenCardNumberLayout.editText?.text
      adyen_card_form_pre_selected_number?.visibility = VISIBLE
      payment_method_ic?.setImageDrawable(adyenCardImageLayout?.drawable)
    } else {
      adyen_card_form_pre_selected_number?.visibility = GONE
      payment_method_ic?.visibility = GONE
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

  private fun handleBonusAnimation() {
    if (StringUtils.isNotBlank(bonus)) {
      lottie_transaction_success.setAnimation(R.raw.transaction_complete_bonus_animation)
      setupTransactionCompleteAnimation()
    } else {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
  }

  private fun handlePreSelectedView() {
    if (!isPreSelected) {
      cancel_button.setText(R.string.back_button)
      iabView.disableBack()
    }
    showBonus()
  }

  private fun setBonusMessage(nextPaymentDate: Date) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(nextPaymentDate)
    val nextPaymentText =
      "${getString(R.string.subscriptions_details_next_payment_title)} $formattedDate"
    next_payment_date.text = nextPaymentText
  }

  private fun handleBuyButtonText() {
    when {
      transactionType.equals(TransactionData.TransactionType.DONATION.name, ignoreCase = true) -> {
        buy_button.setText(R.string.action_donate)
      }
      transactionType.equals(
        TransactionData.TransactionType.INAPP_SUBSCRIPTION.name,
        ignoreCase = true
      ) -> buy_button.text = getString(R.string.subscriptions_subscribe_button)
      else -> {
        buy_button.setText(R.string.action_buy)
      }
    }
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    paymentDataSubject = null
    paymentDetailsSubject = null
    adyen3DSErrorSubject = null
    billingAddressInput = null
    super.onDestroy()
  }

  companion object {

    private const val TRANSACTION_TYPE_KEY = "type"
    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val DOMAIN_KEY = "domain"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val APPC_AMOUNT_KEY = "appc_amount"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val FREQUENCY = "frequency"
    private const val CARD_NUMBER_KEY = "card_number"
    private const val EXPIRY_DATE_KEY = "expiry_date"
    private const val CVV_KEY = "cvv_key"
    private const val SAVE_DETAILS_KEY = "save_details"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"
    private const val PRODUCT_TOKEN = "product_token"

    @JvmStatic
    fun newInstance(
      transactionType: String, paymentType: PaymentType, domain: String,
      origin: String?, transactionData: String?, appcAmount: BigDecimal,
      amount: BigDecimal, currency: String?, bonus: String?, isPreSelected: Boolean,
      gamificationLevel: Int, skuDescription: String, productToken: String?,
      isSubscription: Boolean, frequency: String?
    ): AdyenPaymentFragment {
      val fragment = AdyenPaymentFragment()
      fragment.arguments = Bundle().apply {
        putString(TRANSACTION_TYPE_KEY, transactionType)
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(DOMAIN_KEY, domain)
        putString(ORIGIN_KEY, origin)
        putString(TRANSACTION_DATA_KEY, transactionData)
        putSerializable(APPC_AMOUNT_KEY, appcAmount)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putString(SKU_DESCRIPTION, skuDescription)
        putString(PRODUCT_TOKEN, productToken)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putString(FREQUENCY, frequency)
      }
      return fragment
    }
  }

  private val transactionType: String by lazy {
    if (requireArguments().containsKey(TRANSACTION_TYPE_KEY)) {
      requireArguments().getString(TRANSACTION_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("transaction type data not found")
    }
  }

  private val paymentType: String by lazy {
    if (requireArguments().containsKey(PAYMENT_TYPE_KEY)) {
      requireArguments().getString(PAYMENT_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("payment type data not found")
    }
  }

  private val domain: String by lazy {
    if (requireArguments().containsKey(DOMAIN_KEY)) {
      requireArguments().getString(DOMAIN_KEY, "")
    } else {
      throw IllegalArgumentException("domain data not found")
    }
  }

  private val origin: String? by lazy {
    if (requireArguments().containsKey(ORIGIN_KEY)) {
      requireArguments().getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionData: String by lazy {
    if (requireArguments().containsKey(TRANSACTION_DATA_KEY)) {
      requireArguments().getString(TRANSACTION_DATA_KEY, "")
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val appcAmount: BigDecimal by lazy {
    if (requireArguments().containsKey(APPC_AMOUNT_KEY)) {
      requireArguments().getSerializable(APPC_AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("appc amount data not found")
    }
  }

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY_KEY)) {
      requireArguments().getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val isPreSelected: Boolean by lazy {
    if (requireArguments().containsKey(PRE_SELECTED_KEY)) {
      requireArguments().getBoolean(PRE_SELECTED_KEY)
    } else {
      throw IllegalArgumentException("pre selected data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }

  private val skuDescription: String by lazy {
    if (requireArguments().containsKey(SKU_DESCRIPTION)) {
      requireArguments().getString(SKU_DESCRIPTION, "")
    } else {
      throw IllegalArgumentException("sku description data not found")
    }
  }

  private val skills: Boolean by lazy {
    transactionData.contains("&skills")
  }

  private val isSubscription: Boolean by lazy {
    if (requireArguments().containsKey(IS_SUBSCRIPTION)) {
      requireArguments().getBoolean(IS_SUBSCRIPTION)
    } else {
      throw IllegalArgumentException("isSubscription data not found")
    }
  }

  private val frequency: String? by lazy {
    if (requireArguments().containsKey(FREQUENCY)) {
      requireArguments().getString(FREQUENCY)
    } else {
      null
    }
  }
}
package com.asfoundation.wallet.billing.adyen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardView
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.airbnb.lottie.LottieAnimationView
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.ui.widgets.WalletButtonView
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.AdyenCreditCardLayoutBinding
import com.asf.wallet.databinding.AdyenCreditCardPreSelectedBinding
import com.asfoundation.wallet.billing.address.BillingAddressFragment.Companion.BILLING_ADDRESS_MODEL
import com.asfoundation.wallet.billing.address.BillingAddressModel
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.service.ServicesErrorCodeMapper
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_REQUEST_CODE
import com.asfoundation.wallet.ui.iab.IabActivity.Companion.BILLING_ADDRESS_SUCCESS_CODE
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.util.*
import com.facebook.shimmer.ShimmerFrameLayout
import com.jakewharton.rxbinding2.view.RxView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
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
  lateinit var paymentAnalytics: PaymentMethodsAnalytics

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
  private lateinit var adyenCardView: AdyenCardView
  private var paymentDataSubject: ReplaySubject<AdyenCardWrapper>? = null
  private var paymentDetailsSubject: PublishSubject<AdyenComponentResponseModel>? = null
  private var adyen3DSErrorSubject: PublishSubject<String>? = null
  private var isStored = false
  private var billingAddressInput: PublishSubject<Boolean>? = null
  private var billingAddressModel: BillingAddressModel? = null

  private val bindingCreditCardPreSelected: AdyenCreditCardPreSelectedBinding? by lazy {
    if (isPreSelected) AdyenCreditCardPreSelectedBinding.bind(
      requireView()
    ) else null
  }
  private val bindingCreditCardLayout: AdyenCreditCardLayoutBinding? by lazy {
    if (!isPreSelected) AdyenCreditCardLayoutBinding.bind(
      requireView()
    ) else null
  }

  // dialog_buy_buttons.xml
  private val buy_button: WalletButtonView
    get() = bindingCreditCardPreSelected?.dialogBuyButtonsPaymentMethods?.buyButton
      ?: bindingCreditCardLayout?.dialogBuyButtons?.buyButton!!
  private val cancel_button: WalletButtonView
    get() = bindingCreditCardPreSelected?.dialogBuyButtonsPaymentMethods?.cancelButton
      ?: bindingCreditCardLayout?.dialogBuyButtons?.cancelButton!!

  // dialog_buy_buttons_adyen_error.xml
  private val error_cancel: WalletButtonView
    get() = bindingCreditCardPreSelected?.dialogBuyButtonsError?.errorCancel
      ?: bindingCreditCardLayout?.errorButtons?.errorCancel!!
  private val error_back: WalletButtonView
    get() = bindingCreditCardPreSelected?.dialogBuyButtonsError?.errorBack
      ?: bindingCreditCardLayout?.errorButtons?.errorBack!!
  private val error_try_again: WalletButtonView
    get() = bindingCreditCardPreSelected?.dialogBuyButtonsError?.errorTryAgain
      ?: bindingCreditCardLayout?.errorButtons?.errorTryAgain!!

  // iab_error_layout.xml
  private val error_dismiss: WalletButtonView
    get() = bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.errorDismiss
      ?: bindingCreditCardLayout?.fragmentIabError?.errorDismiss!!

  // support_error_layout.xml
  private val error_message: TextView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorMessage
      ?: bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.errorMessage
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorMessage
      ?: bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.genericErrorLayout?.errorMessage!!
  private val error_verify_wallet_button: WalletButtonView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorVerifyWalletButton
      ?: bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.errorVerifyWalletButton
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorVerifyWalletButton
      ?: bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.genericErrorLayout?.errorVerifyWalletButton!!

  private val error_verify_card_button: WalletButtonView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorVerifyCardButton
      ?: bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.errorVerifyCardButton
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorVerifyCardButton
      ?: bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.genericErrorLayout?.errorVerifyCardButton!!

  private val layout_support_logo: ImageView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.layoutSupportLogo
      ?: bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.layoutSupportLogo
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.layoutSupportLogo
      ?: bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.genericErrorLayout?.layoutSupportLogo!!
  private val layout_support_icn: ImageView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.layoutSupportIcn
      ?: bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.layoutSupportIcn
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.layoutSupportIcn
      ?: bindingCreditCardPreSelected?.fragmentIabErrorPreSelected?.genericErrorLayout?.layoutSupportIcn!!

  // view_purchase_bonus.xml
  private val bonus_value: TextView
    get() = bindingCreditCardPreSelected?.bonusLayoutPreSelected?.bonusValue
      ?: bindingCreditCardLayout?.bonusLayout?.bonusValue!!

  // selected_payment_method_cc.xml
  private val adyen_card_form_pre_selected: CardView
    get() = bindingCreditCardPreSelected?.layoutPreSelected?.adyenCardFormPreSelected
      ?: bindingCreditCardLayout?.adyenCardForm?.adyenCardFormPreSelected!!
  private val payment_method_ic: ImageView
    get() = bindingCreditCardPreSelected?.layoutPreSelected?.paymentMethodIc
      ?: bindingCreditCardLayout?.adyenCardForm?.paymentMethodIc!!
  private val adyen_card_form_pre_selected_number: TextView
    get() = bindingCreditCardPreSelected?.layoutPreSelected?.adyenCardFormPreSelectedNumber
      ?: bindingCreditCardLayout?.adyenCardForm?.adyenCardFormPreSelectedNumber!!

  // payment_methods_header.xml
  private val app_icon: ImageView
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.appIcon
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.appIcon!!
  private val app_name: TextView
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.appName
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.appName!!
  private val app_sku_description: TextView
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.appSkuDescription
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.appSkuDescription!!
  private val fiat_price: TextView
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.fiatPrice
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice!!
  private val appc_price: TextView
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.appcPrice
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.appcPrice!!

  // fragment_iab_transaction_completed.xml
  private val lottie_transaction_success: LottieAnimationView
    get() = bindingCreditCardPreSelected?.fragmentIabTransactionCompleted?.lottieTransactionSuccess
      ?: bindingCreditCardLayout?.fragmentIabTransactionCompleted?.lottieTransactionSuccess!!
  private val transaction_success_bonus_text: TextView
    get() = bindingCreditCardPreSelected?.fragmentIabTransactionCompleted?.transactionSuccessBonusText
      ?: bindingCreditCardLayout?.fragmentIabTransactionCompleted?.transactionSuccessBonusText!!
  private val bonus_success_layout: LinearLayout
    get() = bindingCreditCardPreSelected?.fragmentIabTransactionCompleted?.bonusSuccessLayout
      ?: bindingCreditCardLayout?.fragmentIabTransactionCompleted?.bonusSuccessLayout!!
  private val next_payment_date: TextView
    get() = bindingCreditCardPreSelected?.fragmentIabTransactionCompleted?.nextPaymentDate
      ?: bindingCreditCardLayout?.fragmentIabTransactionCompleted?.nextPaymentDate!!

  // adyen_credit_card_layout.xml and adyen_credit_card_pre_selected.xml
  private val fragment_credit_card_authorization_progress_bar: LottieAnimationView
    get() = bindingCreditCardPreSelected?.fragmentCreditCardAuthorizationProgressBar
      ?: bindingCreditCardLayout?.fragmentCreditCardAuthorizationProgressBar!!
  private val making_purchase_text: TextView
    get() = bindingCreditCardPreSelected?.makingPurchaseText
      ?: bindingCreditCardLayout?.makingPurchaseText!!
  private val fiat_price_skeleton: ShimmerFrameLayout
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.fiatPriceSkeleton?.root
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.fiatPriceSkeleton?.root!!
  private val appc_price_skeleton: ShimmerFrameLayout
    get() = bindingCreditCardPreSelected?.paymentMethodsHeader?.appcPriceSkeleton?.root
      ?: bindingCreditCardLayout?.paymentMethodsHeader?.appcPriceSkeleton?.root!!
  private val iab_activity_transaction_completed: ConstraintLayout
    get() = bindingCreditCardPreSelected?.fragmentIabTransactionCompleted?.iabActivityTransactionCompleted
      ?: bindingCreditCardLayout?.fragmentIabTransactionCompleted?.iabActivityTransactionCompleted!!

  // adyen_credit_card_layout.xml
  private val adyen_credit_card_root: RelativeLayout? get() = bindingCreditCardLayout?.adyenCreditCardRoot
  private val main_view: RelativeLayout? get() = bindingCreditCardLayout?.mainView
  private val credit_card_info: ConstraintLayout? get() = bindingCreditCardLayout?.creditCardInfo
  private val change_card_button: WalletButtonView? get() = bindingCreditCardLayout?.changeCardButton
  private val bonus_layout: ConstraintLayout? get() = bindingCreditCardLayout?.bonusLayout?.root
  private val adyen_card_form: ConstraintLayout? get() = bindingCreditCardLayout?.adyenCardForm?.root
  private val fragment_adyen_error: ConstraintLayout? get() = bindingCreditCardLayout?.fragmentAdyenError?.root
  private val error_buttons: LinearLayout? get() = bindingCreditCardLayout?.errorButtons?.root

  // adyen_credit_card_pre_selected.xml
  private val main_view_pre_selected: RelativeLayout? get() = bindingCreditCardPreSelected?.mainViewPreSelected
  private val payment_methods: ConstraintLayout? get() = bindingCreditCardPreSelected?.paymentMethods
  private val change_card_button_pre_selected: WalletButtonView? get() = bindingCreditCardPreSelected?.changeCardButtonPreSelected
  private val more_payment_methods: WalletButtonView? get() = bindingCreditCardPreSelected?.morePaymentMethods
  private val bonus_layout_pre_selected: ConstraintLayout? get() = bindingCreditCardPreSelected?.bonusLayoutPreSelected?.root
  private val layout_pre_selected: ConstraintLayout? get() = bindingCreditCardPreSelected?.layoutPreSelected?.root
  private val fragment_adyen_error_pre_selected: ConstraintLayout? get() = bindingCreditCardPreSelected?.fragmentAdyenErrorPreSelected?.root
  private val dialog_buy_buttons_error: LinearLayout? get() = bindingCreditCardPreSelected?.dialogBuyButtonsError?.root


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    billingAddressInput = PublishSubject.create()
    val navigator = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    compositeDisposable = CompositeDisposable()
    presenter = AdyenPaymentPresenter(
      view = this,
      iabView = iabView,
      disposables = compositeDisposable,
      viewScheduler = AndroidSchedulers.mainThread(),
      networkScheduler = Schedulers.io(),
      returnUrl = RedirectComponent.getReturnUrl(requireContext()),
      analytics = analytics,
      paymentAnalytics = paymentAnalytics,
      origin = origin,
      adyenPaymentInteractor = adyenPaymentInteractor,
      skillsPaymentInteractor = skillsPaymentInteractor,
      transactionBuilder = transactionBuilder,
      navigator = navigator,
      paymentType = paymentType,
      amount = amount,
      currency = currency,
      skills = skills,
      isPreSelected = isPreSelected,
      adyenErrorCodeMapper = AdyenErrorCodeMapper(),
      servicesErrorCodeMapper = servicesErrorMapper,
      gamificationLevel = gamificationLevel,
      formatter = formatter,
      logger = logger
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = if (isPreSelected) {
    AdyenCreditCardPreSelectedBinding.inflate(inflater).root
  } else {
    AdyenCreditCardLayoutBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()

    val orientation = this.resources.configuration.orientation
    val dpWidth = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 592F else 340F

    val dimensionInPixels = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dpWidth,
      resources.displayMetrics
    ).toInt()
    adyen_credit_card_root?.layoutParams?.width = dimensionInPixels
    adyen_credit_card_root?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT


    presenter.present(savedInstanceState)
  }

  override fun setup3DSComponent() {
    activity?.application?.let { application ->
      adyen3DS2Component =
        Adyen3DS2Component.PROVIDER.get(this, application, adyen3DS2Configuration)
      adyen3DS2Component.observe(this) {
        paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
      }
      adyen3DS2Component.observeErrors(this) {
        adyen3DSErrorSubject?.onNext(it.errorMessage)
      }
    }
  }

  private fun setupUi() {
    adyenCardView = AdyenCardView(adyen_card_form_pre_selected)
    setupTransactionComplete()
    handleBuyButtonText()
    if (paymentType == PaymentType.CARD.name) setupCardConfiguration()
    setupRedirectConfiguration()
    setupAdyen3DS2ConfigurationBuilder()

    handlePreSelectedView()
    handleBonusAnimation()

    showProduct()
  }

  override fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    this.isStored = paymentInfoModel.isStored
    buy_button.visibility = VISIBLE
    cancel_button.visibility = VISIBLE

    prepareCardComponent(paymentInfoModel, forget)
    handleLayoutVisibility(isStored)
    setStoredPaymentInformation(isStored)
  }

  override fun retrievePaymentData() = paymentDataSubject!!

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
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

  override fun billingAddressInput(): Observable<Boolean> = billingAddressInput!!

  override fun retrieveBillingAddressData() = billingAddressModel

  override fun getAnimationDuration() = lottie_transaction_success.duration * 3

  override fun showProduct() {
    try {
      app_icon.setImageDrawable(
        requireContext().packageManager.getApplicationIcon(
          transactionBuilder.domain
        )
      )
      app_name.text = getApplicationName(transactionBuilder.domain)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    app_sku_description.text = skuDescription
    val appcValue =
      formatter.formatPaymentCurrency(transactionBuilder.amount(), WalletCurrency.APPCOINS)
    appc_price.text = appcValue.plus(" " + WalletCurrency.APPCOINS.symbol)
  }

  override fun showLoading() {
    fragment_credit_card_authorization_progress_bar.visibility = VISIBLE
    if (isPreSelected) {
      payment_methods?.visibility = INVISIBLE
    } else {
      if (bonus.isNotEmpty()) {
        bonus_layout?.visibility = INVISIBLE
      }
      adyen_card_form?.visibility = INVISIBLE
      change_card_button?.visibility = INVISIBLE
      cancel_button.visibility = INVISIBLE
      buy_button.visibility = INVISIBLE
      fiat_price_skeleton.visibility = GONE
      appc_price_skeleton.visibility = GONE
    }
  }

  override fun showLoadingMakingPayment() {
    showLoading()
    making_purchase_text.visibility = VISIBLE
  }

  override fun hideLoadingAndShowView() {
    fragment_credit_card_authorization_progress_bar.visibility = GONE
    making_purchase_text.visibility = GONE
    if (isPreSelected) {
      payment_methods?.visibility = VISIBLE
    } else {
      showBonus()
      adyen_card_form?.visibility = VISIBLE
      cancel_button.visibility = VISIBLE
    }
  }

  override fun showNetworkError() = showSpecificError(R.string.notification_no_network_poa)

  override fun backEvent(): Observable<Any> =
    RxView.clicks(cancel_button).mergeWith(iabView.backButtonPress())

  override fun showSuccess(renewal: Date?) {
    iab_activity_transaction_completed.visibility = VISIBLE
    fragment_credit_card_authorization_progress_bar.visibility = GONE
    making_purchase_text.visibility = GONE
    if (isSubscription && renewal != null) {
      next_payment_date.visibility = VISIBLE
      setBonusMessage(renewal)
    }
    if (isPreSelected) {
      main_view?.visibility = GONE
      main_view_pre_selected?.visibility = GONE
    } else {
      fragment_credit_card_authorization_progress_bar.visibility = GONE
      making_purchase_text.visibility = GONE
      credit_card_info?.visibility = GONE
      lottie_transaction_success.visibility = VISIBLE
      fragment_adyen_error?.visibility = GONE
      fragment_adyen_error_pre_selected?.visibility = GONE
    }
  }

  override fun showGenericError() = showSpecificError(R.string.unknown_error)

  override fun showInvalidCardError() =
    showSpecificError(R.string.purchase_error_invalid_credit_card)

  override fun showSecurityValidationError() =
    showSpecificError(R.string.purchase_error_card_security_validation)

  override fun showOutdatedCardError() = showSpecificError(R.string.purchase_card_error_re_insert)

  override fun showAlreadyProcessedError() =
    showSpecificError(R.string.purchase_error_card_already_in_progress)

  override fun showPaymentError() = showSpecificError(R.string.purchase_error_payment_rejected)

  override fun showVerification(isWalletVerified: Boolean) =
    iabView.showVerification(isWalletVerified)

  override fun showBillingAddress(value: BigDecimal, currency: String) {
    main_view?.visibility = GONE
    main_view_pre_selected?.visibility = GONE
    iabView.showBillingAddress(
      value,
      currency,
      bonus,
      transactionBuilder.amount(),
      this,
      adyenCardView.cardSave,
      isStored
    )
  }


  override fun showSpecificError(@StringRes stringRes: Int, backToCard: Boolean) {
    fragment_credit_card_authorization_progress_bar.visibility = GONE
    making_purchase_text.visibility = GONE
    cancel_button.visibility = GONE
    buy_button.visibility = GONE
    payment_methods?.visibility = VISIBLE
    bonus_layout_pre_selected?.visibility = GONE
    bonus_layout?.visibility = GONE
    more_payment_methods?.visibility = GONE
    adyen_card_form?.visibility = GONE
    layout_pre_selected?.visibility = GONE
    change_card_button?.visibility = GONE
    change_card_button_pre_selected?.visibility = GONE

    error_buttons?.visibility = VISIBLE
    dialog_buy_buttons_error?.visibility = VISIBLE

    error_back.visibility = if (backToCard) VISIBLE else GONE
    error_try_again.visibility = if (backToCard) GONE else VISIBLE

    val message = getString(stringRes)

    error_message.text = message
    fragment_adyen_error?.visibility = VISIBLE
    fragment_adyen_error_pre_selected?.visibility = VISIBLE
  }

  override fun showVerificationError(isWalletVerified: Boolean) {
    if (isWalletVerified) {
      showSpecificError(R.string.purchase_error_verify_card)
      error_verify_wallet_button.visibility = GONE
      error_verify_card_button.visibility = VISIBLE
    } else {
      showSpecificError(R.string.purchase_error_verify_wallet)
      error_verify_wallet_button.visibility = VISIBLE
      error_verify_card_button.visibility = GONE
    }
  }

  override fun showCvvError() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (isStored) {
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    }
    buy_button.visibility = VISIBLE
    buy_button.isEnabled = false
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }

  override fun showBackToCard() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (isStored) {
      change_card_button?.visibility = VISIBLE
      change_card_button_pre_selected?.visibility = VISIBLE
    }
    buy_button.visibility = VISIBLE
//    buy_button?.isEnabled = false

    error_buttons?.visibility = GONE
    dialog_buy_buttons_error?.visibility = GONE

    error_back.visibility = VISIBLE
    error_try_again.visibility = GONE

    fragment_adyen_error?.visibility = GONE
    fragment_adyen_error_pre_selected?.visibility = GONE

  }

  override fun getMorePaymentMethodsClicks() = RxView.clicks(more_payment_methods!!)

  override fun showMoreMethods() {
    main_view?.let { KeyboardUtils.hideKeyboard(it) }
    main_view_pre_selected?.let { KeyboardUtils.hideKeyboard(it) }
    iabView.unlockRotation()
    iabView.showPaymentMethodsView()
  }

  override fun setupRedirectComponent() {
    activity?.application?.let { application ->
      redirectComponent = RedirectComponent.PROVIDER.get(this, application, redirectConfiguration)
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
    return if (change_card_button != null) RxView.clicks(change_card_button!!)
    else RxView.clicks(change_card_button_pre_selected!!)
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

  override fun adyenErrorBackClicks() = RxView.clicks(error_try_again)

  override fun adyenErrorBackToCardClicks() = RxView.clicks(error_back)
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

  override fun getVerificationClicks(): Observable<Boolean> =
    Observable.merge(
      RxView.clicks(error_verify_wallet_button).map { false },
      RxView.clicks(error_verify_card_button).map { true }
    )

  override fun lockRotation() = iabView.lockRotation()

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment).build()
  }

  private fun setupRedirectConfiguration() {
    redirectConfiguration =
      RedirectConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  private fun setupAdyen3DS2ConfigurationBuilder() {
    adyen3DS2Configuration =
      Adyen3DS2Configuration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  @Throws(PackageManager.NameNotFoundException::class)
  private fun getApplicationName(appPackage: String): CharSequence {
    val packageManager = requireContext().packageManager
    val packageInfo = packageManager.getApplicationInfo(appPackage, 0)
    return packageManager.getApplicationLabel(packageInfo)
  }

  private fun setupTransactionComplete() {
    if (bonus.isNotEmpty()) {
      transaction_success_bonus_text.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      bonus_success_layout.visibility = GONE
    }
  }

  private fun showBonus() {
    if (bonus.isNotEmpty()) {
      bonus_layout?.visibility = VISIBLE
      bonus_layout_pre_selected?.visibility = VISIBLE
      bonus_value.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      bonus_layout?.visibility = GONE
      bonus_layout_pre_selected?.visibility = GONE
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    change_card_button?.visibility = if (isStored) VISIBLE else GONE
    change_card_button_pre_selected?.visibility = if (isStored) VISIBLE else GONE
    if (isStored) {
      view?.let { KeyboardUtils.showKeyboard(it) }
    }
  }

  private fun prepareCardComponent(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    if (forget) {
      adyenCardView.clear(this)
      unregisterProvider(Adyen3DS2Component::class.java.canonicalName)
      setup3DSComponent()
      unregisterProvider(RedirectComponent::class.java.canonicalName)
      setupRedirectComponent()
    }
    val cardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    adyen_card_form_pre_selected.attach(cardComponent, this)
    cardComponent.observe(this) {
      adyenCardView.setError(null)
      if (it != null && it.isValid) {
        buy_button.isEnabled = true
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
        buy_button.isEnabled = false
      }
    }
  }

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyen_card_form_pre_selected_number.text = adyenCardView.cardNumber
      adyen_card_form_pre_selected_number.visibility = VISIBLE
      payment_method_ic.setImageDrawable(adyenCardView.cardImage)
    } else {
      adyen_card_form_pre_selected_number.visibility = GONE
      payment_method_ic.visibility = GONE
    }
  }

  private fun handleBonusAnimation() {
    if (StringUtils.isNotBlank(bonus)) {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
      setupTransactionComplete()
    } else {
      lottie_transaction_success.setAnimation(R.raw.success_animation)
    }
  }

  private fun handlePreSelectedView() {
    if (!isPreSelected) {
      cancel_button.setText(getString(R.string.back_button))
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
      transactionBuilder.type.equals(
        TransactionData.TransactionType.DONATION.name,
        ignoreCase = true
      ) -> {
        buy_button.setText(getString(R.string.action_donate))
      }
      transactionBuilder.type.equals(
        TransactionData.TransactionType.INAPP_SUBSCRIPTION.name,
        ignoreCase = true
      ) -> buy_button.setText(getString(R.string.subscriptions_subscribe_button))
      else -> {
        buy_button.setText(getString(R.string.action_buy))
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

    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val IS_SKILLS = "is_skills"
    private const val FREQUENCY = "frequency"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"

    @JvmStatic
    fun newInstance(
      paymentType: PaymentType,
      origin: String?,
      transactionBuilder: TransactionBuilder,
      amount: BigDecimal,
      currency: String?,
      bonus: String?,
      isPreSelected: Boolean,
      gamificationLevel: Int,
      skuDescription: String,
      isSubscription: Boolean,
      isSkills: Boolean,
      frequency: String?
    ): AdyenPaymentFragment = AdyenPaymentFragment().apply {
      arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(ORIGIN_KEY, origin)
        putParcelable(TRANSACTION_DATA_KEY, transactionBuilder)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putString(SKU_DESCRIPTION, skuDescription)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putBoolean(IS_SKILLS, isSkills)
        putString(FREQUENCY, frequency)
      }
    }
  }

  private val paymentType: String by lazy {
    if (requireArguments().containsKey(PAYMENT_TYPE_KEY)) {
      requireArguments().getString(PAYMENT_TYPE_KEY, "")
    } else {
      throw IllegalArgumentException("payment type data not found")
    }
  }

  private val origin: String? by lazy {
    if (requireArguments().containsKey(ORIGIN_KEY)) {
      requireArguments().getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_DATA_KEY)) {
      requireArguments().getParcelable<TransactionBuilder>(TRANSACTION_DATA_KEY)!!
    } else {
      throw IllegalArgumentException("transaction data not found")
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
    if (requireArguments().containsKey(IS_SKILLS)) {
      requireArguments().getBoolean(IS_SKILLS)
    } else {
      throw IllegalArgumentException("isSkills data not found")
    }
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
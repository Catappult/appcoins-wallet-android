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
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
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
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.ui.common.R.drawable.ic_card_branc_maestro
import com.appcoins.wallet.ui.common.R.drawable.ic_card_brand_american_express
import com.appcoins.wallet.ui.common.R.drawable.ic_card_brand_diners_club
import com.appcoins.wallet.ui.common.R.drawable.ic_card_brand_discover
import com.appcoins.wallet.ui.common.R.drawable.ic_card_brand_master_card
import com.appcoins.wallet.ui.common.R.drawable.ic_card_brand_visa
import com.appcoins.wallet.ui.widgets.SeparatorView
import com.appcoins.wallet.ui.widgets.WalletButtonView
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.AdyenCreditCardLayoutBinding
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
  private var askCVC = true

  private val bindingCreditCardLayout: AdyenCreditCardLayoutBinding? by lazy {
    AdyenCreditCardLayoutBinding.bind(requireView())
  }

  // dialog_buy_buttons.xml
  private val buyButton: WalletButtonView
    get() = bindingCreditCardLayout?.dialogBuyButtons?.buyButton!!
  private val cancelButton: WalletButtonView
    get() = bindingCreditCardLayout?.dialogBuyButtons?.cancelButton!!

  // dialog_buy_buttons_adyen_error.xml
  private val errorCancel: WalletButtonView
    get() = bindingCreditCardLayout?.errorButtons?.errorCancel!!
  private val errorBack: WalletButtonView
    get() = bindingCreditCardLayout?.errorButtons?.errorBack!!
  private val errorTryAgain: WalletButtonView
    get() = bindingCreditCardLayout?.errorButtons?.errorTryAgain!!

  // iab_error_layout.xml
  private val errorDismiss: WalletButtonView
    get() = bindingCreditCardLayout?.fragmentIabError?.errorDismiss!!

  // support_error_layout.xml
  private val errorMessage: TextView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorMessage
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorMessage!!
  private val errorVerifyWalletButton: WalletButtonView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorVerifyWalletButton
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorVerifyWalletButton!!

  private val errorVerifyCardButton: WalletButtonView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.errorVerifyCardButton
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.errorVerifyCardButton!!

  private val layoutSupportLogo: ImageView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.layoutSupportLogo
      ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.layoutSupportLogo!!
  private val layoutSupportIcn: ImageView
    get() = bindingCreditCardLayout?.fragmentAdyenError?.layoutSupportIcn
        ?: bindingCreditCardLayout?.fragmentIabError?.genericErrorLayout?.layoutSupportIcn!!

  // view_purchase_bonus.xml
  private val bonusValue: TextView get() = bindingCreditCardLayout?.bonusLayout?.bonusValue!!

  // selected_payment_method_cc.xml
  private val adyenCardFormPreSelected: CardView
    get() = bindingCreditCardLayout?.adyenCardForm?.adyenCardFormPreSelected!!
  private val paymentMethodIc: ImageView
    get() = bindingCreditCardLayout?.adyenCardForm?.paymentMethodIc!!
  private val adyenCardFormPreSelectedNumber: TextView
    get() = bindingCreditCardLayout?.adyenCardForm?.adyenCardFormPreSelectedNumber!!

  // payment_methods_header.xml
  private val appIcon: ImageView
    get() = bindingCreditCardLayout?.paymentMethodsHeader?.appIcon!!
  private val appName: TextView
    get() =
      bindingCreditCardLayout?.paymentMethodsHeader?.appName!!
  private val appSkuDescription: TextView
    get() = bindingCreditCardLayout?.paymentMethodsHeader?.appSkuDescription!!
  private val fiatPrice: TextView
    get() = bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice!!

  // fragment_iab_transaction_completed.xml
  private val lottieTransactionSuccess: LottieAnimationView
    get() = bindingCreditCardLayout?.fragmentIabTransactionCompleted?.lottieTransactionSuccess!!
  private val transactionSuccessBonusText: TextView
    get() = bindingCreditCardLayout?.fragmentIabTransactionCompleted?.transactionSuccessBonusText!!
  private val bonusSuccessLayout: LinearLayout
    get() = bindingCreditCardLayout?.fragmentIabTransactionCompleted?.bonusSuccessLayout!!
  private val nextPaymentDate1: TextView
    get() = bindingCreditCardLayout?.fragmentIabTransactionCompleted?.nextPaymentDate!!

  // adyen_credit_card_layout.xml and adyen_credit_card_pre_selected.xml
  private val fragmentCreditCardAuthorizationProgressBar: LottieAnimationView
    get() = bindingCreditCardLayout?.fragmentCreditCardAuthorizationProgressBar!!
  private val makingPurchaseText: TextView
    get() = bindingCreditCardLayout?.makingPurchaseText!!
  private val fiatPriceSkeleton: ShimmerFrameLayout
    get() = bindingCreditCardLayout?.paymentMethodsHeader?.fiatPriceSkeleton?.root!!
  private val appcPriceSkeleton: ShimmerFrameLayout
    get() = bindingCreditCardLayout?.paymentMethodsHeader?.appcPriceSkeleton?.root!!
  private val iabActivityTransactionCompleted: ConstraintLayout
    get() = bindingCreditCardLayout?.fragmentIabTransactionCompleted?.iabActivityTransactionCompleted!!

  // adyen_credit_card_layout.xml
  private val adyenCreditCardRoot: RelativeLayout?
    get() = bindingCreditCardLayout?.adyenCreditCardRoot
  private val mainView: RelativeLayout? get() = bindingCreditCardLayout?.mainView
  private val creditCardInfo: ConstraintLayout? get() = bindingCreditCardLayout?.creditCardInfo
  private val changeCardButton: WalletButtonView?
    get() = bindingCreditCardLayout?.changeCardButton
  private val bonusLayout: ConstraintLayout? get() = bindingCreditCardLayout?.bonusLayout?.root
  private val adyenCardForm: ConstraintLayout?
    get() = bindingCreditCardLayout?.adyenCardForm?.root
  private val fragmentAdyenError: ConstraintLayout?
    get() = bindingCreditCardLayout?.fragmentAdyenError?.root
  private val fragmentAdyenNoNetworkError: ConstraintLayout?
    get() = bindingCreditCardLayout?.noNetworkErrorLayout?.root
  private val errorButtons: LinearLayout? get() = bindingCreditCardLayout?.errorButtons?.root

  // adyen_credit_card_pre_selected.xml
  private val mainViewPreSelected: RelativeLayout? get() = bindingCreditCardLayout?.mainView
  private val paymentMethods: ConstraintLayout? get() = bindingCreditCardLayout?.creditCardInfo
  private val changeCardButtonPreSelected: WalletButtonView?
    get() = bindingCreditCardLayout?.changeCardButton
  private val morePaymentMethods: WalletButtonView?
    get() = bindingCreditCardLayout?.morePaymentMethods

  private val morePaymentStoredMethods: WalletButtonView?
    get() = bindingCreditCardLayout?.morePaymentStoredMethods
  private val bonusLayoutPreSelected: ConstraintLayout?
    get() = bindingCreditCardLayout?.bonusLayout?.root
  private val layoutPreSelected: ConstraintLayout?
    get() = bindingCreditCardLayout?.adyenCardForm?.root
  private val fragmentAdyenErrorPreSelected: ConstraintLayout?
    get() = bindingCreditCardLayout?.fragmentAdyenError?.root
  private val fragmentAdyenNoNetworkErrorPreSelected: ConstraintLayout?
    get() = bindingCreditCardLayout?.noNetworkErrorLayout?.root
  private val dialogBuyButtonsError: LinearLayout?
    get() = bindingCreditCardLayout?.errorButtons?.root
  private val imgStoredCardBrand: ImageView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.imgCardBrand
  private val txtStoredCardNumber: TextView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.txtSavedCardNumber
  private val txtStoredExpiryDate: TextView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.txtSavedCardExpiryDate

  private val scrollPayment: ScrollView?
    get() = bindingCreditCardLayout?.ccInfoView
  private val btnStoredCardPreSelectedChangeCard: WalletButtonView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.storedChangeCardButton
  private val btnStoredCardChangeCard: WalletButtonView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.storedChangeCardButton
  private val layoutAdyenStoredCard: ConstraintLayout?
    get() = bindingCreditCardLayout?.adyenSavedCard?.root

  private val bottomSeparator: SeparatorView? get() = bindingCreditCardLayout?.bottomSeparator

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
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
  ): View = AdyenCreditCardLayoutBinding.inflate(inflater).root

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
    adyenCreditCardRoot?.layoutParams?.width = dimensionInPixels
    adyenCreditCardRoot?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT


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
    adyenCardView = AdyenCardView(adyenCardFormPreSelected)
    setupTransactionComplete()
    handleBuyButtonText()
    if (paymentType == PaymentType.CARD.name) setupCardConfiguration(hideCvcStoredCard = false)
    setupRedirectConfiguration()
    setupAdyen3DS2ConfigurationBuilder()

    handlePreSelectedView()
    handleBonusAnimation()

    showProduct()
  }

  override fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    this.isStored = paymentInfoModel.isStored
    buyButton.visibility = VISIBLE
    cancelButton.visibility = VISIBLE
    if (forget) askCVC = true
    setupCardConfiguration(!askCVC)
    (paymentInfoModel.paymentMethod as? StoredPaymentMethod)?.let { setStoredCardLayoutValues(it) }

    prepareCardComponent(paymentInfoModel, forget)
    handleLayoutVisibility(isStored)
    setStoredPaymentInformation(isStored)
  }

  private fun setStoredCardLayoutValues(storedPaymentMethod: StoredPaymentMethod) {
    txtStoredCardNumber?.text = "**** ".plus(storedPaymentMethod.lastFour)
    txtStoredExpiryDate?.text =
      getString(R.string.dialog_expiry_date).plus(" ").plus(storedPaymentMethod.expiryMonth)
        .plus("/").plus(storedPaymentMethod.expiryYear)
    when (storedPaymentMethod.brand) {
      PaymentBrands.MASTERCARD.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_brand_master_card)
      }

      PaymentBrands.VISA.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_brand_visa)
      }

      PaymentBrands.AMEX.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_brand_american_express)
      }

      PaymentBrands.MAESTRO.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_branc_maestro)
      }

      PaymentBrands.DINERS.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_brand_diners_club)
      }

      PaymentBrands.DISCOVER.brandName -> {
        imgStoredCardBrand?.setImageResource(ic_card_brand_discover)
      }

      else -> {
        imgStoredCardBrand?.setColorFilter(R.color.styleguide_dark_grey)
      }
    }
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
      mainViewPreSelected?.visibility = VISIBLE
      mainView?.visibility = VISIBLE
    } else {
      showMoreMethods()
    }
  }

  override fun getAnimationDuration() = lottieTransactionSuccess.duration * 3

  override fun showProduct() {
    try {
      appIcon.setImageDrawable(
        requireContext().packageManager.getApplicationIcon(
          transactionBuilder.domain
        )
      )
      appName.text = getApplicationName(transactionBuilder.domain)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    appSkuDescription.text = skuDescription
  }

  override fun showLoading() {
    fragmentCreditCardAuthorizationProgressBar.visibility = VISIBLE
    if (bonus.isNotEmpty()) {
      bonusLayout?.visibility = INVISIBLE
    }
    adyenCardForm?.visibility = INVISIBLE
    changeCardButton?.visibility = INVISIBLE
    layoutAdyenStoredCard?.visibility = INVISIBLE
    morePaymentMethods?.visibility = GONE
    morePaymentStoredMethods?.visibility = INVISIBLE
    cancelButton.visibility = INVISIBLE
    buyButton.visibility = INVISIBLE
    fiatPriceSkeleton.visibility = GONE
    appcPriceSkeleton.visibility = GONE
  }

  override fun showLoadingMakingPayment() {
    showLoading()
    makingPurchaseText.visibility = VISIBLE
  }

  override fun hideLoadingAndShowView() {
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    showBonus()
    adyenCardForm?.visibility = VISIBLE
    cancelButton.visibility = VISIBLE
  }

  override fun showNetworkError() = showNoNetworkError()

  override fun backEvent(): Observable<Any> =
    RxView.clicks(cancelButton).mergeWith(iabView.backButtonPress())

  override fun showSuccess(renewal: Date?) {
    iabActivityTransactionCompleted.visibility = VISIBLE
    if (isSubscription && renewal != null) {
      nextPaymentDate1.visibility = VISIBLE
      setBonusMessage(renewal)
    }
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    creditCardInfo?.visibility = GONE
    lottieTransactionSuccess.visibility = VISIBLE
    fragmentAdyenError?.visibility = GONE
    fragmentAdyenErrorPreSelected?.visibility = GONE
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

  override fun showSpecificError(@StringRes stringRes: Int, backToCard: Boolean) {
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    cancelButton.visibility = GONE
    buyButton.visibility = GONE
    paymentMethods?.visibility = VISIBLE
    bonusLayoutPreSelected?.visibility = GONE
    bonusLayout?.visibility = GONE
    morePaymentMethods?.visibility = GONE
    morePaymentStoredMethods?.visibility = GONE
    layoutAdyenStoredCard?.visibility = GONE
    adyenCardForm?.visibility = GONE
    layoutPreSelected?.visibility = GONE
    changeCardButton?.visibility = GONE
    changeCardButtonPreSelected?.visibility = GONE
    bottomSeparator?.visibility = GONE

    errorButtons?.visibility = VISIBLE
    dialogBuyButtonsError?.visibility = VISIBLE

    errorBack.visibility = if (backToCard) VISIBLE else GONE
    errorTryAgain.visibility = if (backToCard) GONE else VISIBLE

    val message = getString(stringRes)

    errorMessage.text = message
    fragmentAdyenError?.visibility = VISIBLE
    fragmentAdyenErrorPreSelected?.visibility = VISIBLE
  }

  override fun showNoNetworkError(backToCard: Boolean) {
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    cancelButton.visibility = GONE
    buyButton.visibility = GONE
    paymentMethods?.visibility = VISIBLE
    bonusLayoutPreSelected?.visibility = GONE
    bonusLayout?.visibility = GONE
    morePaymentMethods?.visibility = GONE
    morePaymentStoredMethods?.visibility = GONE
    layoutAdyenStoredCard?.visibility = GONE
    adyenCardForm?.visibility = GONE
    layoutPreSelected?.visibility = GONE
    changeCardButton?.visibility = GONE
    changeCardButtonPreSelected?.visibility = GONE
    bottomSeparator?.visibility = GONE

    errorButtons?.visibility = VISIBLE
    dialogBuyButtonsError?.visibility = VISIBLE

    errorBack.visibility = if (backToCard) VISIBLE else GONE
    errorTryAgain.visibility = if (backToCard) GONE else VISIBLE

    fragmentAdyenError?.visibility = GONE
    fragmentAdyenErrorPreSelected?.visibility = GONE

    fragmentAdyenNoNetworkError?.visibility = VISIBLE
    fragmentAdyenNoNetworkErrorPreSelected?.visibility = VISIBLE
  }

  override fun showVerificationError(isWalletVerified: Boolean) {
    if (isWalletVerified) {
      showSpecificError(R.string.purchase_error_verify_card)
      errorVerifyWalletButton.visibility = GONE
      errorVerifyCardButton.visibility = VISIBLE
    } else {
      showSpecificError(R.string.purchase_error_verify_wallet)
      errorVerifyWalletButton.visibility = VISIBLE
      errorVerifyCardButton.visibility = GONE
    }
  }

  override fun showCvvError() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (isStored) {
      changeCardButton?.visibility = VISIBLE
      changeCardButtonPreSelected?.visibility = VISIBLE
    }
    buyButton.visibility = VISIBLE
    buyButton.isEnabled = false
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }

  override fun showBackToCard() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (askCVC && isStored) {
      changeCardButton?.visibility = VISIBLE
      changeCardButtonPreSelected?.visibility = VISIBLE
      morePaymentMethods?.visibility = VISIBLE
    } else if (isStored) {
      layoutAdyenStoredCard?.visibility = VISIBLE
      morePaymentStoredMethods?.visibility = VISIBLE
    }
    buyButton.visibility = VISIBLE

    errorButtons?.visibility = GONE
    dialogBuyButtonsError?.visibility = GONE

    errorBack.visibility = VISIBLE
    errorTryAgain.visibility = GONE

    fragmentAdyenError?.visibility = GONE
    fragmentAdyenErrorPreSelected?.visibility = GONE

  }

  override fun getMorePaymentMethodsClicks() = RxView.clicks(morePaymentMethods!!)

  override fun getMorePaymentMethodsStoredClicks() = RxView.clicks(morePaymentStoredMethods!!)

  override fun showMoreMethods() {
    mainView?.let { KeyboardUtils.hideKeyboard(it) }
    mainViewPreSelected?.let { KeyboardUtils.hideKeyboard(it) }
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
    return if (changeCardButton != null) RxView.clicks(changeCardButton!!)
    else RxView.clicks(changeCardButtonPreSelected!!)
  }

  override fun forgetStoredCardClick() =
    RxView.clicks(btnStoredCardPreSelectedChangeCard ?: btnStoredCardChangeCard!!)

  @SuppressLint("SetTextI18n")
  override fun showProductPrice(amount: String, currencyCode: String) {
    var fiatText = "$amount $currencyCode"
    if (isSubscription) {
      val period = Period.parse(frequency!!)
      period?.mapToSubsFrequency(requireContext(), fiatText)
        ?.let { fiatText = it }
    }
    fiatPrice.text = getString(R.string.purchase_total_header, amount, currencyCode)
    fiatPriceSkeleton.visibility = GONE
    appcPriceSkeleton.visibility = GONE
    fiatPrice.visibility = VISIBLE
  }

  override fun adyenErrorBackClicks() = RxView.clicks(errorTryAgain)

  override fun adyenErrorBackToCardClicks() = RxView.clicks(errorBack)
  override fun adyenErrorCancelClicks() = RxView.clicks(errorCancel)

  override fun errorDismisses() = RxView.clicks(errorDismiss)

  override fun buyButtonClicked() = RxView.clicks(buyButton)

  override fun close(bundle: Bundle?) = iabView.close(bundle)

  // TODO: Refactor this to pass the whole Intent.
  // TODO: Currently this relies on the fact that Adyen 4.4.0 internally uses only Intent.getData().
  override fun submitUriResult(uri: Uri) = redirectComponent.handleIntent(Intent("", uri))

  override fun getPaymentDetails(): Observable<AdyenComponentResponseModel> =
    paymentDetailsSubject!!

  override fun getAdyenSupportLogoClicks() = RxView.clicks(layoutSupportLogo)

  override fun getAdyenSupportIconClicks() = RxView.clicks(layoutSupportIcn)

  override fun getVerificationClicks(): Observable<Boolean> =
    Observable.merge(
      RxView.clicks(errorVerifyWalletButton).map { false },
      RxView.clicks(errorVerifyCardButton).map { true }
    )

  override fun lockRotation() = iabView.lockRotation()

  override fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  override fun handleCreditCardNeedCVC(newState: Boolean) {
    askCVC = newState
  }

  override fun shouldStoreCard(): Boolean {
    return adyenCardView.cardSave
  }

  private fun setupCardConfiguration(hideCvcStoredCard: Boolean) {
    cardConfiguration = CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setHideCvcStoredCard(hideCvcStoredCard)
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
      transactionSuccessBonusText.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      bonusSuccessLayout.visibility = GONE
    }
  }

  private fun showBonus() {
    if (bonus.isNotEmpty()) {
      bonusLayout?.visibility = VISIBLE
      bonusLayoutPreSelected?.visibility = VISIBLE
      bonusValue.text = getString(R.string.gamification_purchase_header_part_2, bonus)
    } else {
      bonusLayout?.visibility = GONE
      bonusLayoutPreSelected?.visibility = GONE
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    if (askCVC && isStored) {
      changeCardButton?.visibility = VISIBLE
      changeCardButtonPreSelected?.visibility = VISIBLE
      morePaymentMethods?.visibility = VISIBLE
    } else if (isStored) {
      scrollPayment?.visibility = GONE
      layoutAdyenStoredCard?.visibility = VISIBLE
      morePaymentStoredMethods?.visibility = VISIBLE
    } else {
      scrollPayment?.visibility = VISIBLE
      layoutAdyenStoredCard?.visibility = GONE
      changeCardButton?.visibility = GONE
      changeCardButtonPreSelected?.visibility = GONE
      adyenCardForm?.visibility = VISIBLE
      morePaymentMethods?.visibility = if (isPreSelected) VISIBLE else GONE
      morePaymentStoredMethods?.visibility = GONE
    }
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
    adyenCardFormPreSelected.attach(cardComponent, this)
    cardComponent.observe(this) {
      adyenCardView.setError(null)
      if (it != null && it.isValid) {
        buyButton.isEnabled = true
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
        buyButton.isEnabled = false
      }
    }
  }

  private fun setStoredPaymentInformation(isStored: Boolean) {
    if (isStored) {
      adyenCardFormPreSelectedNumber.text = adyenCardView.cardNumber
      adyenCardFormPreSelectedNumber.visibility = VISIBLE
      paymentMethodIc.setImageDrawable(adyenCardView.cardImage)
    } else {
      adyenCardFormPreSelectedNumber.visibility = GONE
      paymentMethodIc.visibility = GONE
    }
  }

  private fun handleBonusAnimation() {
    if (StringUtils.isNotBlank(bonus)) {
      lottieTransactionSuccess.setAnimation(R.raw.success_animation)
      setupTransactionComplete()
    } else {
      lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    }
  }

  private fun handlePreSelectedView() {
    if (!isPreSelected) {
      cancelButton.setText(getString(R.string.back_button))
      iabView.disableBack()
    }
    showBonus()
  }

  private fun setBonusMessage(nextPaymentDate: Date) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(nextPaymentDate)
    val nextPaymentText =
      "${getString(R.string.subscriptions_details_next_payment_title)} $formattedDate"
    nextPaymentDate1.text = nextPaymentText
  }

  private fun handleBuyButtonText() {
    when {
      transactionBuilder.type.equals(
        TransactionData.TransactionType.DONATION.name,
        ignoreCase = true
      ) -> {
        buyButton.setText(getString(R.string.action_donate))
      }

      transactionBuilder.type.equals(
        TransactionData.TransactionType.INAPP_SUBSCRIPTION.name,
        ignoreCase = true
      ) -> buyButton.setText(getString(R.string.subscriptions_subscribe_button))

      else -> {
        buyButton.setText(getString(R.string.action_buy))
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
    super.onDestroy()
  }

  override fun restartFragment() {
    this.fragmentManager?.beginTransaction()?.replace(
      R.id.fragment_container,
      newInstance(
        PaymentType.CARD,
        origin,
        transactionBuilder,
        amount,
        currency,
        bonus,
        isPreSelected,
        gamificationLevel,
        skuDescription,
        isSubscription,
        skills,
        frequency
      )
    )?.commit()
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
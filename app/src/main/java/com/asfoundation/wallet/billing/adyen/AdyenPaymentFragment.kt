package com.asfoundation.wallet.billing.adyen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.appcoins.wallet.core.utils.android_common.extensions.getParcelableExtra
import com.appcoins.wallet.core.utils.android_common.extensions.getSerializableExtra
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.sharedpreferences.CardPaymentDataSource
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
import com.asfoundation.wallet.billing.adyen.enums.PaymentStateEnum
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.manage_cards.models.StoredCard
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AdyenPaymentFragment : BasePageViewFragment() {

  private val viewModel: AdyenPaymentViewModel by viewModels()

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
  lateinit var adyenEnvironment: Environment

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var servicesErrorMapper: ServicesErrorCodeMapper

  @Inject
  lateinit var cardPaymentDataSource: CardPaymentDataSource

  @Inject
  lateinit var getCachedShowRefundDisclaimerUseCase: GetCachedShowRefundDisclaimerUseCase

  @Inject
  lateinit var logger: Logger
  private lateinit var iabView: IabView
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

  private val scrollPayment: ScrollView?
    get() = bindingCreditCardLayout?.ccInfoView
  private val btnStoredCardOpenCloseCardList: ImageView?
    get() = bindingCreditCardLayout?.adyenSavedCard?.storedCardOpenCloseCardList
  private val layoutAdyenStoredCard: ConstraintLayout?
    get() = bindingCreditCardLayout?.adyenSavedCard?.root

  private val bottomSeparator: SeparatorView? get() = bindingCreditCardLayout?.bottomSeparator

  private var isExpandedCardsList: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    paymentDataSubject = ReplaySubject.createWithSize(1)
    paymentDetailsSubject = PublishSubject.create()
    adyen3DSErrorSubject = PublishSubject.create()
    val navigator = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    compositeDisposable = CompositeDisposable()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View = AdyenCreditCardLayoutBinding.inflate(inflater).root

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.singleEventState.collect { event ->
          when (event) {
            AdyenPaymentViewModel.SingleEventState.setup3DSComponent -> setup3DSComponent()
            AdyenPaymentViewModel.SingleEventState.setupRedirectComponent -> setupRedirectComponent()
            AdyenPaymentViewModel.SingleEventState.showLoading -> showLoading()
            AdyenPaymentViewModel.SingleEventState.showGenericError -> showGenericError()
            AdyenPaymentViewModel.SingleEventState.hideLoadingAndShowView -> hideLoadingAndShowView()
            AdyenPaymentViewModel.SingleEventState.showNetworkError -> showNetworkError()
            is AdyenPaymentViewModel.SingleEventState.finishCardConfiguration -> finishCardConfiguration(
              paymentInfoModel = event.paymentInfoModel, forget = event.forget
            )

            AdyenPaymentViewModel.SingleEventState.restartFragment -> restartFragment()
            is AdyenPaymentViewModel.SingleEventState.showProductPrice -> showProductPrice(
              amount = event.amount, currencyCode = event.currencyCode
            )

            AdyenPaymentViewModel.SingleEventState.lockRotation -> lockRotation()
            AdyenPaymentViewModel.SingleEventState.showLoadingMakingPayment -> showLoadingMakingPayment()
            AdyenPaymentViewModel.SingleEventState.hideKeyboard -> hideKeyboard()
            AdyenPaymentViewModel.SingleEventState.showCvvError -> showCvvError()
            AdyenPaymentViewModel.SingleEventState.showMoreMethods -> showMoreMethods()
            is AdyenPaymentViewModel.SingleEventState.showSuccess -> showSuccess(event.renewal)
            is AdyenPaymentViewModel.SingleEventState.showSpecificError -> showSpecificError(
              stringRes = event.stringRes, backToCard = event.backToCard
            )

            is AdyenPaymentViewModel.SingleEventState.showVerificationError -> showVerificationError(
              isWalletVerified = event.isWalletVerified
            )

            is AdyenPaymentViewModel.SingleEventState.showVerification -> showVerification(
              isWalletVerified = event.isWalletVerified,
              paymentType = event.paymentType
            )

            is AdyenPaymentViewModel.SingleEventState.handleCreditCardNeedCVC -> handleCreditCardNeedCVC(
              needCVC = event.needCVC
            )

            is AdyenPaymentViewModel.SingleEventState.close -> close(event.bundle ?: Bundle())
            is AdyenPaymentViewModel.SingleEventState.submitUriResult -> submitUriResult(event.uri)
            AdyenPaymentViewModel.SingleEventState.showBackToCard -> showBackToCard()
            is AdyenPaymentViewModel.SingleEventState.handle3DSAction -> handle3DSAction(
              action = event.action
            )

            AdyenPaymentViewModel.SingleEventState.showInvalidCardError -> showInvalidCardError()
            AdyenPaymentViewModel.SingleEventState.showSecurityValidationError -> showSecurityValidationError()
            AdyenPaymentViewModel.SingleEventState.showOutdatedCardError -> showOutdatedCardError()
            AdyenPaymentViewModel.SingleEventState.showAlreadyProcessedError -> showAlreadyProcessedError()
            AdyenPaymentViewModel.SingleEventState.showPaymentError -> showPaymentError()
            AdyenPaymentViewModel.SingleEventState.showCvcRequired -> showCvcRequired()
          }
        }
      }
    }

    setupUi()

    val orientation = this.resources.configuration.orientation
    val dpWidth = if (orientation == Configuration.ORIENTATION_LANDSCAPE) 592F else 340F

    val dimensionInPixels = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP, dpWidth, resources.displayMetrics
    ).toInt()
    adyenCreditCardRoot?.layoutParams?.width = dimensionInPixels
    adyenCreditCardRoot?.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT

    viewModel.initialize(
      savedInstanceState = savedInstanceState,
      paymentData = AdyenPaymentViewModel.PaymentData(
        returnUrl = RedirectComponent.getReturnUrl(requireContext()),
        origin = origin,
        transactionBuilder = transactionBuilder,
        paymentType = paymentType,
        amount = amount,
        currency = currency,
        skills = skills,
        isPreSelected = isPreSelected,
        gamificationLevel = gamificationLevel,
        navigator = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView),
        iabView = iabView
      ),
      adyenSupportIconClicks = RxView.clicks(layoutSupportIcn),
      adyenSupportLogoClicks = RxView.clicks(layoutSupportLogo),
      retrievePaymentData = retrievePaymentData(),
      buyButtonClicked = buyButtonClicked(),
      verificationClicks = getVerificationClicks(),
      paymentDetails = getPaymentDetails(),
      onAdyen3DSError = onAdyen3DSError(),
      errorDismisses = RxView.clicks(errorDismiss),
      backEvent = RxView.clicks(cancelButton).mergeWith(iabView.backButtonPress()),
      morePaymentMethodsClicks = getMorePaymentMethodsClicks(),
      adyenErrorBackClicks = RxView.clicks(errorTryAgain),
      adyenErrorBackToCardClicks = RxView.clicks(errorBack),
      adyenErrorCancelClicks = RxView.clicks(errorCancel),
      paymentStateEnumArgs = paymentStateEnum
    )
  }

  fun setup3DSComponent() {
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
    btnStoredCardOpenCloseCardList?.setOnClickListener {
      isExpandedCardsList = !viewModel.cardsList.isNullOrEmpty() && !isExpandedCardsList
      changeVisibilityOfStoredCardList(false)
    }

    showProduct()
  }

  private fun changeVisibilityOfStoredCardList(isNewCardAdded: Boolean) {
    if (isExpandedCardsList) {
      paymentAnalytics.sendShowStoredCardList()
      bindingCreditCardLayout?.composeView?.visibility = VISIBLE
      bindingCreditCardLayout?.adyenSavedCard?.txtSelectPaymentCard?.visibility = VISIBLE
      txtStoredCardNumber?.visibility = GONE
      imgStoredCardBrand?.visibility = GONE

      morePaymentStoredMethods?.visibility = GONE
      btnStoredCardOpenCloseCardList?.rotation = 180F
      bindingCreditCardLayout?.adyenSavedCard?.root?.background =
        resources.getDrawable(R.drawable.background_top_corner_white)
      if (isPortraitMode(requireContext())) {
        bindingCreditCardLayout?.bonusLayout?.root?.visibility = GONE
        val layoutParams =
          bindingCreditCardLayout?.composeView?.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = when (viewModel.cardsList.size) {
          1 -> {
            if (cardPaymentDataSource.isGotItVisible()) {
              160.toPx(requireContext())
            } else {
              100.toPx(requireContext())
            }
          }

          2 -> {
            if (cardPaymentDataSource.isGotItVisible()) {
              200.toPx(requireContext())
            } else {
              142.toPx(requireContext())
            }
          }

          3 -> {
            if (cardPaymentDataSource.isGotItVisible()) {
              240.toPx(requireContext())
            } else {
              184.toPx(requireContext())
            }
          }

          else -> {
            if (cardPaymentDataSource.isGotItVisible()) {
              220.toPx(requireContext())
            } else {
              163.toPx(requireContext())
            }
          }
        }
        bindingCreditCardLayout?.composeView?.layoutParams = layoutParams
        bindingCreditCardLayout?.composeView?.requestLayout()
      }
    } else {
      bindingCreditCardLayout?.composeView?.visibility = GONE
      bindingCreditCardLayout?.adyenSavedCard?.txtSelectPaymentCard?.visibility = GONE
      btnStoredCardOpenCloseCardList?.rotation = 0F
      bindingCreditCardLayout?.adyenSavedCard?.root?.background =
        resources.getDrawable(R.drawable.background_corner_white)
      if (!isNewCardAdded) {
        txtStoredCardNumber?.visibility = VISIBLE
        imgStoredCardBrand?.visibility = VISIBLE
        morePaymentStoredMethods?.visibility = VISIBLE
        bindingCreditCardLayout?.bonusLayout?.root?.visibility = if (!isFreeTrial) VISIBLE else GONE
      }

    }
  }

  fun Int.toPx(context: Context) =
    this * context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT

  private fun isPortraitMode(context: Context): Boolean {
    val orientation = context.resources.configuration.orientation
    return orientation == Configuration.ORIENTATION_PORTRAIT
  }

  fun finishCardConfiguration(paymentInfoModel: PaymentInfoModel, forget: Boolean) {
    requireView().findViewById<ComposeView>(R.id.composeView).apply {
      setContent {
        ShowCardListExpandedLayout()
      }
    }
    viewModel.isStored = paymentInfoModel.isStored
    buyButton.visibility = VISIBLE
    cancelButton.visibility = VISIBLE
    if (forget) viewModel.askCVC = true
    setupCardConfiguration(!viewModel.askCVC)
    (paymentInfoModel.paymentMethod as? StoredPaymentMethod)?.let { setStoredCardLayoutValues(it) }

    prepareCardComponent(paymentInfoModel, forget)
    handleLayoutVisibility(viewModel.isStored)
    setStoredPaymentInformation(viewModel.isStored)
  }

  private fun setStoredCardLayoutValues(storedPaymentMethod: StoredPaymentMethod) {
    txtStoredCardNumber?.text = "**** ".plus(storedPaymentMethod.lastFour)
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

  fun retrievePaymentData() = paymentDataSubject!!

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

  fun showProduct() {
    try {
      bindingCreditCardLayout?.paymentMethodsHeader?.appIcon?.setImageDrawable(
        requireContext().packageManager.getApplicationIcon(
          transactionBuilder.domain
        )
      )
      bindingCreditCardLayout?.paymentMethodsHeader?.appName?.text =
        getApplicationName(transactionBuilder.domain)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    bindingCreditCardLayout?.paymentMethodsHeader?.appSkuDescription?.text = skuDescription
  }

  fun showLoading() {
    fragmentCreditCardAuthorizationProgressBar.visibility = VISIBLE
    if (bonus.isNotEmpty()) {
      bonusLayout?.visibility = INVISIBLE
    }
    adyenCardForm?.visibility = INVISIBLE
    layoutAdyenStoredCard?.visibility = INVISIBLE
    morePaymentMethods?.visibility = GONE
    morePaymentStoredMethods?.visibility = INVISIBLE
    cancelButton.visibility = INVISIBLE
    buyButton.visibility = INVISIBLE
    fiatPriceSkeleton.visibility = GONE
    appcPriceSkeleton.visibility = GONE
    bindingCreditCardLayout?.composeView?.visibility = GONE
    bindingCreditCardLayout?.cvLegalDisclaimer?.visibility = GONE
    bindingCreditCardLayout?.tvLegalDisclaimer?.visibility = GONE
  }

  fun showLoadingMakingPayment() {
    showLoading()
    makingPurchaseText.visibility = VISIBLE
  }

  fun hideLoadingAndShowView() {
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    showBonus()
    adyenCardForm?.visibility = VISIBLE
    cancelButton.visibility = VISIBLE
    bindingCreditCardLayout?.cvLegalDisclaimer?.visibility =
      if (getCachedShowRefundDisclaimerUseCase()) VISIBLE else GONE
    bindingCreditCardLayout?.tvLegalDisclaimer?.visibility =
      if (getCachedShowRefundDisclaimerUseCase()) VISIBLE else GONE
  }

  fun showNetworkError() = showNoNetworkError()

  fun showSuccess(renewal: Date?) {
    iabActivityTransactionCompleted.visibility = VISIBLE
    if (isSubscription && renewal != null) {
      bindingCreditCardLayout?.fragmentIabTransactionCompleted?.nextPaymentDate?.visibility =
        VISIBLE
      setBonusMessage(renewal)
    }
    fragmentCreditCardAuthorizationProgressBar.visibility = GONE
    makingPurchaseText.visibility = GONE
    creditCardInfo?.visibility = GONE
    bindingCreditCardLayout?.fragmentIabTransactionCompleted?.lottieTransactionSuccess?.visibility =
      VISIBLE
    fragmentAdyenError?.visibility = GONE
    fragmentAdyenErrorPreSelected?.visibility = GONE
  }

  fun showGenericError() = showSpecificError(R.string.unknown_error)

  fun showInvalidCardError() = showSpecificError(R.string.purchase_error_invalid_credit_card)

  fun showSecurityValidationError() =
    showSpecificError(R.string.purchase_error_card_security_validation)

  fun showOutdatedCardError() = showSpecificError(R.string.purchase_card_error_re_insert)

  fun showAlreadyProcessedError() =
    showSpecificError(R.string.purchase_error_card_already_in_progress)

  fun showPaymentError() = showSpecificError(R.string.purchase_error_payment_rejected)

  fun showVerification(isWalletVerified: Boolean, paymentType: String) =
    if (paymentType == PaymentType.PAYPAL.name) iabView.showPayPalVerification()
    else iabView.showCreditCardVerification(isWalletVerified)

  fun showSpecificError(@StringRes stringRes: Int, backToCard: Boolean = false) {
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

  fun showNoNetworkError(backToCard: Boolean = false) {
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

  fun showVerificationError(isWalletVerified: Boolean) {
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

  fun showCvvError() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    buyButton.visibility = VISIBLE
    buyButton.isEnabled = false
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }

  fun showBackToCard() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (viewModel.askCVC && viewModel.isStored) {
      morePaymentMethods?.visibility = VISIBLE
    } else if (viewModel.isStored) {
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

  fun showCvcRequired() {
    iabView.unlockRotation()
    hideLoadingAndShowView()
    if (viewModel.askCVC && viewModel.isStored) {
      scrollPayment?.visibility = VISIBLE
      val editTextCvc = adyenCardView.adyenSecurityCodeLayout?.editText
      editTextCvc?.setTextIsSelectable(true)
      editTextCvc?.requestFocus()
      editTextCvc?.post {
        if (editTextCvc.hasFocus() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          context?.getSystemService(InputMethodManager::class.java)
            ?.showSoftInput(editTextCvc, InputMethodManager.SHOW_IMPLICIT)
        }
      }
      morePaymentMethods?.visibility = VISIBLE
      layoutAdyenStoredCard?.visibility = GONE
      morePaymentStoredMethods?.visibility = GONE
    } else if (viewModel.isStored) {
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

  fun getMorePaymentMethodsClicks() =
    RxView.clicks(morePaymentMethods!!).mergeWith(RxView.clicks(morePaymentStoredMethods!!))

  fun showMoreMethods() {
    mainView?.let { KeyboardUtils.hideKeyboard(it) }
    mainViewPreSelected?.let { KeyboardUtils.hideKeyboard(it) }
    iabView.unlockRotation()
    iabView.showPaymentMethodsView()
  }

  fun setupRedirectComponent() {
    activity?.application?.let { application ->
      redirectComponent = RedirectComponent.PROVIDER.get(this, application, redirectConfiguration)
      redirectComponent.observe(this) {
        paymentDetailsSubject?.onNext(AdyenComponentResponseModel(it.details, it.paymentData))
      }
    }
  }


  fun handle3DSAction(action: Action) {
    adyen3DS2Component.handleAction(requireActivity(), action)
  }

  fun onAdyen3DSError(): Observable<String> = adyen3DSErrorSubject!!


  @SuppressLint("SetTextI18n")
  fun showProductPrice(
    amount: String,
    currencyCode: String,
  ) {
    var fiatText = "$amount $currencyCode"
    val period = Period.parse(frequency ?: "")
    period?.mapToSubsFrequency(requireContext(), fiatText)?.let { fiatText = it }
    val freeTrialPeriod = try {
      Period.parse(freeTrialDuration ?: "")
    } catch (e: Exception) {
      null
    }
    if (isSubscription) {
      if (isFreeTrial) {
        showFreeTrialInfo(
          period = period ?: Period(0, 0, 0, 1),
          freeTrialPeriod = freeTrialPeriod ?: Period(0, 0, 0, 1),
          startingDate = subscriptionStartingDate ?: "",
          price = "$amount $currency"
        )
        bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice?.text =
          if (isPortraitMode(requireContext())) {
            getString(R.string.purchase_total_header, "0.00", currencyCode)
          } else {
            getString(R.string.gas_price_value, "0.00", currencyCode)
          }
      } else {
        val period = Period.parse(frequency!!)
        bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice?.text =
          if (isPortraitMode(requireContext())) {
            getString(R.string.purchase_total_header, fiatText, "")
          } else {
            fiatText
          }
      }
    } else {
      bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice?.text =
        if (isPortraitMode(requireContext())) {
          getString(R.string.purchase_total_header, amount, currencyCode)
        } else {
          getString(R.string.gas_price_value, amount, currencyCode)
        }
    }
    if (!isPortraitMode(requireContext())) {
      bindingCreditCardLayout?.paymentMethodsHeader?.fiatTotalPriceLabel?.visibility = VISIBLE
      bindingCreditCardLayout?.paymentMethodsHeader?.infoFeesGroup?.visibility = GONE
    }
    fiatPriceSkeleton.visibility = GONE
    appcPriceSkeleton.visibility = GONE
    bindingCreditCardLayout?.paymentMethodsHeader?.fiatPrice?.visibility = VISIBLE
  }

  private fun showFreeTrialInfo(
    period: Period?,
    freeTrialPeriod: Period?,
    startingDate: String,
    price: String,
  ) {
    if (period == null || freeTrialPeriod == null) return

    bindingCreditCardLayout?.bonusLayout?.root?.visibility = GONE
    bonusLayoutPreSelected?.visibility = GONE
    bindingCreditCardLayout?.paymentMethodsHeader?.freeTrialLayout?.visibility = VISIBLE
    bindingCreditCardLayout?.paymentMethodsHeader?.subsTrialText?.text =
      freeTrialPeriod.mapToFreeTrialDuration(requireContext())
    bindingCreditCardLayout?.paymentMethodsHeader?.subsTrialStaringDateDescription?.text =
      getString(R.string.subscriptions_starting_on_body, Period.formatDayMonth(startingDate))
    bindingCreditCardLayout?.paymentMethodsHeader?.subsTrialStaringDateText?.text =
      period.mapToSubsFrequency(requireContext(), price)
  }

  fun buyButtonClicked() = RxView.clicks(buyButton).map {
    AdyenPaymentViewModel.BuyClickData(
      shouldStoreCard = shouldStoreCard(),
    )
  }

  fun close(bundle: Bundle) = iabView.close(bundle)

  // TODO: Refactor this to pass the whole Intent.
// TODO: Currently this relies on the fact that Adyen 4.4.0 internally uses only Intent.getData().
  fun submitUriResult(uri: Uri) = redirectComponent.handleIntent(Intent("", uri))

  fun getPaymentDetails(): Observable<AdyenComponentResponseModel> = paymentDetailsSubject!!

  fun getVerificationClicks(): Observable<Boolean> =
    Observable.merge(
      RxView.clicks(errorVerifyWalletButton).map { false },
      RxView.clicks(errorVerifyCardButton).map { true })

  fun lockRotation() = iabView.lockRotation()

  fun hideKeyboard() {
    view?.let { KeyboardUtils.hideKeyboard(view) }
  }

  fun handleCreditCardNeedCVC(needCVC: Boolean) {
    viewModel.askCVC = needCVC
  }

  fun shouldStoreCard(): Boolean {
    return adyenCardView.cardSave
  }

  private fun setupCardConfiguration(hideCvcStoredCard: Boolean) {
    cardConfiguration = CardConfiguration.Builder(activity as Context, BuildConfig.ADYEN_PUBLIC_KEY)
      .setHideCvcStoredCard(hideCvcStoredCard).setEnvironment(adyenEnvironment).build()
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
      bindingCreditCardLayout?.fragmentIabTransactionCompleted?.transactionSuccessBonusText?.text =
        getString(R.string.purchase_success_bonus_received_title, bonus)
    } else {
      bindingCreditCardLayout?.fragmentIabTransactionCompleted?.bonusSuccessLayout?.visibility =
        GONE
    }
  }

  private fun showBonus() {
    if (bonus.isNotEmpty() && bonus != " " && !isFreeTrial) {
      bonusLayout?.visibility = VISIBLE
      bonusLayoutPreSelected?.visibility = VISIBLE
      bonusValue.text = if (isPortraitMode(requireContext())) context?.getString(
        R.string.gamification_purchase_header_part_2,
        bonus
      ) else bonus
    } else {
      bonusLayout?.visibility = GONE
      bonusLayoutPreSelected?.visibility = GONE
    }
  }

  private fun handleLayoutVisibility(isStored: Boolean) {
    adyenCardView.showInputFields(!isStored)
    if (viewModel.askCVC && isStored) {
      morePaymentMethods?.visibility = VISIBLE
    } else if (isStored) {
      scrollPayment?.visibility = GONE
      layoutAdyenStoredCard?.visibility = VISIBLE
      morePaymentStoredMethods?.visibility = VISIBLE
    } else {
      scrollPayment?.visibility = VISIBLE
      layoutAdyenStoredCard?.visibility = GONE
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
      bindingCreditCardLayout?.fragmentIabTransactionCompleted?.lottieTransactionSuccess?.setAnimation(
        R.raw.success_animation
      )
      setupTransactionComplete()
    } else {
      bindingCreditCardLayout?.fragmentIabTransactionCompleted?.lottieTransactionSuccess?.setAnimation(
        R.raw.success_animation
      )
    }
  }

  private fun handlePreSelectedView() {
    if (!isPreSelected) {
      cancelButton.setText(getString(R.string.back_button))
      iabView.setBackEnable(false)
    }
  }

  private fun setBonusMessage(nextPaymentDate: Date) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(nextPaymentDate)
    val nextPaymentText =
      "${getString(R.string.subscriptions_details_next_payment_title)} $formattedDate"
    bindingCreditCardLayout?.fragmentIabTransactionCompleted?.nextPaymentDate?.text =
      nextPaymentText
  }

  @Composable
  private fun ShowCardListExpandedLayout() {
    var isGotItVisible by remember { mutableStateOf(cardPaymentDataSource.isGotItVisible()) }
    CardListExpandedScreen(
      onAddNewCardClick = {
        isExpandedCardsList = false
        viewModel.paymentStateEnum = PaymentStateEnum.PAYMENT_WITH_NEW_CARD
        restartFragment()
      }, onChangeCardClick = { storedCard, _ ->
        setSelectedCard(storedCard)
        isExpandedCardsList = false
        restartFragment()

      }, onGotItClick = {
        cardPaymentDataSource.setGotItManageCard(false)
        isGotItVisible = false
      }, cardList = viewModel.cardsList, isGotItVisible = isGotItVisible
    )
  }

  private fun setSelectedCard(storedCard: StoredCard?) {
    if (storedCard != null && viewModel.cardsList.contains(storedCard)) {
      viewModel.cardsList.find { it.isSelectedCard }?.isSelectedCard = false
      viewModel.cardsList.find { it == storedCard }?.isSelectedCard = true
      storedCard.recurringReference?.let {
        viewModel.setCardIdSharedPreferences(it)
      }
    }

  }

  private fun handleBuyButtonText() {
    when {
      transactionBuilder.type.equals(
        TransactionData.TransactionType.DONATION.name, ignoreCase = true
      ) -> {
        buyButton.setText(getString(R.string.action_donate))
      }

      transactionBuilder.type.equals(
        TransactionData.TransactionType.INAPP_SUBSCRIPTION.name, ignoreCase = true
      ) -> buyButton.setText(getString(R.string.subscriptions_subscribe_button))

      else -> {
        buyButton.setText(getString(R.string.action_buy))
      }
    }
  }

  override fun onDestroyView() {
    iabView.setBackEnable(true)
    viewModel.cancelPaypalLaunch = true
//    presenter.stop()
    super.onDestroyView()
  }

  override fun onDestroy() {
    paymentDataSubject = null
    paymentDetailsSubject = null
    adyen3DSErrorSubject = null
    super.onDestroy()
  }

  fun restartFragment(paymentType: PaymentType = PaymentType.CARD) {
    this.fragmentManager?.beginTransaction()?.replace(
      R.id.fragment_container, newInstance(
        paymentType = paymentType,
        origin = origin,
        transactionBuilder = transactionBuilder,
        amount = amount,
        currency = currency,
        bonus = bonus,
        isPreSelected = isPreSelected,
        gamificationLevel = gamificationLevel,
        skuDescription = skuDescription,
        isSubscription = isSubscription,
        isSkills = skills,
        frequency = frequency,
        paymentStateEnum = viewModel.paymentStateEnum.state,
        isFreeTrial = isFreeTrial,
        freeTrialDuration = freeTrialDuration,
        subscriptionStartingDate = subscriptionStartingDate
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
    private const val PAYMENT_STATE_ENUM = "payment_state_enum"
    private const val IS_FREE_TRIAL = "is_free_trial"
    private const val FREE_TRIAL_DURATION = "free_trial_duration"
    private const val SUBSCRIPTION_STARTING_DATE = "subscription_starting_date"

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
      frequency: String?,
      paymentStateEnum: String?,
      isFreeTrial: Boolean,
      freeTrialDuration: String?,
      subscriptionStartingDate: String?
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
        putString(PAYMENT_STATE_ENUM, paymentStateEnum)
        putBoolean(IS_FREE_TRIAL, isFreeTrial)
        putString(FREE_TRIAL_DURATION, freeTrialDuration)
        putString(SUBSCRIPTION_STARTING_DATE, subscriptionStartingDate)
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

  private val transactionBuilder by lazy {
    getParcelableExtra<TransactionBuilder>(TRANSACTION_DATA_KEY)
      ?: throw IllegalArgumentException("transaction data not found")
  }

  private val amount by lazy {
    getSerializableExtra<BigDecimal>(AMOUNT_KEY)
      ?: throw IllegalArgumentException("amount data not found")
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

  private val paymentStateEnum: String? by lazy {
    if (requireArguments().containsKey(PAYMENT_STATE_ENUM)) {
      requireArguments().getString(PAYMENT_STATE_ENUM)
    } else {
      null
    }
  }

  private val isFreeTrial: Boolean by lazy {
    if (requireArguments().containsKey(IS_FREE_TRIAL)) {
      requireArguments().getBoolean(IS_FREE_TRIAL)
    } else {
      false
    }
  }

  private val freeTrialDuration: String? by lazy {
    if (requireArguments().containsKey(FREE_TRIAL_DURATION)) {
      requireArguments().getString(FREE_TRIAL_DURATION)
    } else {
      null
    }
  }

  private val subscriptionStartingDate: String? by lazy {
    if (requireArguments().containsKey(SUBSCRIPTION_STARTING_DATE)) {
      requireArguments().getString(SUBSCRIPTION_STARTING_DATE)
    } else {
      null
    }
  }

}
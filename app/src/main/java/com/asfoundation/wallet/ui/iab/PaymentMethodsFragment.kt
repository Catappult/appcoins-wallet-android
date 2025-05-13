package com.asfoundation.wallet.ui.iab

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appcoins.wallet.core.analytics.analytics.legacy.ChallengeRewardAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.android_common.extensions.getParcelableExtra
import com.appcoins.wallet.core.utils.jvm_common.C.Key.TRANSACTION
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetCachedShowRefundDisclaimerUseCase
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.asf.wallet.R
import com.asf.wallet.databinding.PaymentMethodsLayoutBinding
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.amazonPay.usecases.DeleteAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.GetAmazonPayChargePermissionUseCase
import com.asfoundation.wallet.billing.amazonPay.usecases.SaveAmazonPayChargePermissionLocalStorageUseCase
import com.asfoundation.wallet.billing.paypal.usecases.IsPaypalAgreementCreatedUseCase
import com.asfoundation.wallet.billing.paypal.usecases.RemovePaypalBillingAgreementUseCase
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.util.Period
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxrelay2.PublishRelay
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal
import javax.inject.Inject


@AndroidEntryPoint
class PaymentMethodsFragment : BasePageViewFragment(), PaymentMethodsView {

  companion object {
    private const val IS_BDS = "isBds"
    private const val APP_PACKAGE = "app_package"
    private const val ITEM_ALREADY_OWNED = "item_already_owned"
    private const val IS_DONATION = "is_donation"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val FREQUENCY = "frequency"

    @JvmStatic
    fun newInstance(
      transaction: TransactionBuilder?,
      productName: String?,
      isBds: Boolean,
      isDonation: Boolean,
      developerPayload: String?,
      uri: String?,
      transactionData: String?,
      isSubscription: Boolean,
      frequency: String?
    ): Fragment {
      val bundle = Bundle()
      bundle.apply {
        putParcelable(TRANSACTION, transaction)
        putSerializable(IabActivity.TRANSACTION_AMOUNT, transaction!!.amount())
        putString(APP_PACKAGE, transaction.domain)
        putString(IabActivity.PRODUCT_NAME, productName)
        putString(IabActivity.DEVELOPER_PAYLOAD, developerPayload)
        putString(IabActivity.URI, uri)
        putBoolean(IS_BDS, isBds)
        putBoolean(IS_DONATION, isDonation)
        putString(IabActivity.TRANSACTION_DATA, transactionData)
        putString(FREQUENCY, frequency)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
      }
      return PaymentMethodsFragment().apply { arguments = bundle }
    }
  }

  @Inject
  lateinit var paymentMethodsAnalytics: PaymentMethodsAnalytics

  @Inject
  lateinit var paymentMethodsMapper: PaymentMethodsMapper

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var paymentMethodsInteractor: PaymentMethodsInteractor

  @Inject
  lateinit var getWalletInfoUseCase: GetWalletInfoUseCase

  @Inject
  lateinit var removePaypalBillingAgreementUseCase: RemovePaypalBillingAgreementUseCase

  @Inject
  lateinit var getCachedShowRefundDisclaimerUseCase: GetCachedShowRefundDisclaimerUseCase

  @Inject
  lateinit var isPaypalAgreementCreatedUseCase: IsPaypalAgreementCreatedUseCase

  @Inject
  lateinit var getAmazonPayChargePermissionLocalStorageUseCase: GetAmazonPayChargePermissionLocalStorageUseCase

  @Inject
  lateinit var deleteAmazonPayChargePermissionUseCase: DeleteAmazonPayChargePermissionUseCase

  @Inject
  lateinit var saveAmazonPayChargePermissionLocalStorageUseCase: SaveAmazonPayChargePermissionLocalStorageUseCase

  @Inject
  lateinit var getAmazonPayChargePermissionUseCase: GetAmazonPayChargePermissionUseCase

  @Inject
  lateinit var logger: Logger

  @Inject
  lateinit var challengeRewardAnalytics: ChallengeRewardAnalytics

  private lateinit var presenter: PaymentMethodsPresenter
  private lateinit var iabView: IabView
  private lateinit var compositeDisposable: CompositeDisposable
  private lateinit var paymentMethodClick: PublishRelay<Int>
  private lateinit var topupClick: PublishSubject<String>
  private lateinit var paymentMethodsAdapter: PaymentMethodsAdapter
  private val paymentMethodList: MutableList<PaymentMethod> = ArrayList()
  private var setupSubject: PublishSubject<Boolean>? = null
  private var preSelectedPaymentMethod: BehaviorSubject<PaymentMethod>? = null
  private var isPreSelected = false
  private var itemAlreadyOwnedError = false
  private var bonusMessageValue = ""
  private var bonusValue: BigDecimal? = null

  private val binding by viewBinding(PaymentMethodsLayoutBinding::bind)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Payment Methods Fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    compositeDisposable = CompositeDisposable()
    setupSubject = PublishSubject.create()
    topupClick = PublishSubject.create()
    preSelectedPaymentMethod = BehaviorSubject.create()
    paymentMethodClick = PublishRelay.create()
    itemAlreadyOwnedError = arguments?.getBoolean(ITEM_ALREADY_OWNED, false) ?: false
    val paymentMethodsData =
      PaymentMethodsData(
        appPackage = appPackage,
        isBds = isBds,
        developerPayload = getDeveloperPayload(),
        uri = getUri(),
        sku = transactionBuilder!!.skuId,
        frequency = getFrequency(),
        subscription = getIsSubscription(),
        externalBuyerReference = transactionBuilder?.externalBuyerReference,
        isFreeTrial = transactionBuilder?.isFreeTrial ?: false,
        freeTrialDuration = transactionBuilder?.freeTrialDuration,
        subscriptionStartingDate = transactionBuilder?.subscriptionStartingDate
      )
    presenter =
      PaymentMethodsPresenter(
        view = this,
        activity = iabView,
        viewScheduler = AndroidSchedulers.mainThread(),
        networkThread = Schedulers.io(),
        disposables = CompositeDisposable(),
        analytics = paymentMethodsAnalytics,
        transaction = transactionBuilder!!,
        paymentMethodsMapper = paymentMethodsMapper,
        formatter = formatter,
        getWalletInfoUseCase = getWalletInfoUseCase,
        removePaypalBillingAgreementUseCase = removePaypalBillingAgreementUseCase,
        isPaypalAgreementCreatedUseCase = isPaypalAgreementCreatedUseCase,
        logger = logger,
        interactor = paymentMethodsInteractor,
        paymentMethodsData = paymentMethodsData,
        getAmazonPayChargePermissionLocalStorageUseCase = getAmazonPayChargePermissionLocalStorageUseCase,
        saveAmazonPayChargePermissionLocalStorageUseCase = saveAmazonPayChargePermissionLocalStorageUseCase,
        deleteAmazonPayChargePermissionUseCase = deleteAmazonPayChargePermissionUseCase,
        getAmazonPayChargePermissionUseCase = getAmazonPayChargePermissionUseCase
      )
  }

  override fun getTopupClicks(): Observable<String> {
    return topupClick
  }

  override fun showTopupFlow() {
    iabView.showTopupFlow()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupAppNameAndIcon()

    setBuyButtonText()
    presenter.present(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = PaymentMethodsLayoutBinding.inflate(inflater).root

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    presenter.onSavedInstance(outState)
  }

  override fun onDestroyView() {
    presenter.stop()
    compositeDisposable.clear()
    super.onDestroyView()
  }

  override fun showPaymentMethods(
    paymentMethods: MutableList<PaymentMethod>,
    currency: String,
    paymentMethodId: String,
    fiatAmount: String,
    appcEnabled: Boolean,
    creditsEnabled: Boolean,
    frequency: String?,
    isSubscription: Boolean,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ) {
    updateHeaderInfo(
      currency = currency,
      fiatAmount = fiatAmount,
      frequency = frequency,
      isSubscription = isSubscription,
      isFreeTrial = isFreeTrial,
      freeTrialDuration = freeTrialDuration,
      subscriptionStartingDate = subscriptionStartingDate
    )
    setupPaymentMethods(paymentMethods, paymentMethodId)
    if (paymentMethods.size == 1 && paymentMethods[0].id == PaymentMethodId.APPC_CREDITS.id) {
      hideBonus()
    }
    setupSubject!!.onNext(true)
  }

  override fun onResume() {
    val firstRun = paymentMethodList.isEmpty() && !isPreSelected
    presenter.onResume(firstRun)
    super.onResume()
  }

  override fun showFee(hasFee: Boolean) {
    binding.paymentMethodsHeader.infoFeesGroup.visibility = if (hasFee) View.VISIBLE else View.GONE
  }

  override fun updatePriceAndCurrency(
    currency: String,
    amount: BigDecimal,
    frequency: String?,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?,
  ) {
    var fiatPrice = "$amount $currency"
    val period = Period.parse(frequency ?: "")
    period?.mapToSubsFrequency(requireContext(), fiatPrice)?.let { fiatPrice = it }
    val freeTrialPeriod = try {
      Period.parse(freeTrialDuration ?: "")
    } catch (e: Exception) {
      null
    }

    val price = when {
      isPortraitMode(requireContext()) -> if (getIsSubscription()) {
        if (isFreeTrial) {
          showFreeTrialInfo(
            period = period ?: Period(0,0,0,1),
            freeTrialPeriod = freeTrialPeriod ?: Period(0,0,0,1),
            startingDate = subscriptionStartingDate ?: "",
            price = "$amount $currency"
          )
          "0.00 $currency"
        } else {
          binding.paymentMethodsHeader.freeTrialLayout.visibility = View.GONE
          fiatPrice
        }
      } else {
        getString(R.string.purchase_total_header, amount, currency)
      }

      else -> if (getIsSubscription()) {
        if (isFreeTrial) {
          showFreeTrialInfo(
            period = period ?: Period(0,0,0,1),
            freeTrialPeriod = freeTrialPeriod ?: Period(0,0,0,1),
            startingDate = subscriptionStartingDate ?: "",
            price = "$amount $currency"
          )
          "0.00 $currency"
        } else {
          binding.paymentMethodsHeader.freeTrialLayout.visibility = View.GONE
          fiatPrice
        }
      } else {
        getString(R.string.gas_price_value, amount, currency)
      }
    }

    with(binding.paymentMethodsHeader.fiatPrice) {
      if (text != price) showPriceTransition()
      text = price
    }
  }

  private fun showPriceTransition() {
    with(binding.paymentMethodsHeader) {
      priceTransitionAnimation.playAnimation()
      fiatPrice.visibility = View.INVISIBLE
      priceTransitionAnimation.visibility = View.VISIBLE
      priceTransitionAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          super.onAnimationEnd(animation)
          priceTransitionAnimation.visibility = View.GONE
          fiatPrice.visibility = View.VISIBLE
          if (!isPortraitMode(requireContext())) {
            binding.paymentMethodsHeader.fiatTotalPriceLabel?.visibility = View.VISIBLE
          }
        }
      })
    }
  }

  private fun setupPaymentMethods(
    paymentMethods: MutableList<PaymentMethod>,
    paymentMethodId: String
  ) {
    isPreSelected = false
    binding.preSelectedPaymentMethodGroup.visibility = View.GONE
    binding.midSeparator?.visibility = View.VISIBLE
    if (paymentMethods.isNotEmpty()) {
      paymentMethodsAdapter = when {
        presenter.showPayPalLogout -> {
          PaymentMethodsAdapter(
            paymentMethods = paymentMethods,
            paymentMethodId = paymentMethodId,
            paymentMethodClick = paymentMethodClick,
            logoutCallback = { handleLogoutPaypalCallbackAdapter(false) },
            showLogoutAction = presenter.showPayPalLogout
          )
        }

        presenter.showAmazonPayLogout -> {
          PaymentMethodsAdapter(
            paymentMethods = paymentMethods,
            paymentMethodId = paymentMethodId,
            paymentMethodClick = paymentMethodClick,
            logoutCallback = { handleLogoutAmazonCallbackAdapter(false) },
            showLogoutAction = presenter.showAmazonPayLogout
          )
        }

        else -> {
          PaymentMethodsAdapter(
            paymentMethods = paymentMethods,
            paymentMethodId = paymentMethodId,
            paymentMethodClick = paymentMethodClick,
            logoutCallback = { },
            showLogoutAction = false
          )
        }
      }

      binding.paymentMethodsRadioList.adapter = paymentMethodsAdapter
      paymentMethodList.clear()
      paymentMethodList.addAll(paymentMethods)
      paymentMethodClick.accept(paymentMethodsAdapter.getSelectedItem())
    }
  }

  private fun handleLogoutPaypalCallbackAdapter(isPreselected: Boolean) {
    presenter.removePaypalBillingAgreement()
    presenter.showPayPalLogout = false
    showProgressBarLoading()
    if (!isPreselected) updateAdapter()
  }

  private fun handleLogoutAmazonCallbackAdapter(isPreselected: Boolean) {
    presenter.removeAmazonPayChargePermission()
    presenter.showAmazonPayLogout = false
    showProgressBarLoading()
    if (!isPreselected) updateAdapter()
  }

  @SuppressLint("NotifyDataSetChanged")
  private fun updateAdapter() {
    paymentMethodsAdapter.showLogoutAction =
      presenter.showPayPalLogout || presenter.showAmazonPayLogout
    paymentMethodsAdapter.notifyDataSetChanged()
  }

  private fun updateHeaderInfo(
    currency: String,
    fiatAmount: String,
    frequency: String?,
    isSubscription: Boolean,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?,
  ) {
    var fiatPrice = "$fiatAmount $currency"
    if (isSubscription) {
      val period = Period.parse(frequency!!)
      period?.mapToSubsFrequency(requireContext(), fiatPrice)?.let { fiatPrice = it }
      val freeTrialPeriod = try {
        Period.parse(freeTrialDuration ?: "")
      } catch (e: Exception) {
        null
      }

      binding.paymentMethodsHeader.fiatPrice.text = if (isFreeTrial) {
        showFreeTrialInfo(
          period = period ?: Period(0,0,0,1),
          freeTrialPeriod = freeTrialPeriod ?: Period(0,0,0,1),
          startingDate = subscriptionStartingDate ?: "",
          price = "$fiatAmount $currency"
        )
        "0.00 $currency"
      } else {
        binding.paymentMethodsHeader.freeTrialLayout.visibility = View.GONE
        fiatPrice
      }
    } else {
      binding.paymentMethodsHeader.fiatPrice.text =
        getString(R.string.purchase_total_header, fiatAmount, currency)
    }
    binding.paymentMethodsHeader.fiatPriceSkeleton.root.visibility = View.GONE
    binding.paymentMethodsHeader.appcPriceSkeleton.root.visibility = View.GONE
    binding.paymentMethodsHeader.fiatPrice.visibility = View.VISIBLE
  }

  private fun getPaymentMethodLabel(paymentMethod: PaymentMethod): String {
    return TranslatablePaymentMethods.values()
      .firstOrNull { it.paymentMethod == paymentMethod.id }
      ?.let { getString(it.stringId) } ?: paymentMethod.label
  }

  override fun showPreSelectedPaymentMethod(
    paymentMethod: PaymentMethod,
    currency: String,
    fiatAmount: String,
    isBonusActive: Boolean,
    frequency: String?,
    isSubscription: Boolean,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ) {
    preSelectedPaymentMethod!!.onNext(paymentMethod)
    updateHeaderInfo(
      currency = currency,
      fiatAmount = fiatAmount,
      frequency = frequency,
      isSubscription = isSubscription,
      isFreeTrial = isFreeTrial,
      freeTrialDuration = freeTrialDuration,
      subscriptionStartingDate = subscriptionStartingDate
    )

    setupPaymentMethod(paymentMethod, isBonusActive, isSubscription)
    setupSubject!!.onNext(true)
  }

  private fun setupPaymentMethod(
    paymentMethod: PaymentMethod,
    isBonusActive: Boolean,
    isSubscription: Boolean
  ) {

    if (paymentMethod.showTopup) {
      binding.dialogBuyButtonsPaymentMethods.buyButton.tag = false
    } else {
      binding.dialogBuyButtonsPaymentMethods.buyButton.tag = null
    }

    isPreSelected = true
    binding.midSeparator?.visibility = View.INVISIBLE
    binding.layoutPreSelected.paymentMethodDescription.visibility = View.VISIBLE
    binding.layoutPreSelected.paymentMethodDescription.text = getPaymentMethodLabel(paymentMethod)
    when (paymentMethod.id) {
      PaymentMethodId.APPC_CREDITS.id -> {
        binding.layoutPreSelected.paymentMethodSecondary.visibility = View.VISIBLE
        binding.layoutPreSelected.paymentMoreLogout.visibility = View.GONE
        if (isBonusActive) hideBonus()
      }

      PaymentMethodId.PAYPAL_V2.id -> {
        binding.layoutPreSelected.paymentMoreLogout.visibility = View.VISIBLE
        binding.layoutPreSelected.paymentMethodSecondary.visibility = View.GONE
        binding.layoutPreSelected.paymentMoreLogout.setOnClickListener {
          val popup = PopupMenu(context?.applicationContext, it)
          popup.menuInflater.inflate(R.menu.logout_menu, popup.menu)
          popup.setOnMenuItemClickListener {
            binding.layoutPreSelected.paymentMoreLogout.visibility = View.GONE
            handleLogoutPaypalCallbackAdapter(true)
            return@setOnMenuItemClickListener true
          }
          popup.show()
        }
      }

      PaymentMethodId.AMAZONPAY.id -> {
        binding.layoutPreSelected.paymentMoreLogout.visibility = View.VISIBLE
        binding.layoutPreSelected.paymentMethodSecondary.visibility = View.GONE
        binding.layoutPreSelected.paymentMoreLogout.setOnClickListener {
          val popup = PopupMenu(context?.applicationContext, it)
          popup.menuInflater.inflate(R.menu.logout_menu, popup.menu)
          popup.setOnMenuItemClickListener {
            binding.layoutPreSelected.paymentMoreLogout.visibility = View.GONE
            handleLogoutAmazonCallbackAdapter(true)
            return@setOnMenuItemClickListener true
          }
          popup.show()
        }
      }

      else -> {
        binding.layoutPreSelected.paymentMethodSecondary.visibility = View.GONE
        binding.layoutPreSelected.paymentMoreLogout.visibility = View.GONE
        if (isBonusActive) {
          if (isSubscription) showBonus(R.string.subscriptions_bonus_body)
          else showBonus(R.string.gamification_purchase_body)
        }
      }
    }
    setupFee(paymentMethod.fee)
    loadIcons(paymentMethod, binding.layoutPreSelected.paymentMethodIc)
  }

  @SuppressLint("SetTextI18n")
  private fun setupFee(fee: PaymentMethodFee?) {
    if (fee?.isValidFee() == true) {
      val formattedValue = formatter.formatPaymentCurrency(fee.amount!!, WalletCurrency.FIAT)
      binding.layoutPreSelected.paymentMethodFee.text =
        context?.getString(
          R.string.purchase_fees_and_taxes_known_disclaimer_body,
          formattedValue,
          fee.currency
        )
    } else {
      binding.layoutPreSelected.paymentMethodFee.visibility = View.GONE
    }
  }

  private fun loadIcons(paymentMethod: PaymentMethod, view: ImageView?) {
    compositeDisposable.add(
      Observable.fromCallable {
        val context = context
        GlideApp.with(context!!).asBitmap().load(paymentMethod.iconUrl).submit().get()
      }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { view?.setImageBitmap(it) }
        .subscribe({}) { it.printStackTrace() })
  }

  override fun showError(message: Int) {
    binding.paymentMethodMainView.visibility = View.GONE
    binding.errorMessage.errorDismiss.setText(getString(R.string.ok))
    binding.errorMessage.root.visibility = View.VISIBLE
    binding.errorMessage.genericErrorLayout.errorMessage.setText(message)
  }

  override fun showNoNetworkError() {
    binding.paymentMethodMainView.visibility = View.GONE
    binding.errorMessage.root.visibility = View.VISIBLE
    binding.errorMessage.genericErrorLayout.root.visibility = View.GONE
    binding.errorMessage.noNetworkErrorLayout.root.visibility = View.VISIBLE
    binding.errorMessage.errorDismiss.visibility = View.GONE
    binding.errorMessage.retryButton.visibility = View.VISIBLE
  }

  override fun showItemAlreadyOwnedError() {
    binding.paymentMethodMainView.visibility = View.GONE
    iabView.setBackEnable(false)
    binding.errorMessage.errorDismiss.setText(getString(R.string.ok))
    binding.errorMessage.root.visibility = View.VISIBLE
    binding.errorMessage.genericErrorLayout.errorMessage.setText(
      R.string.purchase_error_incomplete_transaction_body
    )
    binding.errorMessage.genericErrorLayout.layoutSupportIcn.visibility = View.GONE
    binding.errorMessage.genericErrorLayout.layoutSupportLogo.visibility = View.GONE
    binding.errorMessage.genericErrorLayout.contactUs.visibility = View.GONE
  }

  override fun finish(bundle: Bundle) = iabView.finish(bundle)

  override fun showPaymentsSkeletonLoading() {
    binding.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = false
    binding.preSelectedPaymentMethodGroup.visibility = View.GONE
    binding.paymentMethodsListGroup.visibility = View.INVISIBLE
    binding.midSeparator?.visibility = View.VISIBLE
    binding.paymentsSkeleton.visibility = View.VISIBLE
  }

  override fun showSkeletonLoading() {
    showPaymentsSkeletonLoading()
  }

  override fun showProgressBarLoading() {
    binding.paymentMethods.visibility = View.INVISIBLE
    binding.loadingAnimation.visibility = View.VISIBLE
    binding.cvLegalDisclaimer?.visibility = View.GONE
    binding.tvLegalDisclaimer?.visibility = View.GONE
  }

  override fun hideLoading() {
    if (binding.processingLoading.visibility != View.VISIBLE) {
      binding.paymentMethods.visibility = View.VISIBLE
      removeSkeletons()
      if (binding.dialogBuyButtonsPaymentMethods.buyButton.tag != null &&
        binding.dialogBuyButtonsPaymentMethods.buyButton.tag is Boolean
      ) {
        binding.dialogBuyButtonsPaymentMethods.buyButton.isEnabled =
          binding.dialogBuyButtonsPaymentMethods.buyButton.tag as Boolean
      } else {
        binding.dialogBuyButtonsPaymentMethods.buyButton.isEnabled = true
      }
      if (isPreSelected) {
        binding.preSelectedPaymentMethodGroup.visibility = View.VISIBLE
        binding.paymentMethodsListGroup.visibility = View.GONE
        binding.layoutPreSelected.root.visibility = View.VISIBLE
      } else {
        binding.paymentMethodsListGroup.visibility = View.VISIBLE
        binding.preSelectedPaymentMethodGroup.visibility = View.GONE
      }
      binding.cvLegalDisclaimer?.visibility =
        if (getCachedShowRefundDisclaimerUseCase()) View.VISIBLE else View.GONE
      binding.tvLegalDisclaimer?.visibility =
        if (getCachedShowRefundDisclaimerUseCase()) View.VISIBLE else View.GONE
      binding.loadingAnimation.visibility = View.GONE
    }
  }

  private fun showFreeTrialInfo(
    period: Period?,
    freeTrialPeriod: Period?,
    startingDate: String,
    price: String,
  ) {
    if (period == null || freeTrialPeriod == null) return

    binding.bonusLayout.root.visibility = View.GONE
    binding.paymentMethodsHeader.freeTrialLayout.visibility = View.VISIBLE
    binding.paymentMethodsHeader.subsTrialText.text =
      freeTrialPeriod.mapToFreeTrialDuration(requireContext())
    binding.paymentMethodsHeader.subsTrialStaringDateDescription.text =
      getString(R.string.subscriptions_starting_on_body, Period.formatDayMonth(startingDate))
    binding.paymentMethodsHeader.subsTrialStaringDateText.text =
      period.mapToSubsFrequency(requireContext(), price)
  }

  override fun getCancelClick(): Observable<Any> =
    RxView.clicks(binding.dialogBuyButtonsPaymentMethods.cancelButton)

  override fun getSelectedPaymentMethod(hasPreSelectedPaymentMethod: Boolean): PaymentMethod {
    if (!isPreSelected && ::paymentMethodsAdapter.isInitialized.not()) return PaymentMethod()
    val checkedButtonId =
      if (::paymentMethodsAdapter.isInitialized) paymentMethodsAdapter.getSelectedItem() else -1
    return if (paymentMethodList.isNotEmpty() && !isPreSelected && checkedButtonId != -1) {
      paymentMethodList[checkedButtonId]
    } else if (hasPreSelectedPaymentMethod && checkedButtonId == -1) {
      preSelectedPaymentMethod?.value ?: PaymentMethod()
    } else {
      PaymentMethod()
    }
  }

  override fun updateProductName() {
    binding.paymentMethodsHeader.appSkuDescription.text = transactionBuilder?.productName
  }

  override fun close(bundle: Bundle) = iabView.close(bundle)

  override fun errorDismisses(): Observable<Any> =
    RxView.clicks(binding.errorMessage.errorDismiss).map { itemAlreadyOwnedError }

  override fun errorTryAgain(): Observable<Any> =
    RxView.clicks(binding.errorMessage.retryButton).map { itemAlreadyOwnedError }

  override fun getSupportLogoClicks() =
    RxView.clicks(binding.errorMessage.genericErrorLayout.layoutSupportLogo)

  override fun getSupportIconClicks() =
    RxView.clicks(binding.errorMessage.genericErrorLayout.layoutSupportIcn)

  override fun showAuthenticationActivity() = iabView.showAuthenticationActivity()

  override fun setupUiCompleted() = setupSubject!!

  override fun showProcessingLoadingDialog() {
    binding.paymentMethods.visibility = View.INVISIBLE
    binding.processingLoading.visibility = View.VISIBLE
  }

  override fun getBuyClick(): Observable<Any> =
    RxView.clicks(binding.dialogBuyButtonsPaymentMethods.buyButton)

  override fun showCarrierBilling(fiatValue: FiatValue, isPreselected: Boolean) =
    iabView.showCarrierBilling(fiatValue.currency, fiatValue.amount, bonusValue, isPreselected)

  override fun showPaypal(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showAdyenPayment(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.PAYPAL,
      bonusMessageValue,
      false,
      null,
      gamificationLevel,
      isSubscription,
      frequency,
      isFreeTrial = false,
      freeTrialDuration = null,
      subscriptionStartingDate = null
    )
  }

  override fun showPaypalV2(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showPayPalV2(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.PAYPAL,
      bonusMessageValue,
      false,
      null,
      gamificationLevel,
      isSubscription,
      frequency
    )
  }

  override fun showSandbox(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showSandbox(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.SANDBOX,
      bonusMessageValue,
      false,
      null,
      gamificationLevel,
      isSubscription,
      frequency
    )
  }

  override fun showAdyen(
    fiatAmount: BigDecimal,
    fiatCurrency: String,
    paymentType: PaymentType,
    iconUrl: String?,
    gamificationLevel: Int,
    frequency: String?,
    isSubscription: Boolean,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ) {
    if (!itemAlreadyOwnedError) {
      iabView.showAdyenPayment(
        amount = fiatAmount,
        currency = fiatCurrency,
        isBds = isBds,
        paymentType = paymentType,
        bonus = bonusMessageValue,
        isPreselected = true,
        iconUrl = iconUrl,
        gamificationLevel = gamificationLevel,
        isSubscription = isSubscription,
        frequency = frequency,
        isFreeTrial = isFreeTrial,
        freeTrialDuration = freeTrialDuration,
        subscriptionStartingDate = subscriptionStartingDate
      )
    }
  }

  override fun showVkPay(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showVkPay(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.PAYPAL,
      bonusMessageValue,
      null,
      gamificationLevel,
      isSubscription,
      frequency
    )
  }

  override fun showAmazonPay(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showAmazonPay(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.PAYPAL,
      bonusMessageValue,
      null,
      gamificationLevel,
      isSubscription,
      frequency
    )
  }

  override fun showGooglePayWeb(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean
  ) {
    iabView.showGooglePayWeb(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.PAYPAL,
      bonusMessageValue,
      false,
      null,
      gamificationLevel,
      isSubscription,
      frequency
    )
  }

  override fun showMiPayWeb(
    fiatValue: FiatValue,
  ) {
    iabView.showMiPayWeb(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      bonusMessageValue
    )
  }

  override fun showCreditCard(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    frequency: String?,
    isSubscription: Boolean,
    isFreeTrial: Boolean,
    freeTrialDuration: String?,
    subscriptionStartingDate: String?
  ) =
    iabView.showAdyenPayment(
      fiatValue.amount,
      fiatValue.currency,
      isBds,
      PaymentType.CARD,
      bonusMessageValue,
      false,
      null,
      gamificationLevel,
      isSubscription,
      frequency,
      isFreeTrial,
      freeTrialDuration,
      subscriptionStartingDate
    )

  override fun showAppCoins(gamificationLevel: Int, transaction: TransactionBuilder) =
    iabView.showOnChain(
      transaction.amount(), isBds, bonusMessageValue, gamificationLevel, transaction
    )

  override fun showCredits(gamificationLevel: Int, transaction: TransactionBuilder) =
    iabView.showAppcoinsCreditsPayment(
      transaction.amount(), isPreSelected, gamificationLevel, transaction
    )

  override fun showSubscribe() {
    binding.dialogBuyButtonsPaymentMethods.buyButton.setText(
      getString(R.string.subscriptions_subscribe_button)
    )
  }

  override fun showShareLink(selectedPaymentMethod: String) {
    val isOneStep: Boolean = transactionBuilder!!.type.equals("INAPP_UNMANAGED", ignoreCase = true)
    iabView.showShareLinkPayment(
      transactionBuilder!!.domain,
      transactionBuilder!!.skuId,
      if (isOneStep) transactionBuilder!!.originalOneStepValue else null,
      if (isOneStep) transactionBuilder!!.originalOneStepCurrency else null,
      transactionBuilder!!.amount(),
      transactionBuilder!!.type,
      selectedPaymentMethod
    )
  }

  override fun getPaymentSelection(): Observable<PaymentMethod> =
    Observable.merge(
      paymentMethodClick
        .filter { checkedRadioButtonId -> checkedRadioButtonId >= 0 }
        .map { paymentMethodList[it] },
      preSelectedPaymentMethod
    )

  override fun getMorePaymentMethodsClicks(): Observable<Any> =
    RxView.clicks(binding.morePaymentMethods)

  override fun showLocalPayment(
    selectedPaymentMethod: String,
    iconUrl: String,
    label: String,
    async: Boolean,
    fiatAmount: String,
    fiatCurrency: String,
    gamificationLevel: Int
  ) =
    iabView.showLocalPayment(
      transactionBuilder!!.domain,
      transactionBuilder!!.skuId,
      fiatAmount,
      fiatCurrency,
      bonusMessageValue,
      selectedPaymentMethod,
      transactionBuilder!!.toAddress(),
      transactionBuilder!!.type,
      transactionBuilder!!.amount(),
      transactionBuilder!!.callbackUrl,
      transactionBuilder!!.orderReference,
      transactionBuilder!!.payload,
      transactionBuilder!!.origin,
      iconUrl,
      label,
      async,
      transactionBuilder!!.referrerUrl,
      gamificationLevel,
      transactionBuilder!!.guestWalletId
    )

  override fun setPurchaseBonus(bonus: BigDecimal, currency: String, @StringRes bonusText: Int) {
    var scaledBonus =
      bonus.stripTrailingZeros().setScale(CurrencyFormatUtils.FIAT_SCALE, BigDecimal.ROUND_DOWN)
    var newCurrencyString = currency
    if (scaledBonus < BigDecimal("0.01")) {
      newCurrencyString = "~$currency"
    }
    scaledBonus = scaledBonus.max(BigDecimal("0.01"))
    val formattedBonus = formatter.formatCurrency(scaledBonus, WalletCurrency.FIAT)
    bonusMessageValue = newCurrencyString + formattedBonus
    bonusValue = bonus
    binding.bonusLayout.bonusValue.text =
      if (isPortraitMode(requireContext())) context?.getString(
        R.string.gamification_purchase_header_part_2,
        bonusMessageValue
      ) else bonusMessageValue
  }

  override fun onBackPressed(): Observable<Any> =
    iabView.backButtonPress().map { itemAlreadyOwnedError }

  override fun showNext() =
    binding.dialogBuyButtonsPaymentMethods.buyButton.setText(getString(R.string.action_next))

  override fun showBuy() = setBuyButtonText()

  private fun setBuyButtonText() {
    val buyButtonText =
      if (isDonation) getString(R.string.action_donate) else getString(R.string.action_buy)
    binding.dialogBuyButtonsPaymentMethods.buyButton.setText(buyButtonText)
  }

  override fun showMergedAppcoins(
    gamificationLevel: Int,
    fiatValue: FiatValue,
    transaction: TransactionBuilder,
    frequency: String?,
    isSubscription: Boolean
  ) =
    iabView.showMergedAppcoins(
      fiatValue.amount,
      fiatValue.currency,
      bonusMessageValue,
      isBds,
      isDonation,
      gamificationLevel,
      transaction,
      isSubscription,
      frequency
    )

  override fun lockRotation() = iabView.lockRotation()

  override fun showEarnAppcoins() =
    iabView.showEarnAppcoins(
      transactionBuilder!!.domain,
      transactionBuilder!!.skuId,
      transactionBuilder!!.amount(),
      transactionBuilder!!.type
    )

  private fun isPortraitMode(context: Context): Boolean {
    val orientation = context.resources.configuration.orientation
    return orientation == ORIENTATION_PORTRAIT
  }

  private fun changeBonusVisibility(visibility: Int) {
    binding.bonusLayout.root.visibility = visibility
  }

  override fun showBonus(@StringRes bonusText: Int) {
    if (binding.bonusLayout.root.visibility != View.VISIBLE && isPortraitMode(requireContext())) {
      expandViewWithAnimation(0, dpToPx(50), binding.bonusLayout.root)
    } else {
      changeBonusVisibility(View.VISIBLE)
    }
  }


  override fun removeBonus() {
    bonusMessageValue = ""
    bonusValue = null
    if (binding.bonusLayout.root.visibility != View.GONE && isPortraitMode(requireContext())) {
      binding.bonusLayout.root.let {
        retractViewWithAnimation(it.height, 0, it, true)
      }
    } else {
      changeBonusVisibility(View.GONE)
    }
  }

  override fun hideBonus() {
    if (binding.bonusLayout.root.visibility != View.GONE && isPortraitMode(requireContext())) {
      binding.bonusLayout.root.let {
        retractViewWithAnimation(it.height, 0, it, true)
      }
    } else {
      changeBonusVisibility(View.GONE)
    }
  }

  override fun replaceBonus() {
    if (binding.bonusLayout.root.visibility != View.GONE && isPortraitMode(requireContext())) {
      binding.bonusLayout.root.let {
        retractViewWithAnimation(it.height, 0, it, true)
      }
    } else {
      changeBonusVisibility(View.GONE)
    }
  }

  private fun expandViewWithAnimation(startHeight: Int, targetHeight: Int, view: View) {
    view.visibility = View.VISIBLE
    val valueAnimator = createHeightAnimator(startHeight, targetHeight, view)
    valueAnimator.start()
  }

  private fun retractViewWithAnimation(
    startHeight: Int,
    targetHeight: Int,
    view: View,
    needChangeVisibility: Boolean
  ) {
    val valueAnimator = createHeightAnimator(startHeight, targetHeight, view)
    if (needChangeVisibility) {
      valueAnimator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
          // Set visibility to GONE when the animation ends
          view.visibility = View.GONE
        }
      })
    }
    valueAnimator.start()
  }

  private fun createHeightAnimator(startHeight: Int, targetHeight: Int, view: View): ValueAnimator {
    val animator = ValueAnimator.ofInt(startHeight, targetHeight)
    animator.duration = 500 // milliseconds

    animator.addUpdateListener { animation ->
      val animatedValue = animation.animatedValue as Int
      val layoutParams = view.layoutParams
      layoutParams.height = animatedValue
      view.layoutParams = layoutParams
    }

    return animator
  }

  private fun dpToPx(dp: Int): Int {
    val density = resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
  }

  override fun onAuthenticationResult(): Observable<Boolean> = iabView.onAuthenticationResult()

  private fun setupAppNameAndIcon() {
    if (isDonation) {
      binding.paymentMethodsHeader.appSkuDescription.text =
        resources.getString(R.string.item_donation)
      binding.paymentMethodsHeader.appName.text = resources.getString(R.string.item_donation)
    } else {
      compositeDisposable.add(
        Single.defer { Single.just(appPackage) }
          .observeOn(Schedulers.io())
          .map { packageName ->
            Pair(
              getApplicationName(packageName),
              requireContext().packageManager.getApplicationIcon(packageName)
            )
          }
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({ setHeaderInfo(it.first, it.second) }) { it.printStackTrace() })
    }
  }

  private fun setHeaderInfo(appName: String, appIcon: Drawable) {
    binding.paymentMethodsHeader.appName.text = appName
    binding.paymentMethodsHeader.appIcon.setImageDrawable(appIcon)
    binding.paymentMethodsHeader.appSkuDescription.text = transactionBuilder?.productName
  }

  private fun getApplicationName(packageName: String): String {
    val packageManager = requireContext().packageManager
    val packageInfo = packageManager.getApplicationInfo(packageName, 0)
    return packageManager.getApplicationLabel(packageInfo).toString()
  }

  private val isBds: Boolean by lazy {
    if (requireArguments().containsKey(IS_BDS)) {
      requireArguments().getBoolean(IS_BDS)
    } else {
      throw IllegalArgumentException("isBds data not found")
    }
  }

  private val isDonation: Boolean by lazy {
    if (requireArguments().containsKey(IS_DONATION)) {
      requireArguments().getBoolean(IS_DONATION)
    } else {
      throw IllegalArgumentException("isDonation data not found")
    }
  }

  private val transactionBuilder: TransactionBuilder? by lazy {
    if (requireArguments().containsKey(TRANSACTION)) {
      getParcelableExtra<TransactionBuilder>(TRANSACTION)
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val appPackage: String by lazy {
    if (requireArguments().containsKey(IabActivity.APP_PACKAGE)) {
      requireArguments().getString(IabActivity.APP_PACKAGE, "")
    } else {
      throw IllegalArgumentException("appPackage data not found")
    }
  }

  private fun getDeveloperPayload(): String? {
    return if (requireArguments().containsKey(IabActivity.DEVELOPER_PAYLOAD)) {
      requireArguments().getString(IabActivity.DEVELOPER_PAYLOAD, "")
    } else {
      throw IllegalArgumentException("developer payload data not found")
    }
  }

  private fun getUri(): String? {
    return if (requireArguments().containsKey(IabActivity.URI)) {
      requireArguments().getString(IabActivity.URI, "")
    } else {
      throw IllegalArgumentException("uri data not found")
    }
  }

  private fun removeSkeletons() {
    binding.paymentMethodsHeader.fiatPriceSkeleton.root.visibility = View.GONE
    binding.paymentMethodsHeader.appcPriceSkeleton.root.visibility = View.GONE
    binding.paymentsSkeleton.visibility = View.GONE
  }

  private fun getIsSubscription(): Boolean {
    return if (requireArguments().containsKey(IS_SUBSCRIPTION)) {
      requireArguments().getBoolean(IS_SUBSCRIPTION, false)
    } else {
      throw IllegalArgumentException("productName data not found")
    }
  }

  private fun getFrequency(): String? {
    return if (requireArguments().containsKey(FREQUENCY)) {
      requireArguments().getString(FREQUENCY, "")
    } else {
      throw IllegalArgumentException("productName data not found")
    }
  }
}

package com.asfoundation.wallet.ui.iab

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import com.appcoins.wallet.appcoins.rewards.ErrorInfo
import com.appcoins.wallet.appcoins.rewards.ResponseErrorBaseBody
import com.appcoins.wallet.appcoins.rewards.getMessage
import com.appcoins.wallet.bdsbilling.*
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType.Companion.valueOfInsensitive
import com.appcoins.wallet.bdsbilling.repository.RemoteRepository
import com.appcoins.wallet.bdsbilling.repository.TransactionsResponse
import com.appcoins.wallet.bdsbilling.repository.entity.*
import com.appcoins.wallet.bdsbilling.repository.entity.PurchaseState
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.appcoins.wallet.billing.AppcoinsBillingBinder
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.commons.Repository
import com.appcoins.wallet.gamification.Gamification
import com.appcoins.wallet.gamification.repository.*
import com.appcoins.wallet.gamification.repository.Status
import com.appcoins.wallet.gamification.repository.entity.*
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asfoundation.wallet.C
import com.asfoundation.wallet.analytics.AnalyticsSetup
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.adyen.PurchaseBundleModel
import com.asfoundation.wallet.billing.adyen._AdyenPaymentLogic
import com.asfoundation.wallet.billing.partners.AttributionEntity
import com.asfoundation.wallet.billing.partners.InstallerService
import com.asfoundation.wallet.billing.partners.OemIdExtractorService
import com.asfoundation.wallet.entity.*
import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.repository.*
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.service.currencies.ConversionResponseBody
import com.asfoundation.wallet.service.currencies.CurrencyConversionRatesPersistence
import com.asfoundation.wallet.service.currencies.LocalCurrencyConversionService
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.*
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor.CurrentPaymentStep
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod.*
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import com.asfoundation.wallet.util.*
import com.asfoundation.wallet.wallets.domain.WalletInfo
import com.asfoundation.wallet.wallets.repository.WalletInfoRepository
import com.google.gson.Gson
import ethereumj.crypto.ECKey
import ethereumj.crypto.HashUtil
import io.intercom.android.sdk.Intercom
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.kethereum.erc681.isEthereumURLString
import org.kethereum.erc681.parseERC681
import org.web3j.abi.datatypes.Address
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class _PaymentMethodsLogic(
  private val view: _View,
  private val navigator: _Navigator,
  private val analytics: PaymentMethodsAnalytics,
  private val transaction: TransactionBuilder,
  private val formatter: CurrencyFormatUtils,
  private val logger: Logger,
  private val passwordStore: PasswordStore,
  private val subsApi: SubscriptionBillingApi,
  private val gson: Gson,
  private val brokerBdsApi: RemoteRepository.BrokerBdsApi,
  private val gasSettingsRepository: GasSettingsRepository,
  private val defaultTokenRepository: _DefaultTokenRepository,
  private val transactionFromApprove: MutableMap<String, Transaction>,
  private val eipTransactionParser: _EIPTransactionParser,
  private val oneStepTransactionParser: _OneStepTransactionParser,
  private val defaultNetwork: NetworkInfo,
  private val installerService: InstallerService,
  private val oemIdExtractorService: OemIdExtractorService,
  private val countryCodeProvider: CountryCodeProvider,
  private val nonceObtainer: MultiWalletNonceObtainer,
  private val asfPaymentTransactionCache: Repository<String, PaymentTransaction>,
  private val bdsPaymentTransactionCache: Repository<String, PaymentTransaction>,
  private val asfBuyWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val bdsBuyWatchedTransactionCache: Repository<String, com.asfoundation.wallet.repository.Transaction>,
  private val bdsTrackTransactionCache: Repository<String, BdsTransactionService.BdsTransaction>,
  private val transactionIdsFromBuy: MutableMap<String, String>,
  private val inappBdsApi: RemoteRepository.InappBdsApi,
  private val promoCodeRepository: PromoCodeRepository,
  private val userStatsLocalData: UserStatsLocalData,
  private val gamificationApi: GamificationApi,
  private val api: GamificationApi,
  private val currencyConversionRatesPersistence: CurrencyConversionRatesPersistence,
  private val tokenToLocalFiatApi: LocalCurrencyConversionService.TokenToLocalFiatApi,
  private val supportRepository: SupportRepository,
  private val paymentMethodsData: PaymentMethodsData,
  private val walletInfoRepository: WalletInfoRepository,
  private val accountKeystoreService: AccountKeystoreService,
  private val analyticsSetUp: AnalyticsSetup,
  private var pref: SharedPreferences,
) {

  private val defaultStoreAddress: String = BuildConfig.DEFAULT_STORE_ADDRESS
  private val defaultOemAddress: String = BuildConfig.DEFAULT_OEM_ADDRESS

  private val setupSubject: PublishSubject<Boolean> = PublishSubject.create()

  private val normalizer = SignDataStandardNormalizer()
  private var stringECKeyPair: android.util.Pair<String, ECKey>? = null

  private var cachedGamificationLevel = 0
  private var cachedFiatValue: FiatValue? = null
  private var cachedPaymentNavigationData: PaymentNavigationData? = null
  private var viewState: ViewState = ViewState.DEFAULT
  private var hasStartedAuth = false
  private var loadedPaymentMethodEvent: String? = null

  private var isBonusActiveAndValid: Boolean = false

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      cachedGamificationLevel = savedInstanceState.getInt(GAMIFICATION_LEVEL)
      hasStartedAuth = savedInstanceState.getBoolean(HAS_STARTED_AUTH)
      cachedFiatValue = savedInstanceState.getSerializable(FIAT_VALUE) as FiatValue?
      cachedPaymentNavigationData =
        savedInstanceState.getSerializable(PAYMENT_NAVIGATION_DATA) as PaymentNavigationData?
    }
    handleOnGoingPurchases()
  }

  private fun onTopupClick() {
    view.setState(_TopupFlowViewState)
  }

  fun onResume(firstRun: Boolean) {
    if (firstRun.not()) view.setState(_PaymentsSkeletonLoadingViewState)
    setupUi(firstRun)
  }

  private fun onPaymentSelection(id: String) {
    if (paymentMethodsData.isBds) {
      Observable.just(id)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { selectedPaymentMethod ->
          if (isBonusActiveAndValid) {
            handleBonusVisibility(selectedPaymentMethod)
          } else {
            view.setState(_NoBonusViewState)
          }
          handlePositiveButtonText(selectedPaymentMethod)
        }
        .subscribe({}, { it.printStackTrace() })
        .isDisposed
    }
  }

  private fun onBuyClick() {
    Observable.just(null)
      .map { getSelectedPaymentMethod(hasPreSelectedPaymentMethod()) }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext { handleBuyAnalytics(it) }
      .doOnNext { selectedPaymentMethod ->
        when (map(selectedPaymentMethod.id)) {
          APPC_CREDITS -> {
            view.setState(_LoadingViewState)
            handleWalletBlockStatus(selectedPaymentMethod)
          }
          MERGED_APPC -> view.setState(
            _MergedAppcoinsViewState(
              cachedGamificationLevel,
              cachedFiatValue!!,
              transaction,
              paymentMethodsData.frequency,
              paymentMethodsData.subscription
            )
          )

          else -> if (hasAuthenticationPermission()) {
            showAuthenticationActivity(
              selectedPaymentMethod,
              hasPreSelectedPaymentMethod()
            )
          } else {
            when (map(selectedPaymentMethod.id)) {
              PAYPAL -> view.setState(
                _PaypalViewState(
                  cachedGamificationLevel,
                  cachedFiatValue!!,
                  paymentMethodsData.frequency,
                  paymentMethodsData.subscription
                )
              )
              CREDIT_CARD -> view.setState(
                _CreditCardViewState(
                  cachedGamificationLevel,
                  cachedFiatValue!!,
                  paymentMethodsData.frequency,
                  paymentMethodsData.subscription
                )
              )
              APPC -> view.setState(_AppCoinsViewState(cachedGamificationLevel, transaction))
              SHARE_LINK -> view.setState(_ShareLinkViewState(selectedPaymentMethod.id))
              LOCAL_PAYMENTS -> view.setState(
                _LocalPaymentViewState(
                  selectedPaymentMethod.id,
                  selectedPaymentMethod.iconUrl,
                  selectedPaymentMethod.label,
                  selectedPaymentMethod.async,
                  cachedFiatValue!!.amount.toString(),
                  cachedFiatValue!!.currency,
                  cachedGamificationLevel
                )
              )
              CARRIER_BILLING -> view.setState(_CarrierBillingViewState(cachedFiatValue!!, false))
              else -> return@doOnNext
            }
          }
        }
      }
      .retry()
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  fun onAuthenticationResult(auth: Boolean) {
    Observable.just(auth)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnNext {
        analytics.stopTimingForAuthEvent()
        if (cachedPaymentNavigationData == null) {
          close()
        } else if (!it) {
          hasStartedAuth = false
          if (
            cachedPaymentNavigationData!!.isPreselected &&
            hasPaymentOwnPreselectedView(cachedPaymentNavigationData!!.paymentId)
          ) {
            close()
          }
        } else {
          navigateToPayment(cachedPaymentNavigationData!!)
        }
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun hasPaymentOwnPreselectedView(paymentId: String): Boolean {
    val paymentMethod = map(paymentId)
    return paymentMethod == CREDIT_CARD ||
        paymentMethod == CARRIER_BILLING
  }

  private fun handleWalletBlockStatus(selectedPaymentMethod: PaymentMethod) {
    isWalletBlocked()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess {
        if (hasAuthenticationPermission()) {
          showAuthenticationActivity(
            selectedPaymentMethod,
            hasPreSelectedPaymentMethod()
          )
        } else {
          view.setState(_CreditsViewState(cachedGamificationLevel, transaction))
        }
      }
      .doOnError { showError(it) }
      .subscribe({}, { showError(it) })
      .isDisposed
  }

  private fun handleOnGoingPurchases() {
    val billingSupportedType =
      transaction.type?.let { BillingSupportedType.valueOfInsensitive(it) }
    if (transaction.skuId == null || billingSupportedType == null) {
      isSetupCompleted()
        .doOnComplete { view.setState(_LoadedViewState) }
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe({ stopTimingForTotalEvent() }, { it.printStackTrace() })
        .isDisposed
    } else {
      waitForUi(transaction.skuId, billingSupportedType)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnComplete { view.setState(_LoadedViewState) }
        .subscribe({ stopTimingForTotalEvent() }, { showError(it) })
        .isDisposed
    }
  }

  private fun navigateToPayment(paymentNavigationData: PaymentNavigationData) =
    when (map(paymentNavigationData.paymentId)) {
      PAYPAL -> view.setState(
        _PaypalViewState(
          cachedGamificationLevel,
          cachedFiatValue!!,
          paymentMethodsData.frequency,
          paymentMethodsData.subscription
        )
      )
      CREDIT_CARD -> if (paymentNavigationData.isPreselected) {
        view.setState(
          _AdyenAppcoinsViewState(
            cachedFiatValue!!.amount,
            cachedFiatValue!!.currency,
            PaymentType.CARD,
            paymentNavigationData.paymentIconUrl,
            cachedGamificationLevel,
            paymentMethodsData.frequency,
            paymentMethodsData.subscription
          )
        )
      } else {
        view.setState(
          _CreditCardViewState(
            cachedGamificationLevel,
            cachedFiatValue!!,
            paymentMethodsData.frequency,
            paymentMethodsData.subscription
          )
        )
      }
      APPC -> view.setState(_AppCoinsViewState(cachedGamificationLevel, transaction))
      APPC_CREDITS -> view.setState(_CreditsViewState(cachedGamificationLevel, transaction))
      SHARE_LINK -> view.setState(_ShareLinkViewState(paymentNavigationData.paymentId))
      LOCAL_PAYMENTS -> view.setState(
        _LocalPaymentViewState(
          paymentNavigationData.paymentId,
          paymentNavigationData.paymentIconUrl,
          paymentNavigationData.paymentLabel,
          paymentNavigationData.async,
          cachedFiatValue!!.amount.toString(),
          cachedFiatValue!!.currency,
          cachedGamificationLevel
        )
      )
      CARRIER_BILLING -> view.setState(
        _CarrierBillingViewState(
          cachedFiatValue!!,
          paymentNavigationData.isPreselected
        )
      )

      else -> {
        showError(R.string.unknown_error)
        logger.log(TAG, "Wrong payment method after authentication.")
      }
    }

  private fun isSetupCompleted(): Completable = setupSubject
    .takeWhile { isViewSet -> !isViewSet }
    .ignoreElements()

  private fun waitForUi(skuId: String?, type: BillingSupportedType): Completable =
    Completable.mergeArray(
      checkProcessing(skuId, type).subscribeOn(Schedulers.io()),
      checkForOwnedItems(skuId, type).subscribeOn(Schedulers.io()),
      isSetupCompleted().subscribeOn(Schedulers.io())
    )

  private fun checkForOwnedItems(skuId: String?, type: BillingSupportedType): Completable =
    Single.zip(
      checkAndConsumePrevious(skuId, type).subscribeOn(Schedulers.io()),
      checkSubscriptionOwned(skuId, type).subscribeOn(Schedulers.io())
    ) { itemOwned: Boolean, subStatus: SubscriptionStatus -> Pair(itemOwned, subStatus) }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { handleItemsOwned(it.first, it.second) }
      .ignoreElement()

  private fun handleItemsOwned(itemOwned: Boolean, subStatus: SubscriptionStatus) =
    if (itemOwned) {
      viewState = ViewState.ITEM_ALREADY_OWNED
      view.setState(_ItemAlreadyOwnedErrorErrorViewState)
    } else {
      handleSubscriptionAvailability(subStatus)
    }

  private fun checkSubscriptionOwned(
    skuId: String?,
    type: BillingSupportedType
  ): Single<SubscriptionStatus> =
    if (type == BillingSupportedType.INAPP_SUBSCRIPTION && skuId != null) {
      isAbleToSubscribe(paymentMethodsData.appPackage, skuId, Schedulers.io())
        .subscribeOn(Schedulers.io())
    } else {
      Single.just(SubscriptionStatus(true))
    }

  private fun handleSubscriptionAvailability(status: SubscriptionStatus) = status
    .takeUnless { it.isAvailable }
    ?.run {
      if (isAlreadySubscribed) {
        showError(R.string.subscriptions_error_already_subscribed)
      } else {
        showError(R.string.unknown_error)
      }
    }

  private fun checkProcessing(skuId: String?, type: BillingSupportedType): Completable =
    getSkuTransaction(paymentMethodsData.appPackage, skuId, Schedulers.io(), type)
      .subscribeOn(Schedulers.io())
      .filter { (_, status) -> status === Transaction.Status.PROCESSING }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { view.setState(_ProcessingLoadingViewState) }
      .doOnSuccess { handleProcessing() }
      .observeOn(Schedulers.io())
      .flatMapCompletable {
        checkTransactionStateFromTransactionId(it.uid)
          .ignoreElements()
          .andThen(
            finishProcess(
              skuId,
              it.type,
              it.orderReference,
              it.hash,
              it.metadata!!.purchaseUid
            )
          )
      }

  private fun handleProcessing() {
    getCurrentPaymentStep(paymentMethodsData.appPackage, transaction)
      .filter { currentPaymentStep -> currentPaymentStep == CurrentPaymentStep.PAUSED_ON_CHAIN }
      .doOnSuccess {
//        view.lockRotation()
        resume(
          paymentMethodsData.uri!!,
          AsfInAppPurchaseInteractor.TransactionType.NORMAL,
          paymentMethodsData.appPackage,
          transaction.skuId,
          paymentMethodsData.developerPayload,
          paymentMethodsData.isBds,
          transaction.type,
          transaction
        )
      }
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun finishProcess(
    skuId: String?,
    type: String,
    orderReference: String?,
    hash: String?,
    purchaseUid: String
  ): Completable =
    getSkuPurchase(
      appPackage = paymentMethodsData.appPackage,
      skuId = skuId,
      purchaseUid = purchaseUid,
      type = type,
      orderReference = orderReference,
      hash = hash,
      networkThread = Schedulers.io()
    )
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { bundle -> navigator.navigate(_Finish(bundle.bundle)) }
      .ignoreElement()

  private fun checkAndConsumePrevious(sku: String?, type: BillingSupportedType): Single<Boolean> =
    getPurchases(type)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .map { purchases -> hasRequestedSkuPurchase(purchases, sku) }

  private fun setupUi(firstRun: Boolean) {
    Completable.fromAction {
      if (firstRun) analytics.startTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_WALLET_INFO)
    }
      .andThen(
        getWalletInfo(null, cached = false, updateFiat = true)
          .subscribeOn(Schedulers.io())
          .map { "" }
          .onErrorReturnItem("")
      )
      .flatMap {
        if (firstRun) {
          analytics.stopTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_WALLET_INFO)
          analytics.startTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_CONVERT_TO_FIAT)
        }
        return@flatMap getPurchaseFiatValue()
      }
      .flatMapCompletable { fiatValue ->
        if (firstRun) analytics.stopTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_CONVERT_TO_FIAT)
        this.cachedFiatValue = fiatValue
        if (firstRun) analytics.startTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_PAYMENT_METHODS)
        getPaymentMethods(fiatValue)
          .flatMapCompletable { paymentMethods ->
            if (firstRun) {
              analytics.stopTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_PAYMENT_METHODS)
              analytics.startTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_EARNING_BONUS)
            }
            getEarningBonus(transaction.domain, transaction.amount())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMapCompletable {
                if (firstRun) analytics.stopTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_EARNING_BONUS)
                Completable.fromAction {
                  if (firstRun) analytics.startTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_PROCESSING_DATA)
//                  view.updateProductName()
                  setupBonusInformation(it)
                  selectPaymentMethod(
                    paymentMethods,
                    fiatValue,
                    isBonusActiveAndValid(it)
                  )
                  if (firstRun) analytics.stopTimingForStepEvent(PaymentMethodsAnalytics.LOADING_STEP_GET_PROCESSING_DATA)
                }
              }
          }
      }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        //If first run we should rely on the hideLoading of the handleOnGoingPurchases method
        if (!firstRun) view.setState(_LoadedViewState)
        else stopTimingForTotalEvent()
      }, { this.showError(it) })
      .isDisposed
  }

  private fun showError(t: Throwable) {
    t.printStackTrace()
    logger.log(TAG, t)
    when {
      t.isNoNetworkException() -> view.setState(_ErrorViewState(R.string.notification_no_network_poa))
      isItemAlreadyOwnedError(t) -> {
        viewState = ViewState.ITEM_ALREADY_OWNED
        view.setState(_ItemAlreadyOwnedErrorErrorViewState)
      }
      else -> view.setState(_ErrorViewState(R.string.activity_iab_error_message))
    }
  }

  private fun setupBonusInformation(forecastBonus: ForecastBonusAndLevel) {
    if (isBonusActiveAndValid(forecastBonus)) {
      if (paymentMethodsData.subscription) {
        view.setState(
          _PurchaseBonusViewState(
            forecastBonus.amount,
            forecastBonus.currency,
            R.string.subscriptions_bonus_body
          )
        )
      } else {
        view.setState(
          _PurchaseBonusViewState(
            forecastBonus.amount,
            forecastBonus.currency,
            R.string.gamification_purchase_body
          )
        )
      }
    } else {
      view.setState(_NoBonusViewState)
    }
    cachedGamificationLevel = forecastBonus.level
    analytics.setGamificationLevel(cachedGamificationLevel)
  }

  private fun selectPaymentMethod(
    paymentMethods: List<PaymentMethod>,
    fiatValue: FiatValue,
    isBonusActive: Boolean
  ) {
    val fiatAmount = formatter.formatPaymentCurrency(fiatValue.amount, WalletCurrency.FIAT)
    val appcAmount = formatter.formatPaymentCurrency(transaction.amount(), WalletCurrency.APPCOINS)
    if (hasAsyncLocalPayment()) {
      //After a asynchronous payment credits will be used as pre selected
      getCreditsPaymentMethod(paymentMethods)?.let {
        if (it.isEnabled) {
          showPreSelectedPaymentMethod(
            fiatValue,
            it,
            fiatAmount,
            appcAmount,
            isBonusActive,
            paymentMethodsData.frequency
          )
          return
        }
      }
    }

    if (hasPreSelectedPaymentMethod()) {
      val paymentMethod = getPreSelectedPaymentMethod(paymentMethods)
      if (paymentMethod == null || !paymentMethod.isEnabled) {
        showPaymentMethods(
          fiatValue,
          paymentMethods,
          PaymentMethodId.CREDIT_CARD.id,
          fiatAmount,
          appcAmount,
          paymentMethodsData.frequency
        )
      } else {
        when (paymentMethod.id) {
          PaymentMethodId.CARRIER_BILLING.id, PaymentMethodId.CREDIT_CARD.id -> {
            if (viewState == ViewState.DEFAULT) {
              analytics.sendPurchaseDetailsEvent(
                paymentMethodsData.appPackage, transaction.skuId, transaction.amount()
                  .toString(), transaction.type
              )
              if (hasAuthenticationPermission() && !hasStartedAuth) {
                showAuthenticationActivity(paymentMethod, true)
                hasStartedAuth = true
              } else if (paymentMethod.id == PaymentMethodId.CREDIT_CARD.id) {
                view.setState(
                  _AdyenAppcoinsViewState(
                    fiatValue.amount,
                    fiatValue.currency,
                    PaymentType.CARD,
                    paymentMethod.iconUrl,
                    cachedGamificationLevel,
                    paymentMethodsData.frequency,
                    paymentMethodsData.subscription
                  )
                )
              } else if (paymentMethod.id == PaymentMethodId.CARRIER_BILLING.id) {
                view.setState(_CarrierBillingViewState(fiatValue, true))
              }
            }
          }
          else -> showPreSelectedPaymentMethod(
            fiatValue,
            paymentMethod,
            fiatAmount,
            appcAmount,
            isBonusActive,
            paymentMethodsData.frequency
          )
        }
      }
    } else if (paymentMethods.size == 1
      && paymentMethods[0].id == PaymentMethodId.APPC_CREDITS.id
      && paymentMethods[0].isEnabled
    ) {
      showPaymentMethods(
        fiatValue,
        paymentMethods,
        paymentMethods[0].id,
        fiatAmount,
        appcAmount,
        paymentMethodsData.frequency
      )
    } else {
      val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
      showPaymentMethods(
        fiatValue,
        paymentMethods,
        paymentMethodId,
        fiatAmount,
        appcAmount,
        paymentMethodsData.frequency
      )
    }
  }

  private fun getCreditsPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    paymentMethods.forEach {
      if (it.id == PaymentMethodId.MERGED_APPC.id) {
        val mergedPaymentMethod = it as AppCoinsPaymentMethod
        return PaymentMethod(
          PaymentMethodId.APPC_CREDITS.id,
          mergedPaymentMethod.creditsLabel,
          mergedPaymentMethod.iconUrl,
          mergedPaymentMethod.async,
          mergedPaymentMethod.fee,
          mergedPaymentMethod.isCreditsEnabled
        )
      }
      if (it.id == PaymentMethodId.APPC_CREDITS.id) {
        return PaymentMethod(
          it.id,
          it.label,
          it.iconUrl,
          it.async,
          it.fee,
          it.isEnabled,
          it.disabledReason,
          paymentMethods.size == 1
        )
      }
    }

    return null
  }

  private fun showPaymentMethods(
    fiatValue: FiatValue,
    paymentMethods: List<PaymentMethod>,
    paymentMethodId: String,
    fiatAmount: String,
    appcAmount: String,
    frequency: String?
  ) {
    var appcEnabled = false
    var creditsEnabled = false
    val paymentList: MutableList<PaymentMethod>
    val symbol = mapCurrencyCodeToSymbol(fiatValue.currency)
    if (paymentMethodsData.isBds) {
      paymentMethods.forEach {
        if (it is AppCoinsPaymentMethod) {
          appcEnabled = it.isAppcEnabled
          creditsEnabled = it.isCreditsEnabled
        }
      }
      paymentList = paymentMethods.toMutableList()
    } else {
      paymentList = paymentMethods
        .filter { it.id == map(APPC) }
        .toMutableList()
    }
    setLoadedPayment("")
    view.setState(
      _PaymentMethodsDataViewState(
        paymentList,
        symbol,
        paymentMethodId,
        fiatAmount,
        appcAmount,
        appcEnabled,
        creditsEnabled,
        frequency,
        paymentMethodsData.subscription
      )
    )
    setupSubject.onNext(true)
    sendPaymentMethodsEvents()
  }

  private fun showPreSelectedPaymentMethod(
    fiatValue: FiatValue,
    paymentMethod: PaymentMethod,
    fiatAmount: String,
    appcAmount: String,
    isBonusActive: Boolean,
    frequency: String?
  ) {
    setLoadedPayment(paymentMethod.id)
    view.setState(
      _PreSelectedPaymentMethodViewState(
        paymentMethod,
        mapCurrencyCodeToSymbol(fiatValue.currency),
        fiatAmount,
        appcAmount,
        isBonusActive,
        frequency,
        paymentMethodsData.subscription
      )
    )
    setupSubject.onNext(true)
    sendPreSelectedPaymentMethodsEvents()
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String =
    if (currencyCode.equals("APPC", ignoreCase = true)) {
      currencyCode
    } else {
      Currency.getInstance(currencyCode).currencyCode
    }

  private fun onCancelClick() {
    Observable.just(null)
      .map { getSelectedPaymentMethod(hasPreSelectedPaymentMethod()) }
      .observeOn(Schedulers.io())
      .doOnNext { sendCancelPaymentMethodAnalytics(it) }
      .subscribe { close() }
      .isDisposed
  }

  private fun sendCancelPaymentMethodAnalytics(paymentMethod: PaymentMethod) =
    analytics.sendPaymentMethodEvent(
      paymentMethodsData.appPackage,
      transaction.skuId,
      transaction.amount()
        .toString(),
      paymentMethod.id,
      transaction.type,
      "cancel",
      hasPreSelectedPaymentMethod()
    )

  private fun onMorePaymentMethodClicks() {
    Observable.just(null)
      .map { getSelectedPaymentMethod(hasPreSelectedPaymentMethod()) }
      .observeOn(Schedulers.io())
      .doOnNext { selectedPaymentMethod ->
        analytics.sendPaymentMethodEvent(
          paymentMethodsData.appPackage, transaction.skuId, transaction.amount()
            .toString(), selectedPaymentMethod.id, transaction.type, "other_payments"
        )
      }
      .observeOn(AndroidSchedulers.mainThread())
      .doOnEach { view.setState(_SkeletonLoadingViewState) }
      .flatMapSingle {
        if (cachedFiatValue == null) {
          getPurchaseFiatValue().subscribeOn(Schedulers.io())
        } else {
          Single.just(cachedFiatValue)
        }
      }
      .flatMapCompletable { fiatValue ->
        cachedFiatValue = fiatValue
        getPaymentMethods(fiatValue).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .flatMapCompletable { paymentMethods ->
            Completable.fromAction {
              val fiatAmount =
                formatter.formatPaymentCurrency(fiatValue.amount, WalletCurrency.FIAT)
              val appcAmount =
                formatter.formatPaymentCurrency(transaction.amount(), WalletCurrency.APPCOINS)
              val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
              showPaymentMethods(
                fiatValue,
                paymentMethods,
                paymentMethodId,
                fiatAmount,
                appcAmount,
                paymentMethodsData.frequency
              )
            }
          }
          .andThen(Completable.fromAction { removePreSelectedPaymentMethod() })
          .andThen(Completable.fromAction { removeAsyncLocalPayment() })
          .andThen(Completable.fromAction { view.setState(_LoadedViewState) })
      }
      .subscribe({ }, { this.showError(it) })
      .isDisposed
  }

  private fun showError(@StringRes message: Int) {
    if (viewState != ViewState.ITEM_ALREADY_OWNED) {
      viewState = ViewState.ERROR
      view.setState(_ErrorViewState(message))
    }
  }

  private fun isItemAlreadyOwnedError(throwable: Throwable): Boolean =
    throwable is HttpException && throwable.code() == 409

  private fun close() = navigator.navigate(_Close(mapCancellation()))

  private fun onErrorDismisses() {
    Observable.just(null)
      .flatMapCompletable {
        if (viewState == ViewState.ITEM_ALREADY_OWNED) {
          val type = BillingSupportedType.valueOfInsensitive(transaction.type)
          getPurchases(type).doOnSuccess { purchases ->
            val purchase = getRequestedSkuPurchase(purchases, transaction.skuId)
            purchase?.let { finishItemAlreadyOwned(it) } ?: navigator.navigate(_Close(Bundle()))
          }
            .ignoreElement()
        } else {
          return@flatMapCompletable Completable.fromAction { navigator.navigate(_Close(Bundle())) }
        }
      }
      .subscribe({ }, { navigator.navigate(_Close(Bundle())) })
      .isDisposed
  }

  private fun handleSupportClicks() {
    showSupport(cachedGamificationLevel)
      .subscribe({}, { it.printStackTrace() })
      .isDisposed
  }

  private fun finishItemAlreadyOwned(purchase: Purchase) =
    navigator.navigate(_Finish(mapFinishedPurchase(purchase, true)))

  private fun sendPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(
      paymentMethodsData.appPackage,
      transaction.skuId,
      transaction.amount().toString(),
      transaction.type
    )
  }

  private fun sendPreSelectedPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(
      paymentMethodsData.appPackage,
      transaction.skuId,
      transaction.amount().toString(),
      transaction.type
    )
  }

  private fun getPaymentMethods(fiatValue: FiatValue): Single<List<PaymentMethod>> =
    if (paymentMethodsData.isBds) {
      getPaymentMethods(transaction, fiatValue.amount.toString(), fiatValue.currency)
        .map { mergeAppcoins(it) }
        .map { swapDisabledPositions(it) }
    } else {
      Single.just(listOf(PaymentMethod.APPC))
    }

  private fun getPreSelectedPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    val preSelectedPreference = getPreSelectedPaymentMethod()
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == PaymentMethodId.MERGED_APPC.id) {
        if (preSelectedPreference == PaymentMethodId.APPC.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(
            PaymentMethodId.APPC.id,
            mergedPaymentMethod.appcLabel,
            mergedPaymentMethod.iconUrl,
            mergedPaymentMethod.async,
            mergedPaymentMethod.fee,
            mergedPaymentMethod.isAppcEnabled
          )
        }
        if (preSelectedPreference == PaymentMethodId.APPC_CREDITS.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(
            PaymentMethodId.APPC_CREDITS.id,
            mergedPaymentMethod.creditsLabel,
            paymentMethod.creditsIconUrl,
            mergedPaymentMethod.async,
            mergedPaymentMethod.fee,
            mergedPaymentMethod.isCreditsEnabled
          )
        }
      }
      if (paymentMethod.id == preSelectedPreference) return paymentMethod
    }
    return null
  }

  private fun getLastUsedPaymentMethod(paymentMethods: List<PaymentMethod>): String {
    val lastUsedPaymentMethod = getLastUsedPaymentMethod()
    for (it in paymentMethods) {
      if (it.isEnabled) {
        if (it.id == PaymentMethodId.MERGED_APPC.id &&
          (lastUsedPaymentMethod == PaymentMethodId.APPC.id ||
              lastUsedPaymentMethod == PaymentMethodId.APPC_CREDITS.id)
        ) {
          return PaymentMethodId.MERGED_APPC.id
        }
        if (it.id == lastUsedPaymentMethod) {
          return it.id
        }
      }
    }
    return PaymentMethodId.CREDIT_CARD.id
  }

  private fun handleBonusVisibility(selectedPaymentMethod: String) {
    when (selectedPaymentMethod) {
      map(EARN_APPC) -> view.setState(_ReplaceBonusViewState)
      map(MERGED_APPC) -> view.setState(_HideBonusViewState)
      map(APPC_CREDITS) -> view.setState(_HideBonusViewState)
      else -> if (paymentMethodsData.subscription) {
        view.setState(_BonusViewState(R.string.subscriptions_bonus_body))
      } else {
        view.setState(_BonusViewState(R.string.gamification_purchase_body))
      }
    }
  }

  private fun handlePositiveButtonText(selectedPaymentMethod: String) =
    if (
      selectedPaymentMethod == map(MERGED_APPC) ||
      selectedPaymentMethod == map(EARN_APPC)
    ) {
//      view.showNext()
      Unit
    } else if (paymentMethodsData.subscription) {
//      view.showSubscribe()
      Unit
    } else {
//      view.showBuy()
      Unit
    }

  private fun handleBuyAnalytics(selectedPaymentMethod: PaymentMethod) {
    val action =
      if (selectedPaymentMethod.id == PaymentMethodId.MERGED_APPC.id) "next" else "buy"
    if (hasPreSelectedPaymentMethod()) {
      analytics.sendPaymentMethodEvent(
        paymentMethodsData.appPackage, transaction.skuId, transaction.amount()
          .toString(), selectedPaymentMethod.id, transaction.type, action
      )
    } else {
      analytics.sendPaymentMethodEvent(
        paymentMethodsData.appPackage, transaction.skuId, transaction.amount()
          .toString(), selectedPaymentMethod.id, transaction.type, action
      )
    }
  }

  private fun getPurchases(type: BillingSupportedType): Single<List<Purchase>> =
    getPurchases(paymentMethodsData.appPackage, type, Schedulers.io())

  private fun hasRequestedSkuPurchase(purchases: List<Purchase>, sku: String?): Boolean {
    for (purchase in purchases) {
      if (isRequestedSkuPurchase(purchase, sku)) return true
    }
    return false
  }

  private fun getRequestedSkuPurchase(purchases: List<Purchase>, sku: String?): Purchase? {
    for (purchase in purchases) {
      if (isRequestedSkuPurchase(purchase, sku)) return purchase
    }
    return null
  }

  private fun isRequestedSkuPurchase(purchase: Purchase, sku: String?): Boolean =
    purchase.product.name == sku && purchase.state != State.CONSUMED && purchase.state != State.ACKNOWLEDGED

  private fun showAuthenticationActivity(paymentMethod: PaymentMethod, isPreselected: Boolean) {
    analytics.startTimingForAuthEvent()
    cachedPaymentNavigationData =
      PaymentNavigationData(
        paymentMethod.id,
        paymentMethod.label,
        paymentMethod.iconUrl,
        paymentMethod.async,
        isPreselected
      )
    view.setState(_AuthenticationViewState)
  }

  fun onSavedInstance(outState: Bundle) {
    outState.putInt(GAMIFICATION_LEVEL, cachedGamificationLevel)
    outState.putBoolean(HAS_STARTED_AUTH, hasStartedAuth)
    outState.putSerializable(FIAT_VALUE, cachedFiatValue)
    outState.putSerializable(PAYMENT_NAVIGATION_DATA, cachedPaymentNavigationData)
  }

  private fun getPurchaseFiatValue(): Single<FiatValue> {
    // TODO when adding new currency configurable on the settings we need to update this logic to be
    //  aligned with the currency the user chooses
    val billingSupportedType = BillingSupportedType.valueOfInsensitive(transaction.type)
    return getSkuDetails(
      paymentMethodsData.appPackage,
      paymentMethodsData.sku,
      billingSupportedType
    )
      .map { product ->
        val price = product.transactionPrice
        // NOTE we need to convert a double to a string in order to create the big decimal since the
        // direct use of double won't result in the value you expect changing this will be a bad
        // idea ate least for now
        // Setup the appc value according with what we have on the microservices instead of
        // converting, avoiding this way a additional call to the backend
        transaction.amount(BigDecimal(price.appcoinsAmount.toString()))
        transaction.productName = product.title
        return@map FiatValue(
          BigDecimal(price.amount.toString()),
          price.currency,
          price.currencySymbol
        )
      }
      .onErrorResumeNext(
        Single.zip(
          convertCurrencyToLocalFiat(
            getOriginalValue().toDouble(),
            getOriginalCurrency()
          ),
          setTransactionAppcValue(transaction)
        ) { fiatValue, _ -> fiatValue })
  }

  private fun getOriginalValue(): BigDecimal =
    if (transaction.originalOneStepValue.isNullOrEmpty()) {
      transaction.amount()
    } else {
      BigDecimal(transaction.originalOneStepValue)
    }

  private fun getOriginalCurrency(): String =
    if (transaction.originalOneStepCurrency.isNullOrEmpty()) {
      "APPC"
    } else {
      transaction.originalOneStepCurrency
    }

  private fun setTransactionAppcValue(transaction: TransactionBuilder): Single<FiatValue> =
    if (transaction.amount().compareTo(BigDecimal.ZERO) == 0) {
      convertCurrencyToAppc(getOriginalValue().toDouble(), getOriginalCurrency())
        .map { appcValue ->
          transaction.amount(appcValue.amount)
          appcValue
        }
    } else {
      Single.just(FiatValue(transaction.amount(), "APPC"))
    }

  private fun setLoadedPayment(paymentMethodId: String) {
    loadedPaymentMethodEvent = when (paymentMethodId) {
      PaymentMethodId.PAYPAL.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP
      PaymentMethodId.APPC.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_APPC
      PaymentMethodId.APPC_CREDITS.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_APPC
      PaymentMethodId.MERGED_APPC.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_APPC
      PaymentMethodId.CREDIT_CARD.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_CC
      PaymentMethodId.CARRIER_BILLING.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_LOCAL
      PaymentMethodId.ASK_FRIEND.id -> PaymentMethodsAnalytics.PAYMENT_METHOD_ASK_FRIEND
      else -> PaymentMethodsAnalytics.PAYMENT_METHOD_SELECTION
    }
  }

  private fun stopTimingForTotalEvent() {
    val paymentMethod = loadedPaymentMethodEvent ?: return
    loadedPaymentMethodEvent = null
    analytics.stopTimingForTotalEvent(paymentMethod)
  }

  /**
   * Flatten logic
   */

  private fun getSelectedPaymentMethod(hasPreSelectedPaymentMethod: Boolean): PaymentMethod {
    return if (hasPreSelectedPaymentMethod) PaymentMethod() else PaymentMethod()
  }

  /**
   * Retrieves WalletInfo
   *
   * @param address Wallet address, or null to use the currently active wallet
   * @param cached true to return the cached WalletInfo, or false if it should retrieve from network
   * @param updateFiat true if it should also update fiat, or false if not necessary
   */
  private fun getWalletInfo(
    address: String?,
    cached: Boolean,
    updateFiat: Boolean
  ): Single<WalletInfo> {
    val walletAddressSingle =
      address?.let { Single.just(Wallet(address)) } ?: getCurrentWallet()
    return if (cached) {
      walletAddressSingle.flatMap {
        walletInfoRepository.getCachedWalletInfo(it.address)
      }
    } else {
      walletAddressSingle.flatMap {
        walletInfoRepository.getLatestWalletInfo(it.address, updateFiat)
      }
    }
  }

  private fun getCurrentWallet(): Single<Wallet> {
    return getDefaultWallet()
      .onErrorResumeNext {
        fetchWallets()
          .filter { wallets -> wallets.isNotEmpty() }
          .map { wallets: Array<Wallet> ->
            wallets[0]
          }
          .flatMapCompletable { wallet: Wallet ->
            setDefaultWallet(wallet.address)
          }
          .andThen(getDefaultWallet())
      }
  }

  private fun getDefaultWallet(): Single<Wallet> =
    Single.fromCallable { getDefaultWalletAddress() }
      .flatMap { address -> findWallet(address) }

  private fun getDefaultWalletAddress(): String {
    val currentWalletAddress = getCurrentWalletAddress()
    return currentWalletAddress ?: throw WalletNotFoundException()
  }

  private fun findWallet(address: String): Single<Wallet> {
    return fetchWallets().flatMap { accounts ->
      for (wallet in accounts) {
        if (wallet.hasSameAddress(address)) {
          return@flatMap Single.just(wallet)
        }
      }
      null
    }
  }

  private fun fetchWallets(): Single<Array<Wallet>> = accountKeystoreService.fetchAccounts()

  private fun setDefaultWallet(address: String) = Completable.fromAction {
    analyticsSetUp.setUserId(address)
    setCurrentWalletAddress(address)
  }

  private fun getCurrentWalletAddress() =
    pref.getString(CURRENT_ACCOUNT_ADDRESS_KEY, null)

  private fun setCurrentWalletAddress(address: String) = pref.edit()
    .putString(SharedPreferencesRepository.CURRENT_ACCOUNT_ADDRESS_KEY, address)
    .apply()

  private fun map(paymentId: String): PaymentMethodsView.SelectedPaymentMethod {
    return when (paymentId) {
      "ask_friend" -> SHARE_LINK
      "paypal" -> PAYPAL
      "credit_card" -> CREDIT_CARD
      "appcoins" -> APPC
      "appcoins_credits" -> APPC_CREDITS
      "merged_appcoins" -> MERGED_APPC
      "earn_appcoins" -> EARN_APPC
      "onebip" -> CARRIER_BILLING
      "" -> ERROR
      else -> LOCAL_PAYMENTS
    }
  }

  private fun map(selectedPaymentMethod: PaymentMethodsView.SelectedPaymentMethod): String {
    return when (selectedPaymentMethod) {
      SHARE_LINK -> "ask_friend"
      PAYPAL -> "paypal"
      CREDIT_CARD -> "credit_card"
      APPC -> "appcoins"
      APPC_CREDITS -> "appcoins_credits"
      MERGED_APPC -> "merged_appcoins"
      LOCAL_PAYMENTS -> "local_payments"
      EARN_APPC -> "earn_appcoins"
      CARRIER_BILLING -> "carrier_billing"
      ERROR -> ""
    }
  }

  private fun mapCancellation(): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_USER_CANCELED)
    return bundle
  }

  private fun mapFinishedPurchase(purchase: Purchase, itemAlreadyOwned: Boolean): Bundle {
    val bundle = Bundle()
    bundle.putString(AppcoinsBillingBinder.INAPP_PURCHASE_DATA, purchase.signature.message)
    bundle.putString(AppcoinsBillingBinder.INAPP_DATA_SIGNATURE, purchase.signature.value)
    bundle.putString(AppcoinsBillingBinder.INAPP_PURCHASE_ID, purchase.uid)
    if (itemAlreadyOwned) {
      bundle.putInt(
        AppcoinsBillingBinder.RESPONSE_CODE,
        AppcoinsBillingBinder.RESULT_ITEM_ALREADY_OWNED
      )
    }
    return bundle
  }

  private fun hasPreSelectedPaymentMethod(): Boolean {
    return pref.contains(PRE_SELECTED_PAYMENT_METHOD_KEY)
  }

  private fun hasAuthenticationPermission(): Boolean {
    return pref.getBoolean(AUTHENTICATION_PERMISSION, false)
  }

  private fun isWalletBlocked(): Single<Boolean> {
    return getWalletInfo(null, cached = false, updateFiat = false)
      .map { walletInfo -> walletInfo.blocked }
      .onErrorReturn { false }
      .delay(1, TimeUnit.SECONDS)
  }

  private fun isAbleToSubscribe(
    packageName: String, skuId: String,
    networkThread: Scheduler
  ): Single<SubscriptionStatus> {
    return getSubscriptionToken(packageName, skuId, networkThread)
      .map { SubscriptionStatus(true) }
      .onErrorReturn {
        val errorInfo = map(it)
        val isAlreadySubscribed = errorInfo.errorType == ErrorInfo.ErrorType.SUB_ALREADY_OWNED
        SubscriptionStatus(false, isAlreadySubscribed)
      }
  }

  private fun getSubscriptionToken(
    packageName: String, skuId: String,
    networkThread: Scheduler
  ): Single<String> {
    return getAndSignCurrentWalletAddress()
      .observeOn(networkThread)
      .flatMap {
        getSubscriptionToken(packageName, skuId, it.address, it.signedAddress)
      }
  }

  private fun getAndSignCurrentWalletAddress(): Single<WalletAddressModel> = find()
    .flatMap { wallet ->
      getPrivateKey(wallet)
        .map { sign(normalizer.normalize(Keys.toChecksumAddress(wallet.address)), it) }
        .map { WalletAddressModel(wallet.address, it) }
    }

  private fun find(): Single<Wallet> {
    return getDefaultWallet()
      .subscribeOn(Schedulers.io())
      .onErrorResumeNext {
        fetchWallets()
          .filter { wallets -> wallets.isNotEmpty() }
          .map { wallets: Array<Wallet> -> wallets[0] }
          .flatMapCompletable { wallet: Wallet -> setDefaultWallet(wallet.address) }
          .andThen(getDefaultWallet())
      }
  }

  private fun getPrivateKey(wallet: Wallet): Single<ECKey> =
    if (stringECKeyPair?.first?.equals(wallet.address, true) == true) {
      Single.just(stringECKeyPair!!.second)
    } else {
      passwordStore.getPassword(wallet.address)
        .flatMap { password ->
          accountKeystoreService.exportAccount(wallet.address, password, password)
            .map { json ->
              ECKey.fromPrivate(WalletUtils.loadCredentials(password, json).ecKeyPair.privateKey)
            }
        }
        .doOnSuccess { ecKey -> stringECKeyPair = android.util.Pair(wallet.address, ecKey) }
    }

  @Throws(Exception::class)
  private fun sign(plainText: String, ecKey: ECKey): String =
    ecKey.sign(HashUtil.sha3(plainText.toByteArray())).toHex()

  private fun getSubscriptionToken(
    domain: String,
    skuId: String,
    walletAddress: String,
    walletSignature: String
  ): Single<String> =
    subsApi.getSkuSubscriptionToken(domain, skuId, null, walletAddress, walletSignature)

  private fun map(throwable: Throwable): ErrorInfo {
    return when {
      throwable.isNoNetworkException() -> ErrorInfo(ErrorInfo.ErrorType.NO_NETWORK, null, null)
      throwable is HttpException -> mapHttpException(throwable)
      else -> ErrorInfo(ErrorInfo.ErrorType.UNKNOWN, null, throwable.message)
    }
  }

  private fun mapHttpException(exception: HttpException): ErrorInfo {
    return if (exception.code() == FORBIDDEN_CODE) {
      try {
        val messageInfo = gson.fromJson(exception.getMessage(), ResponseErrorBaseBody::class.java)
        when (messageInfo.code) {
          "NotAllowed" -> ErrorInfo(ErrorInfo.ErrorType.SUB_ALREADY_OWNED, null, null)
          "Authorization.Forbidden" -> ErrorInfo(ErrorInfo.ErrorType.BLOCKED, null, null)
          else -> ErrorInfo(ErrorInfo.ErrorType.UNKNOWN, exception.code(), messageInfo.text)
        }
      } catch (e: Exception) {
        ErrorInfo(ErrorInfo.ErrorType.UNKNOWN, null, null)
      }
    } else {
      val message = exception.getMessage()
      ErrorInfo(ErrorInfo.ErrorType.UNKNOWN, exception.code(), message)
    }
  }

  private fun getSkuTransaction(
    merchantName: String,
    sku: String?,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Transaction> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuTransaction(merchantName, sku, it.address, it.signedAddress, type)
      }
  }

  private fun getSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Transaction> = getRemoteSkuTransaction(
    packageName,
    skuId,
    walletAddress,
    walletSignature,
    type
  )
    .flatMap {
      if (it.items.isNotEmpty()) {
        return@flatMap Single.just(it.items[0])
      }
      return@flatMap Single.just(Transaction.notFound())
    }

  private fun getRemoteSkuTransaction(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<TransactionsResponse> =
    brokerBdsApi.getSkuTransaction(
      walletAddress,
      walletSignature,
      0,
      type,
      1,
      "latest",
      false,
      skuId,
      packageName
    )

  private fun checkTransactionStateFromTransactionId(uid: String): Observable<PendingTransaction> {
    return Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
      .timeInterval()
      .switchMap { scan ->
        getAppcoinsTransaction(uid, Schedulers.io())
          .map { (uid1, status): Transaction ->
            PendingTransaction(
              uid1,
              status === Transaction.Status.PROCESSING
            )
          }
          .toObservable()
      }
      .takeUntil { pendingTransaction: PendingTransaction -> !pendingTransaction.isPending }
  }

  private fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap { getAppcoinsTransaction(uid, it.address, it.signedAddress) }
  }

  private fun getAppcoinsTransaction(
    uid: String,
    address: String,
    signedContent: String
  ): Single<Transaction> =
    brokerBdsApi.getAppcoinsTransaction(uid, address, signedContent)

  private fun getCurrentPaymentStep(
    packageName: String,
    transactionBuilder: TransactionBuilder
  ): Single<CurrentPaymentStep> {
    return Single.zip(
      getTransaction(packageName, transactionBuilder.skuId, transactionBuilder.type),
      isAppcoinsPaymentReady(transactionBuilder)
    ) { transaction, isBuyReady -> map(transaction, isBuyReady) }
  }

  private fun getTransaction(
    packageName: String,
    productName: String,
    type: String
  ): Single<Transaction> {
    return Single.defer {
      val billingType = valueOfInsensitive(type)
      getSkuTransaction(
        packageName,
        productName,
        Schedulers.io(),
        billingType
      )
    }
  }

  private fun isAppcoinsPaymentReady(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return fetch(true)
      .doOnSuccess(Consumer { gasSettings: GasSettings ->
        transactionBuilder.gasSettings(
          GasSettings(
            gasSettings.gasPrice,
            paymentGasLimit
          )
        )
      })
      .flatMap { hasBalanceToBuy(transactionBuilder) }
  }

  private fun fetch(forTokenTransfer: Boolean): Single<GasSettings> {
    return gasSettingsRepository.getGasSettings(forTokenTransfer)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
  }

  private fun hasBalanceToBuy(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return getBalanceState(transactionBuilder).flatMap { balanceState: BalanceState ->
      if (balanceState == BalanceState.OK) {
        return@flatMap Single.just(true)
      } else {
        return@flatMap Single.just(false)
      }
    }
  }

  private fun getBalanceState(transactionBuilder: TransactionBuilder): Single<BalanceState> {
    val transactionGasLimit = transactionBuilder.gasSettings().gasLimit
    val gasSettings = transactionBuilder.gasSettings()
    return if (transactionBuilder.shouldSendToken()) {
      checkTokenAddress(transactionBuilder, true).flatMap {
        Single.zip(
          hasEnoughForTransfer(transactionBuilder),
          hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit))
        ) { enoughEther, enoughTokens ->
          mapToState(enoughEther, enoughTokens)
        }
      }
    } else {
      Single.zip(
        hasEnoughForTransfer(transactionBuilder),
        hasEnoughForFee(gasSettings.gasPrice.multiply(transactionGasLimit))
      ) { enoughEther, enoughTokens ->
        mapToState(enoughEther, enoughTokens)
      }
    }
  }

  private fun <T> checkTokenAddress(
    transactionBuilder: TransactionBuilder,
    successValue: T
  ): Single<T> {
    return getDefaultToken()
      .flatMap { tokenInfo: TokenInfo ->
        if (tokenInfo.address.equals(transactionBuilder.contractAddress(), ignoreCase = true)) {
          return@flatMap Single.just(successValue)
        } else {
          return@flatMap Single.error<T>(UnknownTokenException())
        }
      }
  }

  private fun getDefaultToken(): Single<TokenInfo> {
    return Single.fromCallable { defaultTokenRepository.tokenInfo }
  }

  private fun hasEnoughForTransfer(transactionBuilder: TransactionBuilder): Single<Boolean> {
    return if (transactionBuilder.shouldSendToken()) {
      hasEnoughBalance(
        address = null,
        value = transactionBuilder.amount(),
        unit = Convert.Unit.ETHER,
        balanceType = BalanceType.APPC
      )
    } else {
      hasEnoughBalance(
        address = null,
        value = transactionBuilder.amount(),
        unit = Convert.Unit.WEI,
        balanceType = BalanceType.ETH
      )
    }
  }

  private fun hasEnoughForFee(fee: BigDecimal): Single<Boolean> {
    return hasEnoughBalance(
      address = null,
      value = fee,
      unit = Convert.Unit.WEI,
      balanceType = BalanceType.ETH
    )
  }

  private fun hasEnoughBalance(
    address: String?,
    value: BigDecimal,
    unit: Convert.Unit,
    balanceType: BalanceType
  ): Single<Boolean> {
    return getWalletInfo(address, cached = false, updateFiat = false)
      .flatMap { walletInfo ->
        val scaledValue = Convert.toWei(value, unit)
        val scaledCredits = Convert.toWei(
          walletInfo.walletBalance.creditsBalance.token.amount,
          Convert.Unit.ETHER
        )
        val scaledAppc =
          Convert.toWei(walletInfo.walletBalance.appcBalance.token.amount, Convert.Unit.ETHER)
        val scaledEth =
          Convert.toWei(walletInfo.walletBalance.ethBalance.token.amount, Convert.Unit.ETHER)
        return@flatMap when (balanceType) {
          BalanceType.APPC_C -> Single.just(scaledCredits >= scaledValue)
          BalanceType.APPC -> Single.just(scaledAppc >= scaledValue)
          BalanceType.ETH -> Single.just(scaledEth >= scaledValue)
        }
      }
  }

  private fun mapToState(
    enoughEther: Boolean,
    enoughTokens: Boolean
  ): BalanceState {
    return if (enoughTokens && enoughEther) {
      BalanceState.OK
    } else if (!enoughTokens && !enoughEther) {
      BalanceState.NO_ETHER_NO_TOKEN
    } else if (enoughEther) {
      BalanceState.NO_TOKEN
    } else {
      BalanceState.NO_ETHER
    }
  }

  @Throws(UnknownServiceException::class)
  private fun map(transaction: Transaction, isBuyReady: Boolean): CurrentPaymentStep {
    return when (transaction.status) {
      Transaction.Status.PENDING,
      Transaction.Status.PENDING_SERVICE_AUTHORIZATION,
      Transaction.Status.PROCESSING -> when (transaction.gateway?.name) {
        Gateway.Name.appcoins -> CurrentPaymentStep.PAUSED_ON_CHAIN
        Gateway.Name.adyen_v2 -> if (transaction.status == Transaction.Status.PROCESSING) {
          CurrentPaymentStep.PAUSED_CC_PAYMENT
        } else {
          if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
        }
        Gateway.Name.myappcoins -> CurrentPaymentStep.PAUSED_LOCAL_PAYMENT
        Gateway.Name.appcoins_credits -> CurrentPaymentStep.PAUSED_CREDITS
        Gateway.Name.unknown -> throw UnknownServiceException("Unknown gateway")
        else -> throw UnknownServiceException("Unknown gateway")
      }
      Transaction.Status.COMPLETED,
      Transaction.Status.PENDING_USER_PAYMENT,
      Transaction.Status.FAILED,
      Transaction.Status.CANCELED,
      Transaction.Status.INVALID_TRANSACTION -> if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
      else -> if (isBuyReady) CurrentPaymentStep.READY else CurrentPaymentStep.NO_FUNDS
    }
  }

  private fun resume(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String?,
    isBds: Boolean,
    type: String,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return if (isBds) {
      resume(
        uri = uri,
        transactionType = transactionType,
        packageName = packageName,
        productName = productName,
        developerPayload = developerPayload,
        type = type,
        transactionBuilder = transactionBuilder
      )
    } else {
      Completable.error(UnsupportedOperationException("Asf doesn't support resume."))
    }
  }

  private fun resume(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    developerPayload: String?,
    type: String,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return getTransaction(packageName, productName, type)
      .doOnSuccess { transaction -> saveTransactionId(transaction) }
      .flatMapCompletable { (uid): Transaction ->
        resumePayment(
          uri = uri,
          transactionType = transactionType,
          packageName = packageName,
          productName = productName,
          approveKey = uid,
          developerPayload = developerPayload,
          transactionBuilder = transactionBuilder
        )
      }
  }

  private fun saveTransactionId(transaction: Transaction) {
    transactionFromApprove[transaction.uid] = transaction
  }

  private fun resumePayment(
    uri: String,
    transactionType: AsfInAppPurchaseInteractor.TransactionType,
    packageName: String,
    productName: String,
    approveKey: String,
    developerPayload: String?,
    transactionBuilder: TransactionBuilder
  ): Completable {
    return if (transactionType == AsfInAppPurchaseInteractor.TransactionType.NORMAL) {
      buildPaymentTransaction(
        uri,
        packageName,
        productName,
        developerPayload!!,
        transactionBuilder.amount()
      ).flatMapCompletable { paymentTransaction ->
        getSkuTransaction(
          paymentTransaction,
          packageName,
          approveKey
        )
      }
    } else Completable.error(
      java.lang.UnsupportedOperationException(
        "Transaction type $transactionType not supported"
      )
    )
  }

  private fun getSkuTransaction(
    paymentTransaction: PaymentTransaction,
    packageName: String,
    approveKey: String
  ): Completable {
    val billingType = valueOfInsensitive(paymentTransaction.transactionBuilder.type)
    return getSkuTransaction(
      merchantName = packageName,
      sku = paymentTransaction.transactionBuilder.skuId,
      scheduler = Schedulers.io(),
      type = billingType
    )
      .flatMapCompletable { transaction ->
        resumePayment(
          approveKey,
          paymentTransaction,
          transaction
        )
      }
  }

  private fun buildPaymentTransaction(
    uri: String,
    packageName: String,
    productName: String,
    developerPayload: String,
    amount: BigDecimal
  ): Single<PaymentTransaction> {
    return Single.zip(
      parseTransaction(uri).observeOn(Schedulers.io()),
      find().observeOn(Schedulers.io())
    ) { transaction: TransactionBuilder, wallet: Wallet ->
      transaction.fromAddress(
        wallet.address
      )
    }
      .flatMap { transactionBuilder: TransactionBuilder ->
        fetch(true)
          .map { gasSettings: GasSettings ->
            transactionBuilder.gasSettings(GasSettings(gasSettings.gasPrice, paymentGasLimit))
            transactionBuilder.amount(amount)
          }
      }
      .map { transactionBuilder: TransactionBuilder ->
        PaymentTransaction(
          uri, transactionBuilder, packageName,
          productName, transactionBuilder.skuId, developerPayload,
          transactionBuilder.callbackUrl, transactionBuilder.orderReference
        )
      }
  }

  private fun parseTransaction(data: String) = if (data.isEthereumURLString()) {
    Single.just(parseERC681(data))
      .map { erc681 -> eipTransactionParser.buildTransaction(erc681) }
  } else {
    if (Uri.parse(data).isOneStepURLString()) {
      Single.just(parseOneStep(Uri.parse(data)))
        .flatMap { oneStepUri -> oneStepTransactionParser.buildTransaction(oneStepUri, data) }
    } else {
      Single.error(RuntimeException("is not an supported URI"))
    }
  }

  private fun resumePayment(
    approveKey: String, paymentTransaction: PaymentTransaction,
    transaction: Transaction
  ): Completable {
    return when (transaction.status) {
      Transaction.Status.PENDING_SERVICE_AUTHORIZATION -> resume(
        paymentTransaction.uri,
        PaymentTransaction(paymentTransaction, PaymentTransaction.PaymentState.APPROVED, approveKey)
      )
      Transaction.Status.PROCESSING -> trackTransaction(
        key = paymentTransaction.uri,
        packageName = paymentTransaction.packageName,
        skuId = paymentTransaction.transactionBuilder.skuId,
        uid = transaction.uid,
        purchaseUid = null,
        orderReference = transaction.orderReference
      )
      Transaction.Status.PENDING,
      Transaction.Status.COMPLETED,
      Transaction.Status.INVALID_TRANSACTION,
      Transaction.Status.FAILED,
      Transaction.Status.CANCELED -> Completable.error(
        UnsupportedOperationException("Cannot resume from " + transaction.status + " state")
      )
      else -> Completable.error(
        UnsupportedOperationException("Cannot resume from " + transaction.status + " state")
      )
    }
  }

  private fun resume(key: String, paymentTransaction: PaymentTransaction): Completable {
    return checkFunds(key, paymentTransaction, buy(key, paymentTransaction), true)
  }

  private fun buy(key: String, paymentTransaction: PaymentTransaction): Completable {
    val transactionBuilder = paymentTransaction.transactionBuilder
    val cachedTransaction = getTransactionFromUid(key)
    val storeAddress: String = getStoreAddress(cachedTransaction)
    val oemAddress: String = getOemAddress(cachedTransaction)
    return Single.zip(
      countryCodeProvider.countryCode, getDefaultToken()
    ) { countryCode, tokenInfo ->
      transactionBuilder.appcoinsData(
        getBuyData(
          transactionBuilder = transactionBuilder,
          tokenInfo = tokenInfo,
          packageName = paymentTransaction.packageName,
          countryCode = countryCode,
          storeAddress = storeAddress,
          oemAddress = oemAddress
        )
      )
    }
      .map { transaction -> updateTransactionBuilderData(paymentTransaction, transaction) }
      .flatMap { payment: PaymentTransaction? ->
        bdsValidate(paymentTransaction)
          .map<PaymentTransaction> { payment }
      }
      .flatMapCompletable { payment: PaymentTransaction ->
        sendBuyTransaction(key, payment.transactionBuilder, true)
      }
  }

  private fun getTransactionFromUid(uid: String) = transactionFromApprove[uid]

  private fun getStoreAddress(
    transaction: Transaction?
  ): String {
    var tmpStoreAddress: String? = null
    if (transaction?.wallets != null) {
      tmpStoreAddress = transaction.wallets!!.store
    }
    return getStoreAddress(tmpStoreAddress)
  }

  private fun getStoreAddress(suggestedStoreAddress: String?): String {
    return suggestedStoreAddress?.let { suggestedStoreAddress } ?: defaultStoreAddress
  }

  private fun getOemAddress(
    transaction: Transaction?
  ): String {
    var tmpOemAddress: String? = null
    if (transaction?.wallets != null) {
      tmpOemAddress = transaction.wallets!!.oem
    }
    return getOemAddress(tmpOemAddress)
  }

  private fun getOemAddress(suggestedOemAddress: String?): String {
    return suggestedOemAddress?.let { suggestedOemAddress } ?: defaultOemAddress
  }

  private fun getBuyData(
    transactionBuilder: TransactionBuilder,
    tokenInfo: TokenInfo,
    packageName: String,
    countryCode: String,
    storeAddress: String,
    oemAddress: String
  ): ByteArray? {
    return TokenRepository.buyData(
      transactionBuilder.toAddress(),
      storeAddress,
      oemAddress,
      transactionBuilder.skuId,
      transactionBuilder.amount().multiply(BigDecimal("10").pow(transactionBuilder.decimals())),
      tokenInfo.address,
      packageName,
      convertCountryCode(countryCode)
    )
  }

  private fun updateTransactionBuilderData(
    paymentTransaction: PaymentTransaction,
    transaction: TransactionBuilder
  ): PaymentTransaction {
    return PaymentTransaction(
      paymentTransaction.uri, transaction,
      paymentTransaction.state, paymentTransaction.approveHash,
      paymentTransaction.buyHash, paymentTransaction.packageName,
      paymentTransaction.productName, paymentTransaction.productId,
      paymentTransaction.developerPayload, paymentTransaction.callbackUrl,
      paymentTransaction.orderReference, paymentTransaction.errorCode,
      paymentTransaction.errorMessage
    )
  }

  private fun trackTransaction(
    key: String,
    packageName: String,
    skuId: String,
    uid: String,
    purchaseUid: String?,
    orderReference: String?
  ): Completable {
    return bdsTrackTransactionCache.save(
      key,
      BdsTransactionService.BdsTransaction(
        uid,
        purchaseUid,
        key,
        packageName,
        skuId,
        orderReference
      )
    )
  }

  private fun checkFunds(
    key: String,
    paymentTransaction: PaymentTransaction,
    action: Completable,
    isBds: Boolean,
  ): Completable {
    return Completable.fromAction {
      if (isBds) {
        bdsPaymentTransactionCache.saveSync(key, paymentTransaction)
      } else {
        asfPaymentTransactionCache.saveSync(key, paymentTransaction)
      }
    }
      .andThen(getBalanceState(paymentTransaction.transactionBuilder)
        .observeOn(Schedulers.io())
        .flatMapCompletable { balance ->
          when (balance) {
            BalanceState.NO_TOKEN ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentTransaction.PaymentState.NO_TOKENS
                )
              )
            BalanceState.NO_ETHER ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentTransaction.PaymentState.NO_ETHER
                )
              )
            BalanceState.NO_ETHER_NO_TOKEN ->
              return@flatMapCompletable asfPaymentTransactionCache.save(
                key, PaymentTransaction(
                  paymentTransaction,
                  PaymentTransaction.PaymentState.NO_FUNDS
                )
              )
            BalanceState.OK -> return@flatMapCompletable action
            else -> return@flatMapCompletable action
          }
        }
      )
      .onErrorResumeNext { throwable ->
        val (paymentState, errorCode, errorMessage) = mapCF(throwable)
        asfPaymentTransactionCache.save(
          paymentTransaction.uri,
          PaymentTransaction(paymentTransaction, paymentState, errorCode, errorMessage)
        )
      }
  }

  private fun bdsValidate(paymentTransaction: PaymentTransaction): Single<Transaction> {
    val packageName = paymentTransaction.packageName
    val productName = paymentTransaction.transactionBuilder
      .skuId
    val getTransactionHash: Single<String> = getDefaultToken()
      .flatMap {
        computeBuyTransactionHash(paymentTransaction.transactionBuilder)
      }
    val attributionEntity = getAttributionEntity(packageName)
    return Single.zip(
      getTransactionHash, attributionEntity
    ) { hash: String?, (oemId, domain): AttributionEntity ->
      PaymentProof(
        "appcoins",
        paymentTransaction.approveHash!!,
        hash!!, productName, packageName, oemId, domain
      )
    }
      .flatMap { paymentProof ->
        processPurchaseProof(paymentProof)
      }
  }

  private fun sendBuyTransaction(
    key: String,
    transactionBuilder: TransactionBuilder,
    isBds: Boolean
  ): Completable {
    return if (isBds) {
      bdsBuyWatchedTransactionCache
    } else {
      asfBuyWatchedTransactionCache
    }.save(
      key,
      com.asfoundation.wallet.repository.Transaction(
        key,
        com.asfoundation.wallet.repository.Transaction.Status.PENDING,
        transactionBuilder
      )
    )
  }

  private fun computeBuyTransactionHash(transactionBuilder: TransactionBuilder): Single<String> {
    return passwordStore.getPassword(transactionBuilder.fromAddress())
      .flatMap { password -> computeBuyTransactionHash(transactionBuilder, password) }
  }

  private fun computeBuyTransactionHash(
    transactionBuilder: TransactionBuilder,
    password: String
  ): Single<String> {
    return getDefaultToken()
      .observeOn(Schedulers.io())
      .flatMap {
        createRawTransaction(
          transactionBuilder,
          password,
          transactionBuilder.appcoinsData(),
          transactionBuilder.iabContract,
          BigDecimal.ZERO,
          nonceObtainer.getNonce(
            Address(transactionBuilder.fromAddress()),
            getChainId(transactionBuilder)
          )
        )
      }
      .map { signedTx -> calculateHashFromSigned(signedTx) }
  }

  private fun createRawTransaction(
    transactionBuilder: TransactionBuilder,
    password: String,
    data: ByteArray,
    toAddress: String,
    amount: BigDecimal,
    nonce: BigInteger
  ): Single<ByteArray> {
    return Single.just(nonce)
      .flatMap {
        if (transactionBuilder.chainId != TransactionBuilder.NO_CHAIN_ID
          && transactionBuilder.chainId != defaultNetwork.chainId.toLong()
        ) {
          var requestedNetwork = "unknown"
          if (transactionBuilder.chainId == 1L) {
            requestedNetwork = C.ETHEREUM_NETWORK_NAME
          } else if (transactionBuilder.chainId == 3L) {
            requestedNetwork = C.ROPSTEN_NETWORK_NAME
          }
          return@flatMap Single.error<ByteArray>(
            WrongNetworkException(
              """
              Default network is different from the intended on transaction
              Current network: ${defaultNetwork.name}
              Requested: $requestedNetwork
              """.trimIndent()
            )
          )
        }
        accountKeystoreService.signTransaction(
          transactionBuilder.fromAddress(), password,
          toAddress, amount, transactionBuilder.gasSettings().gasPrice,
          transactionBuilder.gasSettings().gasLimit, nonce.toLong(), data,
          defaultNetwork.chainId.toLong()
        )
      }
  }

  private fun getChainId(transactionBuilder: TransactionBuilder): Long {
    return if (transactionBuilder.chainId == TransactionBuilder.NO_CHAIN_ID) {
      defaultNetwork.chainId.toLong()
    } else {
      transactionBuilder.chainId
    }
  }

  private fun calculateHashFromSigned(signedTx: ByteArray): String? {
    val hash = Hash.sha3(signedTx)
    return Numeric.toHexString(hash)
  }

  private fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(
      installerService.getInstallerPackageName(packageName),
      oemIdExtractorService.extractOemId(packageName)
    ) { installerPackage, oemId ->
      AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
    }
  }

  private fun processPurchaseProof(paymentProof: PaymentProof): Single<Transaction> =
    transactionFromApprove[paymentProof.approveProof]?.let { transaction ->
      registerPaymentProof(
        transaction.uid,
        paymentProof.paymentProof,
        paymentProof.paymentType
      ).andThen(Single.just(transaction))
    } ?: Single.error(IllegalArgumentException("No payment id for {${paymentProof.approveProof}}"))

  private fun registerPaymentProof(
    paymentId: String,
    paymentProof: String,
    paymentType: String
  ): Completable =
    getWalletAddress().observeOn(Schedulers.io())
      .flatMapCompletable { walletAddress ->
        signContent(walletAddress).observeOn(Schedulers.io())
          .flatMapCompletable { signedData ->
            registerPaymentProof(
              paymentId = paymentId,
              paymentType = paymentType,
              walletAddress = walletAddress,
              walletSignature = signedData,
              paymentProof = paymentProof
            )
          }
      }
      .andThen(Completable.fromAction { transactionIdsFromBuy[paymentProof] = paymentId })

  private fun getWalletAddress(): Single<String> = find()
    .map { Keys.toChecksumAddress(it.address) }

  private fun signContent(content: String): Single<String> = find()
    .flatMap { wallet -> getPrivateKey(wallet).map { sign(normalizer.normalize(content), it) } }

  private fun registerPaymentProof(
    paymentId: String,
    paymentType: String,
    walletAddress: String,
    walletSignature: String,
    paymentProof: String
  ): Completable =
    brokerBdsApi.patchTransaction(
      paymentType,
      paymentId,
      walletAddress,
      walletSignature,
      paymentProof
    )

  private fun mapCF(throwable: Throwable): PaymentError {
    throwable.printStackTrace()
    return when (throwable) {
      is HttpException -> mapHttpExceptionCF(throwable)
      is UnknownHostException -> PaymentError(PaymentTransaction.PaymentState.NO_INTERNET)
      is WrongNetworkException -> PaymentError(PaymentTransaction.PaymentState.WRONG_NETWORK)
      is TransactionNotFoundException -> PaymentError(PaymentTransaction.PaymentState.ERROR)
      is UnknownTokenException -> PaymentError(PaymentTransaction.PaymentState.UNKNOWN_TOKEN)
      is TransactionException -> mapTransactionException(throwable)
      else -> PaymentError(PaymentTransaction.PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun mapHttpExceptionCF(exception: HttpException): PaymentError {
    return if (exception.code() == FORBIDDEN_CODE) {
      val messageInfo = gson.fromJson(exception.getMessage(), ResponseErrorBaseBody::class.java)
      when (messageInfo.code) {
        "NotAllowed" -> PaymentError(PaymentTransaction.PaymentState.SUB_ALREADY_OWNED)
        "Authorization.Forbidden" -> PaymentError(PaymentTransaction.PaymentState.FORBIDDEN)
        else -> PaymentError(PaymentTransaction.PaymentState.ERROR)
      }
    } else {
      val message = exception.getMessage()
      PaymentError(PaymentTransaction.PaymentState.ERROR, exception.code(), message)
    }
  }

  private fun mapTransactionException(throwable: Throwable): PaymentError {
    return when (throwable.message) {
      INSUFFICIENT_ERROR_MESSAGE -> PaymentError(PaymentTransaction.PaymentState.NO_FUNDS)
      NONCE_TOO_LOW_ERROR_MESSAGE -> PaymentError(PaymentTransaction.PaymentState.NONCE_ERROR)
      else -> PaymentError(PaymentTransaction.PaymentState.ERROR, null, throwable.message)
    }
  }

  private fun getSkuPurchase(
    appPackage: String, skuId: String?, purchaseUid: String, type: String,
    orderReference: String?,
    hash: String?, networkThread: Scheduler
  ): Single<PurchaseBundleModel> {
    return getCompletedPurchaseBundle(
      type, appPackage, skuId, purchaseUid,
      orderReference, hash, networkThread
    )
  }

  private fun getCompletedPurchaseBundle(
    type: String,
    merchantName: String, sku: String?, purchaseUid: String,
    orderReference: String?, hash: String?, scheduler: Scheduler
  ): Single<PurchaseBundleModel> {
    val billingType = valueOfInsensitive(type)
    return if (isManagedTransaction(billingType) && sku != null) {
      getSkuPurchase(merchantName, sku, purchaseUid, scheduler, billingType)
        .map { purchase: Purchase ->
          PurchaseBundleModel(mapPurchase(purchase, orderReference), purchase.renewal)
        }
    } else {
      Single.just(PurchaseBundleModel(successBundle(hash), null))
    }
  }

  private fun isManagedTransaction(type: BillingSupportedType): Boolean {
    return type === BillingSupportedType.INAPP || type === BillingSupportedType.INAPP_SUBSCRIPTION
  }

  private fun getSkuPurchase(
    merchantName: String,
    sku: String,
    purchaseUid: String,
    scheduler: Scheduler,
    type: BillingSupportedType
  ): Single<Purchase> {
    return getAndSignCurrentWalletAddress()
      .observeOn(scheduler)
      .flatMap {
        getSkuPurchase(
          packageName = merchantName,
          skuId = sku,
          purchaseUid = purchaseUid,
          walletAddress = it.address,
          walletSignature = it.signedAddress,
          type = type
        )
      }
  }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<Purchase> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      getSkuPurchase(
        packageName = packageName,
        skuId = skuId,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
    } else {
      getSkuPurchaseSubs(
        packageName = packageName,
        purchaseUid = purchaseUid,
        walletAddress = walletAddress,
        walletSignature = walletSignature
      )
    }

  private fun getSkuPurchase(
    packageName: String,
    skuId: String?,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    inappBdsApi.getPurchases(
      packageName,
      walletAddress,
      walletSignature,
      type = BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT),
      sku = skuId
    )
      .map {
        if (it.items.isEmpty()) {
          throw HttpException(
            Response.error<GetPurchasesResponse>(
              404,
              "{}".toResponseBody("application/json".toMediaType())
            )
          )
        }
        map(packageName, it)[0]
      }

  private fun map(packageName: String, purchasesResponse: GetPurchasesResponse): List<Purchase> =
    purchasesResponse.items.map { map(packageName, it) }

  private fun map(packageName: String, inAppPurchaseResponse: InappPurchaseResponse): Purchase =
    Purchase(
      inAppPurchaseResponse.uid,
      RemoteProduct(inAppPurchaseResponse.sku),
      mapPurchaseState(inAppPurchaseResponse.state),
      false,
      null,
      Package(packageName),
      Signature(
        inAppPurchaseResponse.verification.signature,
        inAppPurchaseResponse.verification.data
      )
    )

  private fun mapPurchaseState(state: PurchaseState): State {
    return when (state) {
      PurchaseState.CONSUMED -> State.CONSUMED
      PurchaseState.PENDING -> State.PENDING
      PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  private fun getSkuPurchaseSubs(
    packageName: String,
    purchaseUid: String,
    walletAddress: String,
    walletSignature: String
  ): Single<Purchase> =
    subsApi.getPurchase(packageName, purchaseUid, walletAddress, walletSignature)
      .map { map(packageName, it) }

  private fun map(
    packageName: String,
    subscriptionPurchaseResponse: SubscriptionPurchaseResponse
  ): Purchase {
    return Purchase(
      subscriptionPurchaseResponse.uid,
      RemoteProduct(subscriptionPurchaseResponse.sku),
      mapPurchaseState(subscriptionPurchaseResponse.state),
      subscriptionPurchaseResponse.autoRenewing,
      mapRenewalDate(subscriptionPurchaseResponse.renewal),
      Package(packageName), Signature(
        subscriptionPurchaseResponse.verification.signature,
        subscriptionPurchaseResponse.verification.data
      )
    )
  }

  private fun mapPurchaseState(state: com.appcoins.wallet.bdsbilling.PurchaseState): State {
    return when (state) {
      com.appcoins.wallet.bdsbilling.PurchaseState.CONSUMED -> State.CONSUMED
      com.appcoins.wallet.bdsbilling.PurchaseState.PENDING -> State.PENDING
      com.appcoins.wallet.bdsbilling.PurchaseState.ACKNOWLEDGED -> State.ACKNOWLEDGED
    }
  }

  private fun mapRenewalDate(renewal: String?): Date? {
    return if (renewal == null) null
    else {
      val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
      dateFormat.parse(renewal)
    }
  }

  private fun mapPurchase(purchase: Purchase, orderReference: String?): Bundle {
    return mapPurchase(
      purchaseId = purchase.uid,
      signature = purchase.signature.value,
      signatureData = purchase.signature.message,
      orderReference = orderReference
    )
  }

  private fun mapPurchase(
    purchaseId: String,
    signature: String,
    signatureData: String,
    orderReference: String?
  ): Bundle {
    val intent = Bundle()
    intent.putString(AppcoinsBillingBinder.INAPP_PURCHASE_ID, purchaseId)
    intent.putString(AppcoinsBillingBinder.INAPP_PURCHASE_DATA, signatureData)
    intent.putString(AppcoinsBillingBinder.INAPP_DATA_SIGNATURE, signature)
    intent.putString(AppcoinsBillingBinder.INAPP_ORDER_REFERENCE, orderReference)
    intent.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    return intent
  }

  private fun successBundle(uid: String?): Bundle {
    val bundle = Bundle()
    bundle.putInt(AppcoinsBillingBinder.RESPONSE_CODE, AppcoinsBillingBinder.RESULT_OK)
    bundle.putString(TRANSACTION_HASH, uid)
    return bundle
  }

  private fun getEarningBonus(
    packageName: String,
    amount: BigDecimal
  ): Single<ForecastBonusAndLevel> {
    return getCurrentPromoCode()
      .flatMap { getEarningBonus(packageName, amount, it.code) }
  }

  private fun getCurrentPromoCode(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .firstOrError()
  }

  private fun getEarningBonus(
    packageName: String, amount: BigDecimal,
    promoCodeString: String?
  ): Single<ForecastBonusAndLevel> {
    return getCurrentPromoCode()
      .flatMap { promoCode ->
        findDefault()
          .flatMap { wallet: Wallet ->
            Single.zip(
              getEarningBonus(wallet.address, packageName, amount, promoCodeString),
              localCurrency,
              getUserBonusAndLevel(wallet.address, promoCode.code)
            ) { appcBonusValue, localCurrency, userBonusAndLevel ->
              map(appcBonusValue, localCurrency, userBonusAndLevel, amount)
            }
          }
          .doOnSuccess { isBonusActiveAndValid = isBonusActiveAndValid(it) }
      }
  }

  private fun isBonusActiveAndValid(forecastBonus: ForecastBonusAndLevel): Boolean {
    return forecastBonus.status == ForecastBonus.Status.ACTIVE && forecastBonus.amount > BigDecimal.ZERO
  }

  private fun map(
    forecastBonus: ForecastBonus, fiatValue: FiatValue,
    forecastBonusAndLevel: ForecastBonusAndLevel,
    amount: BigDecimal
  ): ForecastBonusAndLevel {
    val status = getBonusStatus(forecastBonus, forecastBonusAndLevel)
    var bonus = forecastBonus.amount.multiply(fiatValue.amount)

    if (amount.multiply(fiatValue.amount) >= forecastBonusAndLevel.minAmount) {
      bonus = bonus.add(forecastBonusAndLevel.amount)
    }
    return ForecastBonusAndLevel(
      status, bonus, fiatValue.symbol,
      level = forecastBonusAndLevel.level
    )
  }

  private fun getBonusStatus(
    forecastBonus: ForecastBonus,
    userBonusAndLevel: ForecastBonusAndLevel
  ): ForecastBonus.Status {
    return if (forecastBonus.status == ForecastBonus.Status.ACTIVE || userBonusAndLevel.status == ForecastBonus.Status.ACTIVE) {
      ForecastBonus.Status.ACTIVE
    } else {
      ForecastBonus.Status.INACTIVE
    }
  }

  private fun findDefault(): Single<Wallet> {
    return getDefaultWallet()
      .subscribeOn(Schedulers.io())
      .onErrorResumeNext {
        fetchWallets()
          .filter { wallets -> wallets.isNotEmpty() }
          .map { wallets: Array<Wallet> ->
            wallets[0]
          }
          .flatMapCompletable { wallet: Wallet ->
            setDefaultWallet(wallet.address)
          }
          .andThen(getDefaultWallet())
      }
  }

  private fun getEarningBonus(
    wallet: String, packageName: String,
    amount: BigDecimal, promoCodeString: String?
  ): Single<ForecastBonus> {
    return getForecastBonus(wallet, packageName, amount, promoCodeString)
  }

  private val localCurrency: Single<FiatValue>
    get() = getAppcToLocalFiat("1.0", 18)

  private fun getAppcToLocalFiat(
    value: String, scale: Int,
    getFromCache: Boolean = false
  ): Single<FiatValue> {
    return if (getFromCache) {
      currencyConversionRatesPersistence.getAppcToLocalFiat(value, scale)
    } else getValueToFiat(value, "APPC", null, scale)
      .flatMap {
        currencyConversionRatesPersistence.saveRateFromAppcToFiat(
          value, it.amount
            .toString(), it.currency, it.symbol
        )
          .andThen(Single.just(it))
          .onErrorReturn { throwable: Throwable ->
            throwable.printStackTrace()
            it
          }
      }
  }

  private fun getUserBonusAndLevel(
    wallet: String,
    promoCodeString: String?
  ): Single<ForecastBonusAndLevel> {
    return getUserStats(wallet, promoCodeString, false)
      .map { map(it) }
      .lastOrError()
      .onErrorReturn { mapReferralError(it) }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStats(
    wallet: String,
    promoCodeString: String?,
    offlineFirst: Boolean = true
  ): Observable<UserStats> {
    return getUserStatsFromResponses(wallet, promoCodeString, offlineFirst)
      .flatMap { userStatusResponse ->
        val gamification =
          userStatusResponse.promotions.firstOrNull { it is GamificationResponse } as GamificationResponse?
        if (userStatusResponse.error == null && !userStatusResponse.fromCache) {
          userStatsLocalData.setGamificationLevel(
            gamification?.level ?: GamificationStats.INVALID_LEVEL
          )
        }
        Observable.just(userStatusResponse)
      }
      .doOnError { it.printStackTrace() }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromResponses(
    wallet: String, promoCodeString: String?,
    offlineFirst: Boolean = true
  ): Observable<UserStats> {
    return if (offlineFirst) {
      Observable.concat(getUserStatsFromDB(wallet), getUserStatsFromAPI(wallet, promoCodeString))
    } else {
      getUserStatsFromAPI(wallet, promoCodeString, true)
    }
  }

  // NOTE: the use of the throwable parameter can be dropped once all usages in these repository
  //  follow offline first logic.
  private fun getUserStatsFromDB(
    wallet: String,
    throwable: Throwable? = null
  ): Observable<UserStats> {
    return Single.zip(
      userStatsLocalData.getPromotions(),
      userStatsLocalData.retrieveWalletOrigin(wallet)
    ) { promotions: List<PromotionsResponse>, walletOrigin: WalletOrigin ->
      Pair(promotions, walletOrigin)
    }
      .toObservable()
      .map { (promotions, walletOrigin) ->
        if (throwable == null) UserStats(promotions, walletOrigin, null, true)
        else mapErrorToUserStatsModel(promotions, walletOrigin, throwable)
      }
      .onErrorReturn {
        mapErrorToUserStatsModel(throwable ?: it, throwable == null)
      }
  }

  private fun mapErrorToUserStatsModel(
    promotions: List<PromotionsResponse>,
    walletOrigin: WalletOrigin,
    throwable: Throwable
  ): UserStats {
    return when {
      promotions.isEmpty() && throwable.isNoNetworkException() -> {
        throwable.printStackTrace()
        UserStats(Status.NO_NETWORK)
      }
      promotions.isEmpty() -> {
        throwable.printStackTrace()
        UserStats(Status.UNKNOWN_ERROR)
      }
      else -> UserStats(promotions, walletOrigin)
    }
  }

  private fun mapErrorToUserStatsModel(throwable: Throwable, fromCache: Boolean): UserStats {
    return if (throwable.isNoNetworkException()) {
      UserStats(Status.NO_NETWORK, fromCache)
    } else {
      UserStats(Status.UNKNOWN_ERROR, fromCache)
    }
  }

  // NOTE: the use of the Boolean flag will be dropped once all usages in these repository follow
  //  offline first logic.
  private fun getUserStatsFromAPI(
    wallet: String, promoCodeString: String?,
    useDbOnError: Boolean = false
  ): Observable<UserStats> {
    return gamificationApi.getUserStats(wallet, Locale.getDefault().language, promoCodeString)
      .map { filterByDate(it) }
      .flatMapObservable {
        userStatsLocalData.deleteAndInsertPromotions(it.promotions)
          .andThen(userStatsLocalData.insertWalletOrigin(wallet, it.walletOrigin))
          .toSingle { UserStats(it.promotions, it.walletOrigin) }
          .toObservable()
      }
      .onErrorResumeNext { throwable: Throwable ->
        if (useDbOnError) getUserStatsFromDB(wallet, throwable)
        else Observable.just(mapErrorToUserStatsModel(throwable, false))
      }
  }

  private fun filterByDate(userStatusResponse: UserStatusResponse): UserStatusResponse {
    val validPromotions = userStatusResponse.promotions.filter { hasValidDate(it) }
    return UserStatusResponse(validPromotions, userStatusResponse.walletOrigin)
  }

  private fun hasValidDate(promotionsResponse: PromotionsResponse): Boolean {
    return if (promotionsResponse is GenericResponse) {
      val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
      currentTime < promotionsResponse.endDate
    } else true
  }

  private fun getForecastBonus(
    wallet: String, packageName: String,
    amount: BigDecimal,
    promoCodeString: String?
  ): Single<ForecastBonus> {
    return api.getForecastBonus(wallet, packageName, amount, "APPC", promoCodeString)
      .map { map(it) }
      .onErrorReturn { mapForecastError(it) }
  }

  private fun map(bonusResponse: ForecastBonusResponse): ForecastBonus {
    if (bonusResponse.status == ForecastBonusResponse.Status.ACTIVE) {
      return ForecastBonus(ForecastBonus.Status.ACTIVE, bonusResponse.bonus)
    }
    return ForecastBonus(ForecastBonus.Status.INACTIVE)
  }

  private fun mapForecastError(throwable: Throwable): ForecastBonus {
    throwable.printStackTrace()
    return if (throwable.isNoNetworkException()) {
      ForecastBonus(ForecastBonus.Status.NO_NETWORK)
    } else {
      ForecastBonus(ForecastBonus.Status.UNKNOWN_ERROR)
    }
  }

  private fun mapReferralError(throwable: Throwable): ForecastBonusAndLevel {
    throwable.printStackTrace()
    return when (throwable) {
      is UnknownHostException -> ForecastBonusAndLevel(ForecastBonus.Status.NO_NETWORK)
      else -> {
        ForecastBonusAndLevel(ForecastBonus.Status.UNKNOWN_ERROR)
      }
    }
  }

  private fun map(userStats: UserStats): ForecastBonusAndLevel {
    val gamification = userStats.promotions
      .firstOrNull {
        it is GamificationResponse && it.id == Gamification.GAMIFICATION_ID
      } as GamificationResponse?

    val referral = userStats.promotions
      .firstOrNull {
        it is GamificationResponse && it.id == Gamification.REFERRAL_ID
      } as ReferralResponse?

    return if (referral == null || referral.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
      ForecastBonusAndLevel(
        status = ForecastBonus.Status.INACTIVE,
        level = gamification?.level ?: 0
      )
    } else {
      ForecastBonusAndLevel(
        ForecastBonus.Status.ACTIVE,
        referral.pendingAmount,
        minAmount = referral.minAmount,
        level = gamification?.level ?: 0
      )
    }
  }

  private fun getValueToFiat(
    value: String, currency: String, targetCurrency: String? = null,
    scale: Int
  ): Single<FiatValue> {
    val api = if (targetCurrency != null) tokenToLocalFiatApi.getValueToTargetFiat(
      currency, value,
      targetCurrency
    ) else tokenToLocalFiatApi.getValueToTargetFiat(currency, value)
    return api.map { response: ConversionResponseBody ->
      FiatValue(
        response.value
          .setScale(scale, RoundingMode.FLOOR), response.currency, response.sign
      )
    }
  }

  private fun hasAsyncLocalPayment(): Boolean {
    return pref.contains(LOCAL_PAYMENT_METHOD_KEY)
  }

  private fun removePreSelectedPaymentMethod() {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.remove(PRE_SELECTED_PAYMENT_METHOD_KEY)
    editor.apply()
  }

  private fun removeAsyncLocalPayment() {
    val editor: SharedPreferences.Editor = pref.edit()
    editor.remove(LOCAL_PAYMENT_METHOD_KEY)
    editor.apply()
  }

  private fun showSupport(gamificationLevel: Int): Completable = getWalletAddress()
    .observeOn(AndroidSchedulers.mainThread())
    .flatMapCompletable { showSupport(it, gamificationLevel) }
    .subscribeOn(Schedulers.io())

  private fun showSupport(walletAddress: String, gamificationLevel: Int): Completable =
    Completable.fromAction {
      registerUser(gamificationLevel, walletAddress)
      displayChatScreen()
    }

  private fun registerUser(level: Int, walletAddress: String) {
    // force lowercase to make sure 2 users are not registered with the same wallet address, where
    // one has uppercase letters (to be check summed), and the other does not
    val address = walletAddress.lowercase(Locale.ROOT)
    val currentUser = supportRepository.getCurrentUser()
    if (currentUser.userAddress != address || currentUser.gamificationLevel != level) {
      if (currentUser.userAddress != address) {
        Intercom.client()
          .logout()
      }
      supportRepository.saveNewUser(address, level)
    }
  }

  private fun displayChatScreen() {
    supportRepository.resetUnreadConversations()
    Intercom.client().displayMessenger()
  }

  private fun getPaymentMethods(
    transaction: TransactionBuilder,
    transactionValue: String,
    currency: String
  ): Single<List<PaymentMethod>> {
    return getPaymentMethods(
      transactionValue, currency,
      transaction.type
    )
      .flatMap { paymentMethods ->
        getAvailablePaymentMethods(transaction, paymentMethods)
          .flatMap { availablePaymentMethods ->
            Observable.fromIterable(paymentMethods)
              .map { paymentMethod -> mapPaymentMethods(paymentMethod, availablePaymentMethods) }
              .flatMap { paymentMethod -> retrieveDisableReason(paymentMethod, transaction) }
              .toList()
          }
          .map(this@_PaymentMethodsLogic::removePaymentMethods)
      }
      .map(this@_PaymentMethodsLogic::swapDisabledPositions)
      .map(this@_PaymentMethodsLogic::showTopup)
  }

  private fun getPaymentMethods(
    value: String? = null,
    currency: String? = null,
    currencyType: String? = null,
    direct: Boolean? = null,
    transactionType: String? = null
  ): Single<List<PaymentMethodEntity>> = getPaymentMethodsRepo(
    value = value,
    currency = currency,
    currencyType = currencyType,
    direct = direct,
    transactionType = transactionType
  )
    .onErrorReturn {
      it.printStackTrace()
      ArrayList()
    }

  private fun getPaymentMethodsRepo(
    value: String?,
    currency: String?,
    currencyType: String?,
    direct: Boolean? = null,
    transactionType: String?
  ): Single<List<PaymentMethodEntity>> =
    brokerBdsApi.getPaymentMethods(value, currency, currencyType, direct, transactionType)
      .map { map(it) }

  private fun map(gatewaysResponse: GetMethodsResponse): List<PaymentMethodEntity> {
    return gatewaysResponse.items
  }

  private fun getAvailablePaymentMethods(
    transaction: TransactionBuilder, paymentMethods: List<PaymentMethodEntity>
  ): Single<List<PaymentMethodEntity>> {
    return getFilteredGateways(transaction).map { filteredGateways ->
      removeUnavailable(
        paymentMethods,
        filteredGateways
      )
    }
  }

  private fun getFilteredGateways(transactionBuilder: TransactionBuilder): Single<List<Gateway.Name>> {
    return Single.zip(
      getRewardsBalance(),
      hasAppcoinsFunds(transactionBuilder)
    ) { creditsBalance, hasAppcoinsFunds ->
      getNewPaymentGateways(
        creditsBalance,
        hasAppcoinsFunds,
        transactionBuilder.amount()
      )
    }
  }

  private fun removeUnavailable(
    paymentMethods: List<PaymentMethodEntity>,
    filteredGateways: List<Gateway.Name>
  ): List<PaymentMethodEntity> {
    val clonedPaymentMethods: MutableList<PaymentMethodEntity> = ArrayList(paymentMethods)
    val iterator = clonedPaymentMethods.iterator()
    while (iterator.hasNext()) {
      val paymentMethod = iterator.next()
      val id = paymentMethod.id
      if (id == APPC_ID && !filteredGateways.contains(Gateway.Name.appcoins)) {
        iterator.remove()
      } else if (id == CREDITS_ID && !filteredGateways.contains(
          Gateway.Name.appcoins_credits
        )
      ) {
        iterator.remove()
      } else if (paymentMethod.gateway != null && (paymentMethod.gateway
          .name === Gateway.Name.myappcoins
            || paymentMethod.gateway
          .name === Gateway.Name.adyen_v2) && !paymentMethod.isAvailable()
      ) {
        iterator.remove()
      }
    }
    return clonedPaymentMethods
  }

  private fun getRewardsBalance(): Single<BigDecimal> {
    return getWalletInfo(null, false, false)
      .map { (_, _, walletBalance): WalletInfo ->
        walletBalance
          .creditsBalance
          .token
          .amount
      }
  }

  private fun hasAppcoinsFunds(transaction: TransactionBuilder): Single<Boolean> {
    return isAppcoinsPaymentReady(transaction)
  }

  private fun getNewPaymentGateways(
    creditsBalance: BigDecimal,
    hasAppcoinsFunds: Boolean,
    amount: BigDecimal
  ): List<Gateway.Name> {
    val list: MutableList<Gateway.Name> = LinkedList()
    if (creditsBalance.compareTo(amount) >= 0) {
      list.add(Gateway.Name.appcoins_credits)
    }
    if (hasAppcoinsFunds) {
      list.add(Gateway.Name.appcoins)
    }
    list.add(Gateway.Name.adyen_v2)
    return list
  }

  private fun mapPaymentMethods(
    paymentMethod: PaymentMethodEntity,
    availablePaymentMethods: List<PaymentMethodEntity>
  ): PaymentMethod {
    for (availablePaymentMethod in availablePaymentMethods) {
      if (paymentMethod.id == availablePaymentMethod.id) {
        val paymentMethodFee = mapPaymentMethodFee(availablePaymentMethod.fee)
        return PaymentMethod(
          id = paymentMethod.id,
          label = paymentMethod.label,
          iconUrl = paymentMethod.iconUrl,
          async = paymentMethod.async,
          fee = paymentMethodFee,
          isEnabled = true,
          disabledReason = null,
          showTopup = false
        )
      }
    }
    val paymentMethodFee = mapPaymentMethodFee(paymentMethod.fee)
    return PaymentMethod(
      id = paymentMethod.id,
      label = paymentMethod.label,
      iconUrl = paymentMethod.iconUrl,
      async = paymentMethod.async,
      fee = paymentMethodFee,
      isEnabled = false,
      disabledReason = null,
      showTopup = false
    )
  }

  private fun mapPaymentMethodFee(feeEntity: FeeEntity?): PaymentMethodFee? {
    return if (feeEntity == null) {
      null
    } else {
      if (feeEntity.type === FeeType.EXACT) {
        PaymentMethodFee(
          isExact = true,
          amount = feeEntity.cost?.value,
          currency = feeEntity.cost?.currency
        )
      } else {
        PaymentMethodFee(false, null, null)
      }
    }
  }

  private fun retrieveDisableReason(
    paymentMethod: PaymentMethod,
    transaction: TransactionBuilder
  ): Observable<PaymentMethod> {
    if (!paymentMethod.isEnabled) {
      if (paymentMethod.id
        == CREDITS_ID
      ) {
        paymentMethod.disabledReason = R.string.purchase_appcoins_credits_noavailable_body
      } else if (paymentMethod.id
        == APPC_ID
      ) {
        return getAppcDisableReason(transaction).filter(Predicate { reason: Int -> reason != -1 })
          .map { reason: Int? ->
            paymentMethod.disabledReason = reason
            paymentMethod
          }
      }
    }
    return Observable.just(paymentMethod)
  }

  private fun getAppcDisableReason(transaction: TransactionBuilder): Observable<Int> {
    return getBalanceState(transaction).map { balanceState ->
      return@map when (balanceState) {
        BalanceState.NO_ETHER -> R.string.purchase_no_eth_body
        BalanceState.NO_TOKEN, BalanceState.NO_ETHER_NO_TOKEN -> R.string.purchase_no_appcoins_body
        BalanceState.OK -> -1
        else -> -1
      }
    }
      .toObservable()
  }

  private fun removePaymentMethods(paymentMethods: MutableList<PaymentMethod>): MutableList<PaymentMethod> {
    val iterator = paymentMethods.iterator()
    while (iterator.hasNext()) {
      val paymentMethod = iterator.next()
      if (paymentMethod.id == "earn_appcoins") {
        iterator.remove()
      }
    }
    return paymentMethods
  }

  private fun swapDisabledPositions(paymentMethods: MutableList<PaymentMethod>): MutableList<PaymentMethod> {
    var swapped = false
    if (paymentMethods.size > 1) {
      for (position in 1 until paymentMethods.size) {
        if (shouldSwap(paymentMethods, position)) {
          Collections.swap(paymentMethods, position, position - 1)
          swapped = true
          break
        }
      }
      if (swapped) {
        swapDisabledPositions(paymentMethods)
      }
    }
    return paymentMethods
  }

  private fun shouldSwap(paymentMethods: List<PaymentMethod>, position: Int): Boolean {
    return paymentMethods[position]
      .isEnabled && !paymentMethods[position - 1]
      .isEnabled
  }

  private fun showTopup(paymentMethods: MutableList<PaymentMethod>): MutableList<PaymentMethod> {
    if (paymentMethods.size == 0) {
      return paymentMethods
    }
    var appcCreditPaymentIndex = 0
    for (i in paymentMethods.indices) {
      val paymentMethod = paymentMethods[i]
      if (paymentMethod.isEnabled) {
        return paymentMethods
      }
      if (paymentMethod.id == CREDITS_ID) {
        appcCreditPaymentIndex = i
      }
    }
    val appcPaymentMethod = paymentMethods[appcCreditPaymentIndex]
    paymentMethods[appcCreditPaymentIndex] =
      PaymentMethod(
        appcPaymentMethod.id, appcPaymentMethod.label,
        appcPaymentMethod.iconUrl, appcPaymentMethod.async,
        appcPaymentMethod.fee, appcPaymentMethod.isEnabled,
        appcPaymentMethod.disabledReason, true
      )
    return paymentMethods
  }

  private fun mergeAppcoins(paymentMethods: List<PaymentMethod>): MutableList<PaymentMethod> {
    val appcMethod = getAppcMethod(paymentMethods)
    val creditsMethod = getCreditsMethod(paymentMethods)
    return if (appcMethod != null && creditsMethod != null) {
      buildMergedList(paymentMethods, appcMethod, creditsMethod)
    } else paymentMethods.toMutableList()
  }

  private fun getCreditsMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == CREDITS_ID) {
        return paymentMethod
      }
    }
    return null
  }

  private fun getAppcMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == APPC_ID) {
        return paymentMethod
      }
    }
    return null
  }

  private fun buildMergedList(
    paymentMethods: List<PaymentMethod>,
    appcMethod: PaymentMethod,
    creditsMethod: PaymentMethod
  ): MutableList<PaymentMethod> {
    val mergedList: MutableList<PaymentMethod> = ArrayList()
    var addedMergedAppc = false
    for (paymentMethod in paymentMethods) {
      if (((paymentMethod.id == APPC_ID) || (paymentMethod.id == CREDITS_ID)) && !addedMergedAppc) {
        val mergedId = "merged_appcoins"
        val mergedLabel = creditsMethod.label + " / " + appcMethod.label
        val isMergedEnabled = appcMethod.isEnabled || creditsMethod.isEnabled
        val disableReason = mergeDisableReason(appcMethod, creditsMethod)
        mergedList.add(
          AppCoinsPaymentMethod(
            mergedId, mergedLabel, appcMethod.iconUrl,
            isMergedEnabled, appcMethod.isEnabled, creditsMethod.isEnabled,
            appcMethod.label, creditsMethod.label, creditsMethod.iconUrl,
            disableReason, appcMethod.disabledReason, creditsMethod.disabledReason
          )
        )
        addedMergedAppc = true
      } else if (paymentMethod.id != CREDITS_ID && paymentMethod.id != APPC_ID) {
        mergedList.add(paymentMethod)
      }
    }
    return mergedList
  }

  private fun mergeDisableReason(appcMethod: PaymentMethod, creditsMethod: PaymentMethod): Int? {
    var creditsReason = creditsMethod.disabledReason
    var appcReason = appcMethod.disabledReason
    if (creditsReason == null) {
      creditsReason = -1
    }
    if (appcReason == null) {
      appcReason = -1
    }
    if (!creditsMethod.isEnabled && !appcMethod.isEnabled) {
      // Specific cases that are treated differently:
      // - If user does not have APPC-C, has APPC, but no ETH, the message should be to display
      //    that user does not have enough ETH (may have none, or some but not enough)
      // - If user does not have APPC-C nor APPC (ETH value doesn't matter),
      //    the message should be more generic, indicating that user does not have funds
      return if (appcReason === R.string.purchase_no_eth_body) appcReason else R.string.p2p_send_error_not_enough_funds
    }
    if (!creditsMethod.isEnabled) {
      return if (creditsReason != -1) creditsReason else appcReason
    }
    return if (!appcMethod.isEnabled) {
      if (appcReason != -1) appcReason else creditsReason
    } else null
  }

  private fun getPreSelectedPaymentMethod(): String? {
    return pref.getString(PRE_SELECTED_PAYMENT_METHOD_KEY, PaymentMethodId.APPC_CREDITS.id)
  }

  private fun getLastUsedPaymentMethod(): String? {
    return pref.getString(LAST_USED_PAYMENT_METHOD_KEY, PaymentMethodId.CREDIT_CARD.id)
  }

  private fun getPurchases(
    packageName: String,
    type: BillingSupportedType,
    scheduler: Scheduler
  ): Single<List<Purchase>> {
    return if (BillingSupportedType.isManagedType(type)) {
      getAndSignCurrentWalletAddress()
        .observeOn(scheduler)
        .flatMap {
          getPurchases(packageName, it.address, it.signedAddress, type)
        }
        .onErrorReturn { emptyList() }
    } else Single.just(emptyList())
  }

  private fun getPurchases(
    packageName: String,
    walletAddress: String,
    walletSignature: String,
    type: BillingSupportedType
  ): Single<List<Purchase>> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      getPurchases(packageName, walletAddress, walletSignature)
    } else {
      getPurchasesSubs(packageName, walletAddress, walletSignature)
    }

  private fun getPurchases(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> =
    inappBdsApi.getPurchases(
      packageName,
      walletAddress,
      walletSignature,
      BillingSupportedType.INAPP.name.toLowerCase(Locale.ROOT)
    )
      .map { map(packageName, it) }

  private fun getPurchasesSubs(
    packageName: String,
    walletAddress: String,
    walletSignature: String
  ): Single<List<Purchase>> =
    subsApi.getPurchases(packageName, walletAddress, walletSignature)
      .map { map(packageName, it) }


  private fun map(
    packageName: String,
    purchasesResponseSubscription: SubscriptionPurchaseListResponse
  ): List<Purchase> {
    return purchasesResponseSubscription.items.map { map(packageName, it) }
  }

  private fun getSkuDetails(
    domain: String,
    sku: String,
    type: BillingSupportedType
  ): Single<Product> {
    return getProducts(domain, mutableListOf(sku), type)
      .map { products -> products.first() }
  }

  private fun getProducts(
    merchantName: String, skus: List<String>,
    type: BillingSupportedType
  ): Single<List<Product>> {
    return getSkuDetails(merchantName, skus, type)
  }

  private fun getSkuDetails(
    packageName: String,
    skus: List<String>,
    type: BillingSupportedType
  ): Single<List<Product>> =
    if (BillingSupportedType.mapToProductType(type) == BillingSupportedType.INAPP) {
      getSkuDetails(packageName, skus)
    } else {
      getSkuDetailsSubs(packageName, skus)
    }

  private fun getSkuDetails(packageName: String, skus: List<String>): Single<List<Product>> =
    requestSkusDetails(packageName, skus).map { map(it) }

  private fun getSkuDetailsSubs(packageName: String, skus: List<String>): Single<List<Product>> =
    requestSkusDetailsSubs(packageName, skus).map { map(it) }

  private fun requestSkusDetails(
    packageName: String,
    skus: List<String>
  ): Single<DetailsResponseBody> =
    if (skus.size <= SKUS_DETAILS_REQUEST_LIMIT) {
      inappBdsApi.getConsumables(packageName, skus.joinToString(separator = ","))
    } else {
      Single.zip(
        inappBdsApi.getConsumables(
          packageName,
          skus.take(SKUS_DETAILS_REQUEST_LIMIT).joinToString(separator = ",")
        ), requestSkusDetails(packageName, skus.drop(SKUS_DETAILS_REQUEST_LIMIT))
      ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
    }

  private fun requestSkusDetailsSubs(
    packageName: String,
    skus: List<String>
  ): Single<SubscriptionsResponse> =
    if (skus.size <= SKUS_SUBS_DETAILS_REQUEST_LIMIT) {
      subsApi.getSubscriptions(Locale.getDefault().toLanguageTag(), packageName, skus)
    } else {
      Single.zip(
        subsApi.getSubscriptions(
          Locale.getDefault().toLanguageTag(),
          packageName,
          skus.take(SKUS_SUBS_DETAILS_REQUEST_LIMIT)
        ), requestSkusDetailsSubs(packageName, skus.drop(SKUS_SUBS_DETAILS_REQUEST_LIMIT))
      ) { firstResponse, secondResponse -> firstResponse.merge(secondResponse) }
    }

  private fun map(productDetails: DetailsResponseBody): List<Product> =
    ArrayList(productDetails.items.map {
      InAppProduct(
        sku = it.sku,
        title = it.title,
        description = it.description,
        transactionPrice = TransactionPrice(
          base = it.price.currency,
          appcoinsAmount = it.price.appc.value.toDouble(),
          amount = it.price.value.toDouble(),
          currency = it.price.currency,
          currencySymbol = it.price.symbol
        ),
        billingType = BillingSupportedType.INAPP.name
      )
    })

  private fun map(subscriptionsResponse: SubscriptionsResponse): List<Product> {
    return ArrayList(subscriptionsResponse.items.map {
      SubsProduct(
        sku = it.sku,
        title = it.title,
        description = it.description,
        transactionPrice = TransactionPrice(
          base = it.price.currency,
          appcoinsAmount = it.price.appc.value.toDouble(),
          amount = it.price.value.toDouble(),
          currency = it.price.currency,
          currencySymbol = it.price.symbol
        ),
        billingType = BillingSupportedType.SUBS_TYPE,
        subscriptionPeriod = it.period,
        trialPeriod = it.trialPeriod
      )
    })
  }

  private fun convertCurrencyToLocalFiat(value: Double, currency: String): Single<FiatValue> =
    convertFiatToLocalFiat(value, currency)

  private fun convertFiatToLocalFiat(value: Double, currency: String): Single<FiatValue> {
    return getLocalFiatAmount(value.toString(), currency)
  }

  private fun getLocalFiatAmount(value: String, currency: String): Single<FiatValue> {
    return getFiatToLocalFiat(currency, value, 2)
  }

  private fun getFiatToLocalFiat(currency: String, value: String, scale: Int): Single<FiatValue> {
    return tokenToLocalFiatApi.getValueToTargetFiat(currency, value)
      .map { response: ConversionResponseBody ->
        FiatValue(
          amount = response.value.setScale(scale, RoundingMode.FLOOR),
          currency = response.currency,
          symbol = response.sign
        )
      }
  }

  private fun convertCurrencyToAppc(value: Double, currency: String): Single<FiatValue> =
    convertFiatToAppc(value, currency)

  private fun convertFiatToAppc(value: Double, currency: String): Single<FiatValue> {
    return getFiatToAppcAmount(value.toString(), currency)
  }

  private fun getFiatToAppcAmount(value: String, currency: String): Single<FiatValue> {
    return getFiatToAppc(currency, value, 18)
  }

  private fun getFiatToAppc(currency: String, value: String, scale: Int): Single<FiatValue> {
    return tokenToLocalFiatApi.convertFiatToAppc(currency, value)
      .map { response: ConversionResponseBody ->
        FiatValue(
          response.value
            .setScale(scale, RoundingMode.FLOOR), response.currency, response.sign
        )
      }
  }

  private enum class BalanceType {
    APPC, ETH, APPC_C
  }

  enum class ViewState {
    DEFAULT, ITEM_ALREADY_OWNED, ERROR
  }

  private enum class BalanceState {
    NO_TOKEN, NO_ETHER, NO_ETHER_NO_TOKEN, OK
  }

  companion object {
    private val TAG = _PaymentMethodsLogic::class.java.name
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val HAS_STARTED_AUTH = "has_started_auth"
    private const val FIAT_VALUE = "fiat_value"
    private const val PAYMENT_NAVIGATION_DATA = "payment_navigation_data"

    private const val CURRENT_ACCOUNT_ADDRESS_KEY = "current_account_address"

    private val paymentGasLimit = BigDecimal(BuildConfig.PAYMENT_GAS_LIMIT)

    private const val INSUFFICIENT_ERROR_MESSAGE = "insufficient funds for gas * price + value"
    private const val NONCE_TOO_LOW_ERROR_MESSAGE = "nonce too low"

    private const val PRE_SELECTED_PAYMENT_METHOD_KEY = "PRE_SELECTED_PAYMENT_METHOD_KEY"
    private const val LAST_USED_PAYMENT_METHOD_KEY = "LAST_USED_PAYMENT_METHOD_KEY"
    private const val AUTHENTICATION_PERMISSION = "authentication_permission"
    private const val FORBIDDEN_CODE = 403

    private const val LOCAL_PAYMENT_METHOD_KEY = "LOCAL_PAYMENT_METHOD_KEY"
    private const val APPC_ID = "appcoins"
    private const val CREDITS_ID = "appcoins_credits"

    private const val SKUS_DETAILS_REQUEST_LIMIT = 50
    private const val SKUS_SUBS_DETAILS_REQUEST_LIMIT = 100

    const val TRANSACTION_HASH = "transaction_hash"
  }
}

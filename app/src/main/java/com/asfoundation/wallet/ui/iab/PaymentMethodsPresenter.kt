package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import androidx.annotation.StringRes
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.State
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.commons.Logger
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.TaskTimer
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.PaymentNavigationData
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.PaymentMethodId
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod.*
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.Single.zip
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import retrofit2.HttpException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class PaymentMethodsPresenter(
    private val view: PaymentMethodsView,
    private val viewScheduler: Scheduler,
    private val networkThread: Scheduler,
    private val disposables: CompositeDisposable,
    private val analytics: PaymentMethodsAnalytics,
    private val transaction: TransactionBuilder,
    private val paymentMethodsMapper: PaymentMethodsMapper,
    private val formatter: CurrencyFormatUtils,
    private val logger: Logger,
    private val interactor: PaymentMethodsInteractor,
    private val paymentMethodsData: PaymentMethodsData,
    private val taskTimer: TaskTimer
) {

  private var cachedGamificationLevel = 0
  private var cachedFiatValue: FiatValue? = null
  private var cachedPaymentNavigationData: PaymentNavigationData? = null
  private var viewState: ViewState = ViewState.DEFAULT
  private var hasStartedAuth = false

  companion object {
    private val TAG = PaymentMethodsPresenter::class.java.name
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val HAS_STARTED_AUTH = "has_started_auth"
    private const val FIAT_VALUE = "fiat_value"
    private const val PAYMENT_NAVIGATION_DATA = "payment_navigation_data"
  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      cachedGamificationLevel = savedInstanceState.getInt(GAMIFICATION_LEVEL)
      hasStartedAuth = savedInstanceState.getBoolean(HAS_STARTED_AUTH)
      cachedFiatValue = savedInstanceState.getSerializable(FIAT_VALUE) as FiatValue?
      cachedPaymentNavigationData =
          savedInstanceState.getSerializable(PAYMENT_NAVIGATION_DATA) as PaymentNavigationData?
    }
    handleOnGoingPurchases()
    handleCancelClick()
    handleErrorDismisses()
    handleMorePaymentMethodClicks()
    handleBuyClick()
    handleSupportClicks()
    handleAuthenticationResult()
    handleTopupClicks()
    if (paymentMethodsData.isBds) handlePaymentSelection()
  }

  private fun handleTopupClicks() {
    disposables.add(view.getTopupClicks()
        .retry()
        .doOnNext { view.showTopupFlow() }
        .subscribe())
  }

  fun onResume(firstRun: Boolean) {
    startMeasure(PaymentMethodsAnalytics.WALLET_PAYMENT_LOADING_TOTAL, firstRun)
    if (firstRun.not()) view.showPaymentsSkeletonLoading()
    setupUi(firstRun)
  }

  private fun handlePaymentSelection() {
    disposables.add(
        view.getPaymentSelection()
            .observeOn(viewScheduler)
            .doOnNext { selectedPaymentMethod ->
              if (interactor.isBonusActiveAndValid()) {
                handleBonusVisibility(selectedPaymentMethod)
              } else {
                view.removeBonus()
              }
              handlePositiveButtonText(selectedPaymentMethod)
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleBuyClick() {
    disposables.add(view.getBuyClick()
        .map { view.getSelectedPaymentMethod(interactor.hasPreSelectedPaymentMethod()) }
        .observeOn(viewScheduler)
        .doOnNext { handleBuyAnalytics(it) }
        .doOnNext { selectedPaymentMethod ->
          when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
            APPC_CREDITS -> {
              view.showProgressBarLoading()
              handleWalletBlockStatus(selectedPaymentMethod)
            }
            MERGED_APPC -> view.showMergedAppcoins(
                cachedGamificationLevel,
                cachedFiatValue!!, transaction, paymentMethodsData.frequency,
                paymentMethodsData.subscription
            )

            else -> {
              if (interactor.hasAuthenticationPermission()) {
                showAuthenticationActivity(
                    selectedPaymentMethod,
                    interactor.hasPreSelectedPaymentMethod()
                )
              } else {
                when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
                  PAYPAL -> view.showPaypal(
                      cachedGamificationLevel,
                      cachedFiatValue!!, paymentMethodsData.frequency,
                      paymentMethodsData.subscription
                  )
                  CREDIT_CARD -> view.showCreditCard(
                      cachedGamificationLevel,
                      cachedFiatValue!!, paymentMethodsData.frequency,
                      paymentMethodsData.subscription
                  )
                  APPC -> view.showAppCoins(cachedGamificationLevel,
                      transaction)
                  SHARE_LINK -> view.showShareLink(selectedPaymentMethod.id)
                  LOCAL_PAYMENTS -> view.showLocalPayment(
                      selectedPaymentMethod.id, selectedPaymentMethod.iconUrl,
                      selectedPaymentMethod.label, selectedPaymentMethod.async,
                      cachedFiatValue!!.amount.toString(), cachedFiatValue!!.currency,
                      cachedGamificationLevel
                  )
                  CARRIER_BILLING -> view.showCarrierBilling(
                      cachedFiatValue!!, false
                  )
                  else -> return@doOnNext
                }
              }
            }
          }
        }
        .retry()
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.onAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          if (cachedPaymentNavigationData == null) close()
          else if (!it) {
            hasStartedAuth = false
            if (cachedPaymentNavigationData!!.isPreselected &&
                hasPaymentOwnPreselectedView(cachedPaymentNavigationData!!.paymentId)
            ) {
              close()
            }
          } else {
            navigateToPayment(cachedPaymentNavigationData!!)
          }
        }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun hasPaymentOwnPreselectedView(paymentId: String): Boolean {
    val paymentMethod = paymentMethodsMapper.map(paymentId)
    return paymentMethod == CREDIT_CARD ||
        paymentMethod == CARRIER_BILLING
  }

  private fun handleWalletBlockStatus(selectedPaymentMethod: PaymentMethod) {
    disposables.add(interactor.isWalletBlocked()
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (interactor.hasAuthenticationPermission()) {
            showAuthenticationActivity(
                selectedPaymentMethod,
                interactor.hasPreSelectedPaymentMethod()
            )
          } else {
            view.showCredits(cachedGamificationLevel, transaction)
          }
        }
        .doOnError { showError(it) }
        .subscribe({}, { showError(it) })
    )
  }

  private fun handleOnGoingPurchases() {
    val billingSupportedType =
        transaction.type?.let { BillingSupportedType.valueOfInsensitive(it) }
    if (transaction.skuId == null || billingSupportedType == null) {
      disposables.add(isSetupCompleted()
          .doOnComplete { view.hideLoading() }
          .subscribeOn(viewScheduler)
          .subscribe({}, { it.printStackTrace() })
      )
      return
    }
    disposables.add(waitForUi(transaction.skuId, billingSupportedType)
        .observeOn(viewScheduler)
        .doOnComplete { view.hideLoading() }
        .subscribe({ }, { showError(it) })
    )
  }

  private fun navigateToPayment(paymentNavigationData: PaymentNavigationData) {
    when (paymentMethodsMapper.map(paymentNavigationData.paymentId)) {
      PAYPAL -> view.showPaypal(cachedGamificationLevel, cachedFiatValue!!,
          paymentMethodsData.frequency, paymentMethodsData.subscription)
      CREDIT_CARD -> {
        if (paymentNavigationData.isPreselected) {
          view.showAdyen(
              cachedFiatValue!!.amount, cachedFiatValue!!.currency, PaymentType.CARD,
              paymentNavigationData.paymentIconUrl, cachedGamificationLevel,
              paymentMethodsData.frequency, paymentMethodsData.subscription
          )
        } else view.showCreditCard(cachedGamificationLevel, cachedFiatValue!!,
            paymentMethodsData.frequency, paymentMethodsData.subscription)
      }
      APPC -> view.showAppCoins(cachedGamificationLevel, transaction)
      APPC_CREDITS -> view.showCredits(cachedGamificationLevel, transaction)
      SHARE_LINK -> view.showShareLink(paymentNavigationData.paymentId)
      LOCAL_PAYMENTS -> {
        view.showLocalPayment(
            paymentNavigationData.paymentId, paymentNavigationData.paymentIconUrl,
            paymentNavigationData.paymentLabel, paymentNavigationData.async,
            cachedFiatValue!!.amount.toString(), cachedFiatValue!!.currency,
            cachedGamificationLevel
        )
      }
      CARRIER_BILLING -> {
        view.showCarrierBilling(cachedFiatValue!!, paymentNavigationData.isPreselected)
      }
      else -> {
        showError(R.string.unknown_error)
        logger.log(TAG, "Wrong payment method after authentication.")
      }
    }
  }

  private fun isSetupCompleted(): Completable {
    return view.setupUiCompleted()
        .takeWhile { isViewSet -> !isViewSet }
        .ignoreElements()
  }

  private fun waitForUi(skuId: String?, type: BillingSupportedType): Completable {
    return Completable.mergeArray(
        checkProcessing(skuId, type).subscribeOn(networkThread),
        checkForOwnedItems(skuId, type).subscribeOn(networkThread),
        isSetupCompleted().subscribeOn(networkThread)
    )
  }

  private fun checkForOwnedItems(skuId: String?, type: BillingSupportedType): Completable {
    return Single.zip(checkAndConsumePrevious(skuId, type).subscribeOn(networkThread),
        checkSubscriptionOwned(skuId, type).subscribeOn(networkThread),
        BiFunction { itemOwned: Boolean, subStatus: SubscriptionStatus ->
          Pair(itemOwned, subStatus)
        })
        .observeOn(viewScheduler)
        .doOnSuccess { handleItemsOwned(it.first, it.second) }
        .ignoreElement()
  }

  private fun handleItemsOwned(itemOwned: Boolean, subStatus: SubscriptionStatus) {
    if (itemOwned) {
      viewState = ViewState.ITEM_ALREADY_OWNED
      view.showItemAlreadyOwnedError()
    } else {
      handleSubscriptionAvailability(subStatus)
    }
  }

  private fun checkSubscriptionOwned(skuId: String?,
                                     type: BillingSupportedType): Single<SubscriptionStatus> {
    return if (type == BillingSupportedType.INAPP_SUBSCRIPTION && skuId != null) {
      interactor.isAbleToSubscribe(paymentMethodsData.appPackage, skuId, networkThread)
          .subscribeOn(networkThread)
    } else {
      Single.just(SubscriptionStatus(true))
    }
  }

  private fun handleSubscriptionAvailability(status: SubscriptionStatus) {
    if (status.isAvailable.not()) {
      if (status.isAlreadySubscribed) {
        showError(R.string.subscriptions_error_already_subscribed)
      } else {
        showError(R.string.unknown_error)
      }
    }
  }

  private fun checkProcessing(skuId: String?, type: BillingSupportedType): Completable {
    return interactor.getSkuTransaction(
        paymentMethodsData.appPackage, skuId, networkThread, type
    )
        .subscribeOn(networkThread)
        .filter { (_, status) -> status === Transaction.Status.PROCESSING }
        .observeOn(viewScheduler)
        .doOnSuccess { view.showProcessingLoadingDialog() }
        .doOnSuccess { handleProcessing() }
        .observeOn(networkThread)
        .flatMapCompletable {
          interactor.checkTransactionStateFromTransactionId(it.uid)
              .ignoreElements()
              .andThen(
                  finishProcess(skuId, it.type, it.orderReference, it.hash,
                      it.metadata?.purchaseUid))
        }
  }

  private fun handleProcessing() {
    disposables.add(
        interactor.getCurrentPaymentStep(paymentMethodsData.appPackage, transaction)
            .filter { currentPaymentStep -> currentPaymentStep == AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_ON_CHAIN }
            .doOnSuccess {
              view.lockRotation()
              interactor.resume(
                  paymentMethodsData.uri,
                  AsfInAppPurchaseInteractor.TransactionType.NORMAL,
                  paymentMethodsData.appPackage, transaction.skuId,
                  paymentMethodsData.developerPayload, paymentMethodsData.isBds, transaction.type,
                  transaction
              )
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun finishProcess(
      skuId: String?, type: String, orderReference: String?,
      hash: String?, purchaseUid: String?
  ): Completable {
    return interactor.getSkuPurchase(
        paymentMethodsData.appPackage, skuId, purchaseUid, type, orderReference,
        hash, networkThread
    )
        .observeOn(viewScheduler)
        .doOnSuccess { bundle -> view.finish(bundle.bundle) }
        .ignoreElement()
  }

  private fun checkAndConsumePrevious(sku: String?, type: BillingSupportedType): Single<Boolean> {
    return getPurchases(type)
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .map { purchases -> hasRequestedSkuPurchase(purchases, sku) }
  }

  private fun setupUi(firstRun: Boolean) {
    disposables.add(Completable.fromAction {
      startMeasure(PaymentMethodsAnalytics.LOADING_STEP_CONVERT_TO_FIAT, firstRun)
    }
        .andThen(getPurchaseFiatValue())
        .flatMapCompletable { fiatValue ->
          endMeasure(PaymentMethodsAnalytics.LOADING_STEP_CONVERT_TO_FIAT, firstRun)
          this.cachedFiatValue = fiatValue
          startMeasure(PaymentMethodsAnalytics.LOADING_STEP_GET_PAYMENT_METHODS, firstRun)
          getPaymentMethods(fiatValue)
              .flatMapCompletable { paymentMethods ->
                endMeasure(PaymentMethodsAnalytics.LOADING_STEP_GET_PAYMENT_METHODS, firstRun)
                startMeasure(PaymentMethodsAnalytics.LOADING_STEP_GET_EARNING_BONUS, firstRun)
                interactor.getEarningBonus(transaction.domain, transaction.amount())
                    .observeOn(viewScheduler)
                    .flatMapCompletable {
                      endMeasure(PaymentMethodsAnalytics.LOADING_STEP_GET_EARNING_BONUS, firstRun)
                      Completable.fromAction {
                        startMeasure(
                            PaymentMethodsAnalytics.LOADING_STEP_GET_PROCESSING_DATA,
                            firstRun
                        )
                        view.updateProductName()
                        setupBonusInformation(it)
                        selectPaymentMethod(
                            paymentMethods, fiatValue,
                            interactor.isBonusActiveAndValid(it)
                        )
                        endMeasure(
                            PaymentMethodsAnalytics.LOADING_STEP_GET_PROCESSING_DATA,
                            firstRun
                        )
                      }
                    }
              }
        }
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .doOnComplete {
          //If first run we should rely on the hideLoading of the handleOnGoingPurchases method
          if (!firstRun) view.hideLoading()
          endMeasure(PaymentMethodsAnalytics.WALLET_PAYMENT_LOADING_TOTAL, firstRun)
        }
        .subscribe({ }, { this.showError(it) })
    )
  }

  private fun showError(t: Throwable) {
    t.printStackTrace()
    logger.log(TAG, t)
    when {
      t.isNoNetworkException() -> view.showError(R.string.notification_no_network_poa)
      isItemAlreadyOwnedError(t) -> {
        viewState = ViewState.ITEM_ALREADY_OWNED
        view.showItemAlreadyOwnedError()
      }
      else -> view.showError(R.string.activity_iab_error_message)
    }
  }

  private fun startMeasure(id: String, firstRun: Boolean) {
    if (firstRun) {
      taskTimer.start(id)
    }
  }

  private fun endMeasure(id: String, firstRun: Boolean) {
    val duration = taskTimer.end(id)
    if (firstRun && duration != -1L) {
      if (id == PaymentMethodsAnalytics.WALLET_PAYMENT_LOADING_TOTAL) {
        analytics.sendTimeToLoadTotalEvent(duration)
      } else {
        analytics.sendTimeToLoadStepEvent(id, duration)
      }
    }
  }

  private fun setupBonusInformation(forecastBonus: ForecastBonusAndLevel) {
    if (interactor.isBonusActiveAndValid(forecastBonus)) {
      if (paymentMethodsData.subscription) {
        view.setPurchaseBonus(forecastBonus.amount, forecastBonus.currency,
            R.string.subscriptions_bonus_body)
      } else {
        view.setPurchaseBonus(forecastBonus.amount, forecastBonus.currency,
            R.string.gamification_purchase_body)
      }
    } else {
      view.removeBonus()
    }
    cachedGamificationLevel = forecastBonus.level
    analytics.setGamificationLevel(cachedGamificationLevel)
  }

  private fun selectPaymentMethod(
      paymentMethods: List<PaymentMethod>, fiatValue: FiatValue,
      isBonusActive: Boolean
  ) {
    val fiatAmount = formatter.formatPaymentCurrency(fiatValue.amount, WalletCurrency.FIAT)
    val appcAmount = formatter.formatPaymentCurrency(transaction.amount(), WalletCurrency.APPCOINS)
    if (interactor.hasAsyncLocalPayment()) {
      //After a asynchronous payment credits will be used as pre selected
      getCreditsPaymentMethod(paymentMethods)?.let {
        if (it.isEnabled) {
          showPreSelectedPaymentMethod(fiatValue, it, fiatAmount, appcAmount, isBonusActive,
              paymentMethodsData.frequency)
          return
        }
      }
    }

    if (interactor.hasPreSelectedPaymentMethod()) {
      val paymentMethod = getPreSelectedPaymentMethod(paymentMethods)
      if (paymentMethod == null || !paymentMethod.isEnabled) {
        showPaymentMethods(
            fiatValue, paymentMethods,
            PaymentMethodId.CREDIT_CARD.id, fiatAmount, appcAmount,
            paymentMethodsData.frequency
        )
      } else {
        when (paymentMethod.id) {
          PaymentMethodId.CARRIER_BILLING.id,
          PaymentMethodId.CREDIT_CARD.id -> {
            if (viewState == ViewState.DEFAULT) {
              analytics.sendPurchaseDetailsEvent(
                  paymentMethodsData.appPackage, transaction.skuId,
                  transaction.amount()
                      .toString(), transaction.type
              )
              if (interactor.hasAuthenticationPermission()) {
                if (!hasStartedAuth) {
                  showAuthenticationActivity(paymentMethod, true)
                  hasStartedAuth = true
                }
              } else {
                if (paymentMethod.id == PaymentMethodId.CREDIT_CARD.id) {
                  view.showAdyen(
                      fiatValue.amount, fiatValue.currency, PaymentType.CARD,
                      paymentMethod.iconUrl, cachedGamificationLevel, paymentMethodsData.frequency,
                      paymentMethodsData.subscription
                  )
                } else if (paymentMethod.id == PaymentMethodId.CARRIER_BILLING.id) {
                  view.showCarrierBilling(fiatValue, true)
                }
              }
            }
          }
          else -> showPreSelectedPaymentMethod(fiatValue, paymentMethod, fiatAmount, appcAmount,
              isBonusActive, paymentMethodsData.frequency)
        }
      }
    } else if (paymentMethods.size == 1
        && paymentMethods[0].id == PaymentMethodId.APPC_CREDITS.id
        && paymentMethods[0].isEnabled) {
      showPaymentMethods(
          fiatValue, paymentMethods, paymentMethods[0].id, fiatAmount, appcAmount,
          paymentMethodsData.frequency
      )
    } else {
      val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
      showPaymentMethods(fiatValue, paymentMethods, paymentMethodId, fiatAmount, appcAmount,
          paymentMethodsData.frequency)
    }
  }

  private fun getCreditsPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    paymentMethods.forEach {
      if (it.id == PaymentMethodId.MERGED_APPC.id) {
        val mergedPaymentMethod = it as AppCoinsPaymentMethod
        return PaymentMethod(
            PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
            mergedPaymentMethod.creditsLabel, mergedPaymentMethod.iconUrl,
            mergedPaymentMethod.async, mergedPaymentMethod.fee,
            mergedPaymentMethod.isCreditsEnabled
        )
      }
      if (it.id == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id) {
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
      fiatValue: FiatValue, paymentMethods: List<PaymentMethod>,
      paymentMethodId: String, fiatAmount: String, appcAmount: String,
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
          .filter { it.id == paymentMethodsMapper.map(APPC) }
          .toMutableList()
    }
    view.showPaymentMethods(
        paymentList, symbol, paymentMethodId, fiatAmount, appcAmount,
        appcEnabled, creditsEnabled, frequency, paymentMethodsData.subscription
    )
    sendPaymentMethodsEvents()
  }

  private fun showPreSelectedPaymentMethod(
      fiatValue: FiatValue, paymentMethod: PaymentMethod,
      fiatAmount: String, appcAmount: String,
      isBonusActive: Boolean, frequency: String?
  ) {
    view.showPreSelectedPaymentMethod(
        paymentMethod, mapCurrencyCodeToSymbol(fiatValue.currency),
        fiatAmount, appcAmount, isBonusActive, frequency, paymentMethodsData.subscription
    )
    sendPreSelectedPaymentMethodsEvents()
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode).currencyCode
  }

  private fun handleCancelClick() {
    disposables.add(view.getCancelClick()
        .map { view.getSelectedPaymentMethod(interactor.hasPreSelectedPaymentMethod()) }
        .observeOn(networkThread)
        .doOnNext { sendCancelPaymentMethodAnalytics(it) }
        .subscribe { close() })
  }

  private fun sendCancelPaymentMethodAnalytics(paymentMethod: PaymentMethod) {
    analytics.sendPaymentMethodEvent(
        paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), paymentMethod.id, transaction.type, "cancel",
        interactor.hasPreSelectedPaymentMethod()
    )

  }

  private fun handleMorePaymentMethodClicks() {
    disposables.add(view.getMorePaymentMethodsClicks()
        .map { view.getSelectedPaymentMethod(interactor.hasPreSelectedPaymentMethod()) }
        .observeOn(networkThread)
        .doOnNext { selectedPaymentMethod ->
          analytics.sendPaymentMethodEvent(
              paymentMethodsData.appPackage,
              transaction.skuId,
              transaction.amount()
                  .toString(), selectedPaymentMethod.id, transaction.type, "other_payments"
          )
        }
        .observeOn(viewScheduler)
        .doOnEach { view.showSkeletonLoading() }
        .flatMapSingle {
          if (cachedFiatValue == null) {
            getPurchaseFiatValue().subscribeOn(networkThread)
          } else {
            Single.just(cachedFiatValue)
          }
        }
        .flatMapCompletable { fiatValue ->
          cachedFiatValue = fiatValue
          getPaymentMethods(fiatValue).subscribeOn(networkThread)
              .observeOn(viewScheduler)
              .flatMapCompletable { paymentMethods ->
                Completable.fromAction {
                  val fiatAmount =
                      formatter.formatPaymentCurrency(fiatValue.amount, WalletCurrency.FIAT)
                  val appcAmount =
                      formatter.formatPaymentCurrency(transaction.amount(), WalletCurrency.APPCOINS)
                  val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
                  showPaymentMethods(
                      fiatValue, paymentMethods, paymentMethodId, fiatAmount,
                      appcAmount, paymentMethodsData.frequency
                  )
                }
              }
              .andThen(
                  Completable.fromAction { interactor.removePreSelectedPaymentMethod() })
              .andThen(Completable.fromAction { interactor.removeAsyncLocalPayment() })
              .andThen(Completable.fromAction { view.hideLoading() })
        }
        .subscribe({ }, { this.showError(it) })
    )
  }

  private fun showError(@StringRes message: Int) {
    if (viewState != ViewState.ITEM_ALREADY_OWNED) {
      viewState = ViewState.ERROR
      view.showError(message)
    }
  }

  private fun isItemAlreadyOwnedError(throwable: Throwable): Boolean {
    return throwable is HttpException && throwable.code() == 409
  }

  private fun close() = view.close(paymentMethodsMapper.mapCancellation())

  private fun handleErrorDismisses() {
    disposables.add(Observable.merge(view.errorDismisses(), view.onBackPressed())
        .flatMapCompletable {
          if (viewState == ViewState.ITEM_ALREADY_OWNED) {
            val type = BillingSupportedType.valueOfInsensitive(transaction.type)
            getPurchases(type).doOnSuccess { purchases ->
              val purchase = getRequestedSkuPurchase(purchases, transaction.skuId)
              purchase?.let { finishItemAlreadyOwned(it) } ?: view.close(Bundle())
            }
                .ignoreElement()
          } else {
            return@flatMapCompletable Completable.fromAction { view.close(Bundle()) }
          }
        }
        .subscribe({ }, { view.close(Bundle()) })
    )
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { interactor.showSupport(cachedGamificationLevel) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun finishItemAlreadyOwned(purchase: Purchase) {
    view.finish(paymentMethodsMapper.mapFinishedPurchase(purchase, true))
  }

  private fun sendPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(
        paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), transaction.type
    )
  }

  private fun sendPreSelectedPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(
        paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), transaction.type
    )
  }

  fun stop() = disposables.clear()

  private fun getPaymentMethods(fiatValue: FiatValue): Single<List<PaymentMethod>> {
    return if (paymentMethodsData.isBds) {
      interactor.getPaymentMethods(
          transaction, fiatValue.amount.toString(),
          fiatValue.currency
      )
          .map { interactor.mergeAppcoins(it) }
          .map { interactor.swapDisabledPositions(it) }
          .doOnSuccess { updateBalanceDao() }
    } else {
      Single.just(listOf(PaymentMethod.APPC))
    }
  }

  //Updates database with the latest balance to take less time loading the merged appcoins view
  private fun updateBalanceDao() {
    disposables.add(
        Observable.zip(interactor.getEthBalance(),
            interactor.getCreditsBalance(),
            interactor.getAppcBalance(), Function3 { _: Any, _: Any, _: Any -> })
            .take(1)
            .subscribeOn(networkThread)
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun getPreSelectedPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    val preSelectedPreference = interactor.getPreSelectedPaymentMethod()
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == PaymentMethodId.MERGED_APPC.id) {
        if (preSelectedPreference == PaymentMethodId.APPC.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(
              PaymentMethodsView.PaymentMethodId.APPC.id,
              mergedPaymentMethod.appcLabel, mergedPaymentMethod.iconUrl, mergedPaymentMethod.async,
              mergedPaymentMethod.fee, mergedPaymentMethod.isAppcEnabled
          )
        }
        if (preSelectedPreference == PaymentMethodId.APPC_CREDITS.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(
              PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
              mergedPaymentMethod.creditsLabel, paymentMethod.creditsIconUrl,
              mergedPaymentMethod.async, mergedPaymentMethod.fee,
              mergedPaymentMethod.isCreditsEnabled
          )
        }
      }
      if (paymentMethod.id == preSelectedPreference) return paymentMethod
    }
    return null
  }

  private fun getLastUsedPaymentMethod(paymentMethods: List<PaymentMethod>): String {
    val lastUsedPaymentMethod = interactor.getLastUsedPaymentMethod()
    for (it in paymentMethods) {
      if (it.isEnabled) {
        if (it.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id &&
            (lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC.id ||
                lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id)
        ) {
          return PaymentMethodsView.PaymentMethodId.MERGED_APPC.id
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
      paymentMethodsMapper.map(EARN_APPC) -> view.replaceBonus()
      paymentMethodsMapper.map(MERGED_APPC) -> view.hideBonus()
      paymentMethodsMapper.map(APPC_CREDITS) -> view.hideBonus()
      else -> if (paymentMethodsData.subscription) {
        view.showBonus(R.string.subscriptions_bonus_body)
      } else {
        view.showBonus(R.string.gamification_purchase_body)
      }
    }
  }

  private fun handlePositiveButtonText(selectedPaymentMethod: String) {
    if (selectedPaymentMethod == paymentMethodsMapper.map(
            MERGED_APPC) || selectedPaymentMethod == paymentMethodsMapper.map(EARN_APPC)) {
      view.showNext()
    } else {
      if (paymentMethodsData.subscription) {
        view.showSubscribe()
      } else {
        view.showBuy()
      }
    }
  }

  private fun handleBuyAnalytics(selectedPaymentMethod: PaymentMethod) {
    val action =
        if (selectedPaymentMethod.id == PaymentMethodId.MERGED_APPC.id) "next" else "buy"
    if (interactor.hasPreSelectedPaymentMethod()) {
      analytics.sendPaymentMethodEvent(
          paymentMethodsData.appPackage, transaction.skuId,
          transaction.amount()
              .toString(), selectedPaymentMethod.id, transaction.type, action
      )
    } else {
      analytics.sendPaymentMethodEvent(
          paymentMethodsData.appPackage, transaction.skuId,
          transaction.amount()
              .toString(), selectedPaymentMethod.id, transaction.type, action
      )
    }
  }

  private fun getPurchases(type: BillingSupportedType): Single<List<Purchase>> {
    return interactor.getPurchases(
        paymentMethodsData.appPackage, type, networkThread
    )
  }

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

  private fun isRequestedSkuPurchase(purchase: Purchase, sku: String?): Boolean {
    return purchase.product.name == sku &&
        purchase.state != State.CONSUMED && purchase.state != State.ACKNOWLEDGED
  }

  private fun showAuthenticationActivity(paymentMethod: PaymentMethod, isPreselected: Boolean) {
    cachedPaymentNavigationData =
        PaymentNavigationData(
            paymentMethod.id, paymentMethod.label, paymentMethod.iconUrl,
            paymentMethod.async, isPreselected
        )
    view.showAuthenticationActivity()
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
    return interactor.getSkuDetails(paymentMethodsData.appPackage, paymentMethodsData.sku,
        billingSupportedType)
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
            zip(
                interactor.convertCurrencyToLocalFiat(
                    getOriginalValue().toDouble(),
                    getOriginalCurrency()
                ),
                setTransactionAppcValue(transaction),
                { fiatValue, _ -> fiatValue }
            ))
  }

  private fun getOriginalValue(): BigDecimal {
    return if (transaction.originalOneStepValue.isNullOrEmpty()) {
      transaction.amount()
    } else {
      BigDecimal(transaction.originalOneStepValue)
    }
  }

  private fun getOriginalCurrency(): String {
    return if (transaction.originalOneStepCurrency.isNullOrEmpty()) {
      "APPC"
    } else {
      transaction.originalOneStepCurrency
    }
  }

  private fun setTransactionAppcValue(transaction: TransactionBuilder): Single<FiatValue> {
    return if (transaction.amount()
            .compareTo(BigDecimal.ZERO) == 0) {
      interactor.convertCurrencyToAppc(getOriginalValue().toDouble(), getOriginalCurrency())
          .map { appcValue ->
            transaction.amount(appcValue.amount)
            appcValue
          }
    } else {
      Single.just(FiatValue(transaction.amount(), "APPC"))
    }
  }

  enum class ViewState {
    DEFAULT, ITEM_ALREADY_OWNED, ERROR
  }
}

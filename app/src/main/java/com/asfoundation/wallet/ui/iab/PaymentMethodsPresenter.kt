package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.State
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit

class PaymentMethodsPresenter(private val view: PaymentMethodsView,
                              private val viewScheduler: Scheduler,
                              private val networkThread: Scheduler,
                              private val disposables: CompositeDisposable,
                              private val analytics: PaymentMethodsAnalytics,
                              private val transaction: TransactionBuilder,
                              private val paymentMethodsMapper: PaymentMethodsMapper,
                              private val formatter: CurrencyFormatUtils,
                              private val logger: Logger,
                              private val interactor: PaymentMethodsInteractor,
                              private val paymentMethodsData: PaymentMethodsData) {

  private var cachedGamificationLevel = 0
  private var cachedFiatValue: FiatValue? = null
  private var cachedPaymentNavigationData: PaymentNavigationData? = null
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
    if (paymentMethodsData.isBds) handlePaymentSelection()
  }

  fun onResume(firstRun: Boolean) {
    if (firstRun.not()) view.showPaymentsSkeletonLoading()
    setupUi(firstRun)
  }

  private fun handlePaymentSelection() {
    disposables.add(view.getPaymentSelection()
        .observeOn(viewScheduler)
        .doOnNext { selectedPaymentMethod ->
          if (interactor.isBonusActiveAndValid()) {
            handleBonusVisibility(selectedPaymentMethod)
          }
          handlePositiveButtonText(selectedPaymentMethod)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBuyClick() {
    disposables.add(view.getBuyClick()
        .map { view.getSelectedPaymentMethod(interactor.hasPreSelectedPaymentMethod()) }
        .observeOn(viewScheduler)
        .doOnNext { handleBuyAnalytics(it) }
        .doOnNext { selectedPaymentMethod ->
          when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
            EARN_APPC -> view.showEarnAppcoins()
            APPC_CREDITS -> {
              view.showProgressBarLoading()
              handleWalletBlockStatus(selectedPaymentMethod)
            }
            MERGED_APPC -> view.showMergedAppcoins(cachedGamificationLevel,
                cachedFiatValue!!, paymentMethodsData.frequency, paymentMethodsData.subscription)

            else -> {
              if (interactor.hasAuthenticationPermission()) {
                showAuthenticationActivity(selectedPaymentMethod,
                    interactor.hasPreSelectedPaymentMethod())
              } else {
                when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
                  PAYPAL -> view.showPaypal(cachedGamificationLevel,
                      cachedFiatValue!!, paymentMethodsData.frequency,
                      paymentMethodsData.subscription)
                  CREDIT_CARD -> view.showCreditCard(cachedGamificationLevel,
                      cachedFiatValue!!, paymentMethodsData.frequency,
                      paymentMethodsData.subscription)
                  APPC -> view.showAppCoins(cachedGamificationLevel)
                  SHARE_LINK -> view.showShareLink(selectedPaymentMethod.id)
                  LOCAL_PAYMENTS -> view.showLocalPayment(
                      selectedPaymentMethod.id, selectedPaymentMethod.iconUrl,
                      selectedPaymentMethod.label, selectedPaymentMethod.async,
                      cachedFiatValue!!.amount.toString(), cachedFiatValue!!.currency,
                      cachedGamificationLevel)
                  CARRIER_BILLING -> view.showCarrierBilling(
                      cachedFiatValue!!, false)
                  else -> return@doOnNext
                }
              }
            }
          }
        }
        .retry()
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.onAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          if (cachedPaymentNavigationData == null) close()
          else if (!it) {
            hasStartedAuth = false
            if (cachedPaymentNavigationData!!.isPreselected &&
                hasPaymentOwnPreselectedView(cachedPaymentNavigationData!!.paymentId)) {
              close()
            }
          } else {
            navigateToPayment(cachedPaymentNavigationData!!)
          }
        }
        .subscribe({}, { it.printStackTrace() }))
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
            showAuthenticationActivity(selectedPaymentMethod,
                interactor.hasPreSelectedPaymentMethod())
          } else {
            view.showCredits(cachedGamificationLevel)
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
          .subscribe({}, { it.printStackTrace() }))
      return
    }
    disposables.add(waitForUi(transaction.skuId, billingSupportedType)
        .observeOn(viewScheduler)
        .doOnComplete { view.hideLoading() }
        .subscribe({ }, { showError(it) }))
  }

  private fun navigateToPayment(paymentNavigationData: PaymentNavigationData) {
    when (paymentMethodsMapper.map(paymentNavigationData.paymentId)) {
      PAYPAL -> view.showPaypal(cachedGamificationLevel, cachedFiatValue!!,
          paymentMethodsData.frequency, paymentMethodsData.subscription)
      CREDIT_CARD -> {
        if (paymentNavigationData.isPreselected) {
          view.showAdyen(cachedFiatValue!!.amount, cachedFiatValue!!.currency, PaymentType.CARD,
              paymentNavigationData.paymentIconUrl, cachedGamificationLevel,
              paymentMethodsData.frequency, paymentMethodsData.subscription)
        } else view.showCreditCard(cachedGamificationLevel, cachedFiatValue!!,
            paymentMethodsData.frequency, paymentMethodsData.subscription)
      }
      APPC -> view.showAppCoins(cachedGamificationLevel)
      APPC_CREDITS -> view.showCredits(cachedGamificationLevel)
      SHARE_LINK -> view.showShareLink(paymentNavigationData.paymentId)
      LOCAL_PAYMENTS -> {
        view.showLocalPayment(paymentNavigationData.paymentId, paymentNavigationData.paymentIconUrl,
            paymentNavigationData.paymentLabel, paymentNavigationData.async,
            cachedFiatValue!!.amount.toString(), cachedFiatValue!!.currency,
            cachedGamificationLevel)
      }
      CARRIER_BILLING -> {
        view.showCarrierBilling(cachedFiatValue!!, paymentNavigationData.isPreselected)
      }
      else -> {
        view.showError(R.string.unknown_error)
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
    return Completable.mergeArray(checkProcessing(skuId, type).subscribeOn(networkThread),
        checkAndConsumePrevious(skuId, type).subscribeOn(networkThread),
        checkSubscriptionOwned(skuId, type).subscribeOn(networkThread),
        isSetupCompleted())
  }

  private fun checkSubscriptionOwned(skuId: String?, type: BillingSupportedType): Completable {
    return if (type == BillingSupportedType.INAPP_SUBSCRIPTION && skuId != null) {
      interactor.isAbleToSubscribe(paymentMethodsData.appPackage, skuId, networkThread)
          .subscribeOn(networkThread)
          .observeOn(viewScheduler)
          .doOnSuccess { handleSubscriptionAvailability(it) }
          .ignoreElement()
    } else {
      Completable.complete()
    }
  }

  private fun handleSubscriptionAvailability(status: SubscriptionStatus) {
    if (status.isAvailable.not()) {
      if (status.isAlreadySubscribed) {
        view.showError(R.string.purchase_error_incomplete_transaction_body)
      } else {
        view.showError(R.string.unknown_error)
      }
    }
  }

  private fun checkProcessing(skuId: String?, type: BillingSupportedType): Completable {
    return interactor.getSkuTransaction(paymentMethodsData.appPackage, skuId,
        networkThread, type)
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
                  finishProcess(skuId, it.orderReference, it.hash, it.metadata?.purchaseUid))
        }
  }

  private fun handleProcessing() {
    disposables.add(
        interactor.getCurrentPaymentStep(paymentMethodsData.appPackage, transaction)
            .filter { currentPaymentStep -> currentPaymentStep == AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_ON_CHAIN }
            .doOnSuccess {
              view.lockRotation()
              interactor.resume(paymentMethodsData.uri,
                  AsfInAppPurchaseInteractor.TransactionType.NORMAL,
                  paymentMethodsData.appPackage, transaction.skuId,
                  paymentMethodsData.developerPayload, paymentMethodsData.isBds, transaction.type)
            }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun finishProcess(skuId: String?, orderReference: String?,
                            hash: String?, purchaseUid: String?): Completable {
    return interactor.getSkuPurchase(paymentMethodsData.appPackage, skuId, purchaseUid,
        transaction.type, orderReference, hash, networkThread)
        .observeOn(viewScheduler)
        .doOnSuccess { bundle -> view.finish(bundle.bundle) }
        .ignoreElement()
  }

  private fun checkAndConsumePrevious(sku: String?, type: BillingSupportedType): Completable {
    return getPurchases(type)
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .flatMapCompletable { purchases ->
          Completable.fromAction {
            if (hasRequestedSkuPurchase(purchases, sku)) view.showItemAlreadyOwnedError()
          }
        }
  }

  private fun setupUi(firstRun: Boolean) {
    disposables.add(
        interactor.convertToLocalFiat(paymentMethodsData.transactionValue.toDouble())
            .subscribeOn(networkThread)
            .flatMapCompletable { fiatValue ->
              this.cachedFiatValue = fiatValue
              getPaymentMethods(fiatValue)
                  .flatMapCompletable { paymentMethods ->
                    interactor.getEarningBonus(transaction.domain, transaction.amount())
                        .observeOn(viewScheduler)
                        .flatMapCompletable {
                          Completable.fromAction {
                            setupBonusInformation(it)
                            selectPaymentMethod(paymentMethods, fiatValue,
                                interactor.isBonusActiveAndValid(it))
                          }
                        }
                  }
            }
            .subscribeOn(networkThread)
            .observeOn(viewScheduler)
            .doOnComplete {
              //If not first run we should rely on the hideLoading of the handleOnGoingPurchases method
              if (!firstRun) view.hideLoading()
            }
            .subscribe({ }, { this.showError(it) }))
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

  private fun selectPaymentMethod(paymentMethods: List<PaymentMethod>, fiatValue: FiatValue,
                                  isBonusActive: Boolean) {
    val fiatAmount = formatter.formatCurrency(fiatValue.amount, WalletCurrency.FIAT)
    val appcAmount = formatter.formatCurrency(transaction.amount(), WalletCurrency.APPCOINS)
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
        showPaymentMethods(fiatValue, paymentMethods,
            PaymentMethodId.CREDIT_CARD.id, fiatAmount, appcAmount,
            paymentMethodsData.frequency)
      } else {
        when (paymentMethod.id) {
          PaymentMethodId.CARRIER_BILLING.id,
          PaymentMethodId.CREDIT_CARD.id -> {
            analytics.sendPurchaseDetailsEvent(paymentMethodsData.appPackage, transaction.skuId,
                transaction.amount()
                    .toString(), transaction.type)
            if (interactor.hasAuthenticationPermission()) {
              if (!hasStartedAuth) {
                showAuthenticationActivity(paymentMethod, true)
                hasStartedAuth = true
              }
            } else {
              if (paymentMethod.id == PaymentMethodId.CREDIT_CARD.id) {
                view.showAdyen(fiatValue.amount, fiatValue.currency, PaymentType.CARD,
                    paymentMethod.iconUrl, cachedGamificationLevel, paymentMethodsData.frequency,
                    paymentMethodsData.subscription)
              } else if (paymentMethod.id == PaymentMethodId.CARRIER_BILLING.id) {
                view.showCarrierBilling(fiatValue, true)
              }

            }
          }
          else -> showPreSelectedPaymentMethod(fiatValue, paymentMethod, fiatAmount, appcAmount,
              isBonusActive, paymentMethodsData.frequency)
        }
      }
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
        return PaymentMethod(PaymentMethodId.APPC_CREDITS.id,
            mergedPaymentMethod.creditsLabel, mergedPaymentMethod.iconUrl,
            mergedPaymentMethod.async, mergedPaymentMethod.fee,
            mergedPaymentMethod.isCreditsEnabled)
      }
      if (it.id == PaymentMethodId.APPC_CREDITS.id) {
        return it
      }
    }

    return null
  }

  private fun showPaymentMethods(fiatValue: FiatValue, paymentMethods: List<PaymentMethod>,
                                 paymentMethodId: String, fiatAmount: String, appcAmount: String,
                                 frequency: String?) {
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
    view.showPaymentMethods(paymentList, symbol, paymentMethodId, fiatAmount, appcAmount,
        appcEnabled, creditsEnabled, frequency, paymentMethodsData.subscription)
    sendPaymentMethodsEvents()
  }

  private fun showPreSelectedPaymentMethod(fiatValue: FiatValue, paymentMethod: PaymentMethod,
                                           fiatAmount: String, appcAmount: String,
                                           isBonusActive: Boolean, frequency: String?) {
    view.showPreSelectedPaymentMethod(paymentMethod, mapCurrencyCodeToSymbol(fiatValue.currency),
        fiatAmount, appcAmount, isBonusActive, frequency, paymentMethodsData.subscription)
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
    analytics.sendPaymentMethodEvent(paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), paymentMethod.id, transaction.type, "cancel",
        interactor.hasPreSelectedPaymentMethod())

  }

  private fun handleMorePaymentMethodClicks() {
    disposables.add(view.getMorePaymentMethodsClicks()
        .map { view.getSelectedPaymentMethod(interactor.hasPreSelectedPaymentMethod()) }
        .observeOn(networkThread)
        .doOnNext { selectedPaymentMethod ->
          analytics.sendPaymentMethodEvent(paymentMethodsData.appPackage,
              transaction.skuId,
              transaction.amount()
                  .toString(), selectedPaymentMethod.id, transaction.type, "other_payments")
        }
        .observeOn(viewScheduler)
        .doOnEach { view.showSkeletonLoading() }
        .flatMapSingle {
          if (cachedFiatValue == null) {
            interactor.convertToLocalFiat(paymentMethodsData.transactionValue.toDouble())
                .subscribeOn(networkThread)
          } else {
            Single.just(cachedFiatValue)
          }
        }
        .flatMapCompletable { fiatValue ->
          getPaymentMethods(fiatValue).subscribeOn(networkThread)
              .observeOn(viewScheduler)
              .flatMapCompletable { paymentMethods ->
                Completable.fromAction {
                  val fiatAmount = formatter.formatCurrency(fiatValue.amount, WalletCurrency.FIAT)
                  val appcAmount = formatter.formatCurrency(transaction.amount(),
                      WalletCurrency.APPCOINS)
                  val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
                  showPaymentMethods(fiatValue, paymentMethods, paymentMethodId, fiatAmount,
                      appcAmount, paymentMethodsData.frequency)
                }
              }
              .andThen(
                  Completable.fromAction { interactor.removePreSelectedPaymentMethod() })
              .andThen(Completable.fromAction { interactor.removeAsyncLocalPayment() })
              .andThen(Completable.fromAction { view.hideLoading() })
        }
        .subscribe({ }, { this.showError(it) }))
  }

  private fun showError(t: Throwable) {
    t.printStackTrace()
    logger.log(TAG, t)
    when {
      t.isNoNetworkException() -> view.showError(R.string.notification_no_network_poa)
      isItemAlreadyOwnedError(t) -> view.showItemAlreadyOwnedError()
      else -> view.showError(R.string.activity_iab_error_message)
    }
  }

  private fun isItemAlreadyOwnedError(throwable: Throwable): Boolean {
    return throwable is HttpException && throwable.code() == 409
  }

  private fun close() = view.close(paymentMethodsMapper.mapCancellation())

  private fun handleErrorDismisses() {
    disposables.add(Observable.merge(view.errorDismisses(), view.onBackPressed())
        .flatMapCompletable { itemAlreadyOwned ->
          if (itemAlreadyOwned) {
            val type = BillingSupportedType.valueOfInsensitive(transaction.type)
            getPurchases(type).doOnSuccess { purchases ->
              val purchase = getRequestedSkuPurchase(purchases, transaction.skuId)
              purchase?.let { finish(it, itemAlreadyOwned) } ?: view.close(Bundle())
            }
                .ignoreElement()
          } else {
            return@flatMapCompletable Completable.fromAction { view.close(Bundle()) }
          }
        }
        .subscribe({ }, { view.close(Bundle()) }))
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { interactor.showSupport(cachedGamificationLevel) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun finish(purchase: Purchase, itemAlreadyOwned: Boolean) {
    view.finish(paymentMethodsMapper.mapFinishedPurchase(purchase, itemAlreadyOwned))
  }

  private fun sendPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), transaction.type)
  }

  private fun sendPreSelectedPaymentMethodsEvents() {
    analytics.sendPurchaseDetailsEvent(paymentMethodsData.appPackage, transaction.skuId,
        transaction.amount()
            .toString(), transaction.type)
  }

  fun stop() = disposables.clear()

  private fun getPaymentMethods(fiatValue: FiatValue): Single<List<PaymentMethod>> {
    return if (paymentMethodsData.isBds) {
      interactor.getPaymentMethods(transaction, fiatValue.amount.toString(),
          fiatValue.currency)
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
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun getPreSelectedPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    val preSelectedPreference = interactor.getPreSelectedPaymentMethod()
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == PaymentMethodId.MERGED_APPC.id) {
        if (preSelectedPreference == PaymentMethodId.APPC.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodId.APPC.id,
              mergedPaymentMethod.appcLabel, mergedPaymentMethod.iconUrl, mergedPaymentMethod.async,
              mergedPaymentMethod.fee, mergedPaymentMethod.isAppcEnabled)
        }
        if (preSelectedPreference == PaymentMethodId.APPC_CREDITS.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodId.APPC_CREDITS.id,
              mergedPaymentMethod.creditsLabel, paymentMethod.creditsIconUrl,
              mergedPaymentMethod.async, mergedPaymentMethod.fee,
              mergedPaymentMethod.isCreditsEnabled)
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
        if (it.id == PaymentMethodId.MERGED_APPC.id &&
            (lastUsedPaymentMethod == PaymentMethodId.APPC.id ||
                lastUsedPaymentMethod == PaymentMethodId.APPC_CREDITS.id)) {
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
    if (isMergedAppCoins(selectedPaymentMethod)) {
      view.showNext()
    } else {
      if (paymentMethodsData.subscription) {
        view.showSubscribe()
      } else {
        view.showBuy()
      }
    }
  }

  private fun isMergedAppCoins(selectedPaymentMethod: String): Boolean {
    return selectedPaymentMethod == paymentMethodsMapper.map(MERGED_APPC)
        || selectedPaymentMethod == paymentMethodsMapper.map(EARN_APPC)
  }

  private fun handleBuyAnalytics(selectedPaymentMethod: PaymentMethod) {
    val action =
        if (selectedPaymentMethod.id == PaymentMethodId.MERGED_APPC.id) "next" else "buy"
    if (interactor.hasPreSelectedPaymentMethod()) {
      analytics.sendPaymentMethodEvent(paymentMethodsData.appPackage, transaction.skuId,
          transaction.amount()
              .toString(), selectedPaymentMethod.id, transaction.type, action)
    } else {
      analytics.sendPaymentMethodEvent(paymentMethodsData.appPackage, transaction.skuId,
          transaction.amount()
              .toString(), selectedPaymentMethod.id, transaction.type, action)
    }
  }

  private fun getPurchases(type: BillingSupportedType): Single<List<Purchase>> {
    return interactor.getPurchases(paymentMethodsData.appPackage,
        type, networkThread)
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
        PaymentNavigationData(paymentMethod.id, paymentMethod.label, paymentMethod.iconUrl,
            paymentMethod.async, isPreselected)
    view.showAuthenticationActivity()
  }

  fun onSavedInstance(outState: Bundle) {
    outState.putInt(GAMIFICATION_LEVEL, cachedGamificationLevel)
    outState.putBoolean(HAS_STARTED_AUTH, hasStartedAuth)
    outState.putSerializable(FIAT_VALUE, cachedFiatValue)
    outState.putSerializable(PAYMENT_NAVIGATION_DATA, cachedPaymentNavigationData)
  }
}

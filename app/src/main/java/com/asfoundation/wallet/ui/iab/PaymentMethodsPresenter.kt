package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.gamification.repository.ForecastBonusAndLevel
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.PaymentNavigationData
import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod
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
            SelectedPaymentMethod.EARN_APPC -> view.showEarnAppcoins()
            SelectedPaymentMethod.APPC_CREDITS -> {
              view.showProgressBarLoading()
              handleWalletBlockStatus(selectedPaymentMethod)
            }
            SelectedPaymentMethod.MERGED_APPC -> view.showMergedAppcoins(cachedGamificationLevel,
                cachedFiatValue!!)

            else -> {
              if (interactor.hasAuthenticationPermission()) {
                showAuthenticationActivity(selectedPaymentMethod,
                    interactor.hasPreSelectedPaymentMethod())
              } else {
                when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
                  SelectedPaymentMethod.PAYPAL -> view.showPaypal(cachedGamificationLevel,
                      cachedFiatValue!!)
                  SelectedPaymentMethod.CREDIT_CARD -> view.showCreditCard(cachedGamificationLevel,
                      cachedFiatValue!!)
                  SelectedPaymentMethod.APPC -> view.showAppCoins(cachedGamificationLevel)
                  SelectedPaymentMethod.SHARE_LINK -> view.showShareLink(selectedPaymentMethod.id)
                  SelectedPaymentMethod.LOCAL_PAYMENTS -> view.showLocalPayment(
                      selectedPaymentMethod.id, selectedPaymentMethod.iconUrl,
                      selectedPaymentMethod.label, cachedGamificationLevel)
                  else -> return@doOnNext
                }
              }
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.onAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          if (cachedPaymentNavigationData == null) close()
          else if (!it) {
            hasStartedAuth = false
            if (cachedPaymentNavigationData!!.isPreselected && paymentMethodsMapper.map(
                    cachedPaymentNavigationData!!.paymentId) == SelectedPaymentMethod.CREDIT_CARD) {
              close()
            }
          } else {
            navigateToPayment(cachedPaymentNavigationData!!)
          }
        }
        .subscribe({}, { it.printStackTrace() }))
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
    if (transaction.skuId == null) {
      disposables.add(isSetupCompleted()
          .doOnComplete { view.hideLoading() }
          .subscribeOn(viewScheduler)
          .subscribe({}, { it.printStackTrace() }))
      return
    }
    val billingSupportedType =
        transaction.type?.let { BillingSupportedType.valueOfInsensitive(it) }
            ?: BillingSupportedType.INAPP
    disposables.add(waitForUi(transaction.skuId, billingSupportedType)
        .observeOn(viewScheduler)
        .doOnComplete { view.hideLoading() }
        .subscribe({ }, { showError(it) }))
  }

  private fun navigateToPayment(paymentNavigationData: PaymentNavigationData) {
    when (paymentMethodsMapper.map(paymentNavigationData.paymentId)) {
      SelectedPaymentMethod.PAYPAL -> view.showPaypal(cachedGamificationLevel, cachedFiatValue!!)
      SelectedPaymentMethod.CREDIT_CARD -> {
        if (paymentNavigationData.isPreselected) {
          view.showAdyen(cachedFiatValue!!.amount, cachedFiatValue!!.currency, PaymentType.CARD,
              paymentNavigationData.paymentIconUrl, cachedGamificationLevel)
        } else view.showCreditCard(cachedGamificationLevel, cachedFiatValue!!)
      }
      SelectedPaymentMethod.APPC -> view.showAppCoins(cachedGamificationLevel)
      SelectedPaymentMethod.APPC_CREDITS -> view.showCredits(cachedGamificationLevel)
      SelectedPaymentMethod.SHARE_LINK -> view.showShareLink(paymentNavigationData.paymentId)
      SelectedPaymentMethod.LOCAL_PAYMENTS -> {
        view.showLocalPayment(paymentNavigationData.paymentId, paymentNavigationData.paymentIconUrl,
            paymentNavigationData.paymentLabel, cachedGamificationLevel)
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
    return Completable.mergeArray(checkProcessing(skuId, type),
        checkAndConsumePrevious(skuId, type),
        isSetupCompleted())
  }

  private fun checkProcessing(skuId: String?,type: BillingSupportedType): Completable {
    return interactor.getSkuTransaction(paymentMethodsData.appPackage, skuId,
        networkThread, type)
        .subscribeOn(networkThread)
        .filter { (_, status) -> status === Transaction.Status.PROCESSING }
        .observeOn(viewScheduler)
        .doOnSuccess { view.showProcessingLoadingDialog() }
        .doOnSuccess { handleProcessing() }
        .map { it.uid }
        .observeOn(networkThread)
        .flatMapCompletable { uid ->
          interactor.checkTransactionStateFromTransactionId(uid)
              .ignoreElements()
              .andThen(finishProcess(skuId, type))
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

  private fun finishProcess(skuId: String?, billingType: BillingSupportedType): Completable {
    return interactor.getSkuPurchase(paymentMethodsData.appPackage, skuId,
        networkThread, billingType)
        .observeOn(viewScheduler)
        .doOnSuccess { purchase -> finish(purchase, false) }
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
                                interactor.isBonusActiveAndValid(it), paymentMethodsData.frequency)
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
      if (paymentMethodsData.isSubscription) {
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
                                  isBonusActive: Boolean, frequency: String?) {
    val fiatAmount = formatter.formatCurrency(fiatValue.amount, WalletCurrency.FIAT)
    val appcAmount = formatter.formatCurrency(transaction.amount(), WalletCurrency.APPCOINS)
    if (interactor.hasAsyncLocalPayment()) {
      //After a asynchronous payment credits will be used as pre selected
      getCreditsPaymentMethod(paymentMethods)?.let {
        if (it.isEnabled) {
          showPreSelectedPaymentMethod(fiatValue, it, fiatAmount, appcAmount, isBonusActive,
              frequency)
          return
        }
      }
    }

    if (interactor.hasPreSelectedPaymentMethod()) {
      val paymentMethod = getPreSelectedPaymentMethod(paymentMethods)
      if (paymentMethod == null || !paymentMethod.isEnabled) {
        showPaymentMethods(fiatValue, paymentMethods,
            PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id, fiatAmount, appcAmount, frequency)
      } else {
        when (paymentMethod.id) {
          PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id -> {
            analytics.sendPurchaseDetailsEvent(paymentMethodsData.appPackage, transaction.skuId,
                transaction.amount()
                    .toString(), transaction.type)
            if (interactor.hasAuthenticationPermission()) {
              if (!hasStartedAuth) {
                showAuthenticationActivity(paymentMethod, true)
                hasStartedAuth = true
              }
            } else {
              view.showAdyen(fiatValue.amount, fiatValue.currency, PaymentType.CARD,
                  paymentMethod.iconUrl, cachedGamificationLevel)
            }
          }
          else -> showPreSelectedPaymentMethod(fiatValue, paymentMethod, fiatAmount, appcAmount,
              isBonusActive, frequency)
        }
      }
    } else {
      val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
      showPaymentMethods(fiatValue, paymentMethods, paymentMethodId, fiatAmount, appcAmount,
          frequency)
    }
  }

  private fun getCreditsPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    paymentMethods.forEach {
      if (it.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id) {
        val mergedPaymentMethod = it as AppCoinsPaymentMethod
        return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
            mergedPaymentMethod.creditsLabel, mergedPaymentMethod.iconUrl, mergedPaymentMethod.fee,
            mergedPaymentMethod.isCreditsEnabled)
      }
      if (it.id == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id) {
        return it
      }
    }

    return null
  }

  private fun showPaymentMethods(fiatValue: FiatValue, paymentMethods: List<PaymentMethod>,
                                 paymentMethodId: String, fiatAmount: String, appcAmount: String, frequency: String?) {
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
          .filter {
            it.id == paymentMethodsMapper.map(SelectedPaymentMethod.APPC)
          }
          .toMutableList()
    }
    view.showPaymentMethods(paymentList, symbol, paymentMethodId, fiatAmount, appcAmount,
        appcEnabled, creditsEnabled,frequency)
    sendPaymentMethodsEvents()
  }

  private fun showPreSelectedPaymentMethod(fiatValue: FiatValue, paymentMethod: PaymentMethod,
                                           fiatAmount: String, appcAmount: String,
                                           isBonusActive: Boolean, frequency: String?) {
    view.showPreSelectedPaymentMethod(paymentMethod, fiatValue,
        mapCurrencyCodeToSymbol(fiatValue.currency), fiatAmount, appcAmount, isBonusActive,
        frequency)
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
                      appcAmount, frequency)
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

  private fun close() {
    view.close(paymentMethodsMapper.mapCancellation())
  }

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
      if (paymentMethod.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id) {
        if (preSelectedPreference == PaymentMethodsView.PaymentMethodId.APPC.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC.id,
              mergedPaymentMethod.appcLabel, mergedPaymentMethod.iconUrl, mergedPaymentMethod.fee,
              mergedPaymentMethod.isAppcEnabled)
        }
        if (preSelectedPreference == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
              mergedPaymentMethod.creditsLabel, paymentMethod.creditsIconUrl,
              mergedPaymentMethod.fee, mergedPaymentMethod.isCreditsEnabled)
        }
      }
      if (paymentMethod.id == preSelectedPreference) {
        return paymentMethod
      }
    }
    return null
  }

  private fun getLastUsedPaymentMethod(paymentMethods: List<PaymentMethod>): String {
    val lastUsedPaymentMethod = interactor.getLastUsedPaymentMethod()
    for (it in paymentMethods) {
      if (it.isEnabled) {
        if (it.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id &&
            (lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC.id ||
                lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id)) {
          return PaymentMethodsView.PaymentMethodId.MERGED_APPC.id
        }
        if (it.id == lastUsedPaymentMethod) {
          return it.id
        }
      }
    }
    return PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id
  }

  private fun handleBonusVisibility(selectedPaymentMethod: String) {
    when (selectedPaymentMethod) {
      paymentMethodsMapper.map(SelectedPaymentMethod.EARN_APPC) -> view.replaceBonus()
      paymentMethodsMapper.map(SelectedPaymentMethod.MERGED_APPC) -> view.hideBonus()
      paymentMethodsMapper.map(SelectedPaymentMethod.APPC_CREDITS) -> view.hideBonus()
      else -> if (paymentMethodsData.isSubscription) {
        view.showBonus(R.string.subscriptions_bonus_body)
      } else {
        view.showBonus(R.string.gamification_purchase_body)
      }    }
  }

  private fun handlePositiveButtonText(selectedPaymentMethod: String) {
    if (selectedPaymentMethod == paymentMethodsMapper.map(
            SelectedPaymentMethod.MERGED_APPC) || selectedPaymentMethod == paymentMethodsMapper.map(
            SelectedPaymentMethod.EARN_APPC)) {
      view.showNext()
    } else {
      if (paymentMethodsData.isSubscription) {
        view.showSubscribe()
      } else {
        view.showBuy()
      }
    }
  }

  private fun handleBuyAnalytics(selectedPaymentMethod: PaymentMethod) {
    val action =
        if (selectedPaymentMethod.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id) "next" else "buy"
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
      if (purchase.product.name == sku) {
        return true
      }
    }
    return false
  }

  private fun getRequestedSkuPurchase(purchases: List<Purchase>, sku: String?): Purchase? {
    for (purchase in purchases) {
      if (purchase.product.name == sku) {
        return purchase
      }
    }
    return null
  }

  private fun showAuthenticationActivity(paymentMethod: PaymentMethod, isPreselected: Boolean) {
    cachedPaymentNavigationData =
        PaymentNavigationData(paymentMethod.id, paymentMethod.label, paymentMethod.iconUrl,
            isPreselected)
    view.showAuthenticationActivity()
  }

  fun onSavedInstance(outState: Bundle) {
    outState.putInt(GAMIFICATION_LEVEL, cachedGamificationLevel)
    outState.putBoolean(HAS_STARTED_AUTH, hasStartedAuth)
    outState.putSerializable(FIAT_VALUE, cachedFiatValue)
    outState.putSerializable(PAYMENT_NAVIGATION_DATA, cachedPaymentNavigationData)
  }
}

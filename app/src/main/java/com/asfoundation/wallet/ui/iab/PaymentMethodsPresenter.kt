package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.Billing
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.asf.wallet.R
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.repository.BdsPendingTransactionService
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.math.BigDecimal
import java.util.*

class PaymentMethodsPresenter(
    private val view: PaymentMethodsView,
    private val appPackage: String,
    private val viewScheduler: Scheduler,
    private val networkThread: Scheduler,
    private val disposables: CompositeDisposable,
    private val inAppPurchaseInteractor: InAppPurchaseInteractor,
    private val balanceInteract: BalanceInteract,
    private val billingMessagesMapper: BillingMessagesMapper,
    private val bdsPendingTransactionService: BdsPendingTransactionService,
    private val billing: Billing,
    private val analytics: BillingAnalytics,
    private val isBds: Boolean,
    private val developerPayload: String?,
    private val uri: String?,
    private val gamification: GamificationInteractor,
    private val transaction: TransactionBuilder,
    private val paymentMethodsMapper: PaymentMethodsMapper,
    private val walletBlockedInteract: WalletBlockedInteract,
    private val transactionValue: Double) {

  fun present() {

    handleCancelClick()
    handleErrorDismisses()
    handleMorePaymentMethodClicks()
    loadBonusIntoView()
    setupUi(transactionValue)
    handleOnGoingPurchases()
    handleBuyClick()
    if (isBds) {
      handlePaymentSelection()
    }

  }

  private fun handlePaymentSelection() {
    disposables.add(view.getPaymentSelection()
        .observeOn(viewScheduler)
        .doOnNext { selectedPaymentMethod ->
          handleBonusVisibility(selectedPaymentMethod)
          handlePositiveButtonText(selectedPaymentMethod)
        }
        .subscribe())
  }

  private fun loadBonusIntoView() {
    disposables.add(gamification.getEarningBonus(transaction.domain, transaction.amount())
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .doOnSuccess {
          if (it.status == ForecastBonus.Status.ACTIVE && it.amount > BigDecimal.ZERO) {
            view.setBonus(it.amount, it.currency)
          }
        }
        .subscribe())
  }

  private fun handleBuyClick() {
    disposables.add(view.getBuyClick()
        .observeOn(viewScheduler)
        .doOnNext { selectedPaymentMethod ->
          when (paymentMethodsMapper.map(selectedPaymentMethod.id)) {
            PaymentMethodsView.SelectedPaymentMethod.PAYPAL -> view.showPaypal()
            PaymentMethodsView.SelectedPaymentMethod.CREDIT_CARD -> view.showCreditCard()
            PaymentMethodsView.SelectedPaymentMethod.APPC -> view.showAppCoins()
            PaymentMethodsView.SelectedPaymentMethod.APPC_CREDITS -> handleWalletBlockStatus()
            PaymentMethodsView.SelectedPaymentMethod.SHARE_LINK -> view.showShareLink(
                selectedPaymentMethod.id)
            PaymentMethodsView.SelectedPaymentMethod.LOCAL_PAYMENTS -> view.showLocalPayment(
                selectedPaymentMethod.id, selectedPaymentMethod.iconUrl,
                selectedPaymentMethod.label)
            PaymentMethodsView.SelectedPaymentMethod.MERGED_APPC -> view.showMergedAppcoins()
            PaymentMethodsView.SelectedPaymentMethod.EARN_APPC -> view.showEarnAppcoins()
            else -> return@doOnNext
          }
        }
        .subscribe())
  }

  private fun handleWalletBlockStatus() {
    disposables.add(
        walletBlockedInteract.isWalletBlocked()
            .subscribeOn(networkThread)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              if (it) {
                Completable.fromAction {
                  view.hideLoading()
                  view.showWalletBlocked()
                }
              } else {
                Completable.fromAction {
                  view.showCredits()
                }
              }
            }
            .andThen { Completable.fromAction { view.hideLoading() } }
            .doOnSubscribe { view.showLoading() }
            .doOnError { showError(it) }
            .subscribe()
    )
  }

  private fun handleOnGoingPurchases() {
    if (transaction.skuId == null) {
      disposables.add(isSetupCompleted().doOnComplete { view.hideLoading() }
          .subscribeOn(viewScheduler)
          .subscribe())
      return
    }
    disposables.add(waitForUi(transaction.skuId).observeOn(viewScheduler)
        .subscribe({ view.hideLoading() }, { throwable: Throwable ->
          showError(throwable)
          throwable.printStackTrace()
        }))
  }

  private fun isSetupCompleted(): Completable {
    return view.setupUiCompleted()
        .takeWhile { isViewSet -> !isViewSet }
        .ignoreElements()
  }

  private fun waitForUi(skuId: String?): Completable {
    return Completable.mergeArray(checkProcessing(skuId), checkAndConsumePrevious(skuId),
        isSetupCompleted())
  }

  private fun waitForOngoingPurchase(skuId: String?): Completable {
    return Completable.mergeArray(checkProcessing(skuId), checkAndConsumePrevious(skuId))
  }

  private fun checkProcessing(skuId: String?): Completable {
    return billing.getSkuTransaction(appPackage, skuId, Schedulers.io())
        .filter { (_, status) -> status === Transaction.Status.PROCESSING }
        .observeOn(viewScheduler)
        .doOnSuccess { view.showProcessingLoadingDialog() }
        .doOnSuccess { handleProcessing() }
        .map { it.uid }
        .observeOn(Schedulers.io())
        .flatMapCompletable { uid ->
          bdsPendingTransactionService.checkTransactionStateFromTransactionId(uid)
              .ignoreElements()
              .andThen(finishProcess(skuId))
        }
  }

  private fun handleProcessing() {
    disposables.add(inAppPurchaseInteractor.getCurrentPaymentStep(appPackage, transaction)
        .filter { currentPaymentStep -> currentPaymentStep == AsfInAppPurchaseInteractor.CurrentPaymentStep.PAUSED_ON_CHAIN }
        .doOnSuccess {
          view.lockRotation()
          inAppPurchaseInteractor.resume(uri, AsfInAppPurchaseInteractor.TransactionType.NORMAL,
              appPackage, transaction.skuId, developerPayload, isBds)
        }
        .subscribe())
  }

  private fun finishProcess(skuId: String?): Completable {
    return billing.getSkuPurchase(appPackage, skuId, Schedulers.io())
        .observeOn(viewScheduler)
        .doOnSuccess { purchase -> finish(purchase, false) }
        .toCompletable()
  }

  private fun checkAndConsumePrevious(sku: String?): Completable {
    return getPurchases(sku).observeOn(viewScheduler)
        .doOnNext { view.showItemAlreadyOwnedError() }
        .ignoreElements()
  }

  private fun getPurchases(sku: String?): Observable<Purchase> {
    return billing.getPurchases(appPackage, BillingSupportedType.INAPP, Schedulers.io())
        .flatMapObservable { purchases ->
          for (purchase in purchases) {
            if (purchase.product
                    .name == sku) {
              return@flatMapObservable Observable.just(purchase)
            }
          }
          return@flatMapObservable Observable.empty<Purchase>()
        }
  }

  private fun setupUi(transactionValue: Double) {
    disposables.add(waitForOngoingPurchase(transaction.skuId).subscribeOn(networkThread).andThen(
        inAppPurchaseInteractor.convertToLocalFiat(transactionValue).subscribeOn(networkThread)
            .flatMapCompletable { fiatValue ->
              getPaymentMethods(fiatValue)
                  .observeOn(viewScheduler)
                  .flatMapCompletable { paymentMethods ->
                    Completable.fromAction { selectPaymentMethod(paymentMethods, fiatValue) }
                  }
            })
        .subscribeOn(networkThread)
        .observeOn(viewScheduler)
        .subscribe({ }, { this.showError(it) }))
  }

  private fun selectPaymentMethod(paymentMethods: List<PaymentMethod>, fiatValue: FiatValue) {
    if (inAppPurchaseInteractor.hasAsyncLocalPayment()) {
      getCreditsPaymentMethod(paymentMethods)?.let {
        if (it.isEnabled) {
          showPreSelectedPaymentMethod(fiatValue, it)
          return
        }
      }
    }

    if (inAppPurchaseInteractor.hasPreSelectedPaymentMethod()) {
      val paymentMethod = getPreSelectedPaymentMethod(paymentMethods)
      if (paymentMethod == null || !paymentMethod.isEnabled) {
        showPaymentMethods(fiatValue, paymentMethods,
            PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id)
      } else {
        when (paymentMethod.id) {
          PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id -> {
            analytics.sendPurchaseDetailsEvent(appPackage, transaction.skuId, transaction.amount()
                .toString(), transaction.type)
            view.showAdyen(fiatValue,
                PaymentType.CARD, paymentMethod.iconUrl)
          }
          else -> showPreSelectedPaymentMethod(fiatValue, paymentMethod)
        }
      }
    } else {
      val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
      showPaymentMethods(fiatValue, paymentMethods, paymentMethodId)
    }
  }

  private fun getCreditsPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    paymentMethods.forEach {
      if (it.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id) {
        val mergedPaymentMethod = it as AppCoinsPaymentMethod
        return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
            mergedPaymentMethod.creditsLabel, mergedPaymentMethod.iconUrl,
            mergedPaymentMethod.isCreditsEnabled)
      }
      if (it.id == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id) {
        return it
      }
    }

    return null
  }

  private fun showPaymentMethods(fiatValue: FiatValue, paymentMethods: List<PaymentMethod>,
                                 paymentMethodId: String) {
    view.showPaymentMethods(paymentMethods.toMutableList(), fiatValue,
        mapCurrencyCodeToSymbol(fiatValue.currency), paymentMethodId)
  }

  private fun showPreSelectedPaymentMethod(fiatValue: FiatValue, paymentMethod: PaymentMethod) {
    view.showPreSelectedPaymentMethod(paymentMethod, fiatValue,
        TransactionData.TransactionType.DONATION.name
            .equals(transaction.type, ignoreCase = true),
        mapCurrencyCodeToSymbol(fiatValue.currency))
  }

  private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
    return if (currencyCode.equals("APPC", ignoreCase = true))
      currencyCode
    else
      Currency.getInstance(currencyCode)
          .currencyCode
  }

  private fun handleCancelClick() {
    disposables.add(view.getCancelClick()
        .subscribe { close() })
  }

  private fun handleMorePaymentMethodClicks() {
    disposables.add(view.getMorePaymentMethodsClicks()
        .doOnEach { view.showLoading() }
        .flatMapSingle {
          inAppPurchaseInteractor.convertToLocalFiat(transactionValue)
              .subscribeOn(networkThread)
        }
        .flatMapCompletable { fiatValue ->
          getPaymentMethods(fiatValue).observeOn(viewScheduler)
              .flatMapCompletable { paymentMethods ->
                Completable.fromAction {
                  val paymentMethodId = getLastUsedPaymentMethod(paymentMethods)
                  loadBonusIntoView()
                  showPaymentMethods(fiatValue, paymentMethods, paymentMethodId)
                }
              }
              .andThen(
                  Completable.fromAction { inAppPurchaseInteractor.removePreSelectedPaymentMethod() })
              .andThen(Completable.fromAction { inAppPurchaseInteractor.removeAsyncLocalPayment() })
              .andThen(Completable.fromAction { view.hideLoading() })
        }
        .subscribe({ }, { this.showError(it) }))
  }

  private fun showError(t: Throwable) {
    t.printStackTrace()
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
    view.close(billingMessagesMapper.mapCancellation())
  }

  private fun handleErrorDismisses() {
    disposables.add(Observable.merge(view.errorDismisses(), view.onBackPressed())
        .flatMapCompletable { itemAlreadyOwned ->
          if (itemAlreadyOwned) {
            return@flatMapCompletable getPurchases(transaction.skuId).doOnNext {
              finish(it, true)
            }
                .ignoreElements()
          } else {
            return@flatMapCompletable Completable.fromAction { view.close(Bundle()) }
          }
        }
        .subscribe({ }, { this.showError(it) }))
  }

  private fun finish(purchase: Purchase, itemAlreadyOwned: Boolean) {
    view.finish(billingMessagesMapper.mapFinishedPurchase(purchase, itemAlreadyOwned))
  }

  fun sendPurchaseDetailsEvent() {
    analytics.sendPurchaseDetailsEvent(appPackage, transaction.skuId, transaction.amount()
        .toString(), transaction.type)
  }

  fun stop() {
    disposables.clear()
  }

  private fun getPaymentMethods(fiatValue: FiatValue): Single<List<PaymentMethod>> {
    return if (isBds) {
      inAppPurchaseInteractor.getPaymentMethods(transaction, fiatValue.amount.toString(),
          fiatValue.currency)
          .map { inAppPurchaseInteractor.mergeAppcoins(it) }
          .doOnSuccess { updateBalanceDao() }
    } else {
      Single.just(listOf(PaymentMethod.APPC))
    }
  }

  //Updates database with the latest balance to take less time loading the merged appcoins view
  private fun updateBalanceDao() {
    disposables.add(
        Observable.zip(balanceInteract.getEthBalance(), balanceInteract.getCreditsBalance(),
            balanceInteract.getAppcBalance(), Function3 { _: Any, _: Any, _: Any -> }).take(1)
            .subscribeOn(networkThread)
            .subscribe())
  }

  private fun getPreSelectedPaymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? {
    val preSelectedPreference = inAppPurchaseInteractor.preSelectedPaymentMethod
    for (paymentMethod in paymentMethods) {
      if (paymentMethod.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id) {
        if (preSelectedPreference == PaymentMethodsView.PaymentMethodId.APPC.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC.id,
              mergedPaymentMethod.appcLabel, mergedPaymentMethod.iconUrl,
              mergedPaymentMethod.isAppcEnabled)
        }
        if (preSelectedPreference == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id) {
          val mergedPaymentMethod = paymentMethod as AppCoinsPaymentMethod
          return PaymentMethod(PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id,
              mergedPaymentMethod.creditsLabel, paymentMethod.creditsIconUrl,
              mergedPaymentMethod.isCreditsEnabled)
        }
      }
      if (paymentMethod.id == preSelectedPreference) {
        return paymentMethod
      }
    }
    return null
  }

  private fun getLastUsedPaymentMethod(paymentMethods: List<PaymentMethod>): String {
    val lastUsedPaymentMethod = inAppPurchaseInteractor.lastUsedPaymentMethod
    for (it in paymentMethods) {
      if (it.id == PaymentMethodsView.PaymentMethodId.MERGED_APPC.id &&
          (lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC.id ||
              lastUsedPaymentMethod == PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id)) {
        return PaymentMethodsView.PaymentMethodId.MERGED_APPC.id
      }
      if (it.id == lastUsedPaymentMethod && it.isEnabled) {
        return it.id
      }
    }
    return PaymentMethodsView.PaymentMethodId.CREDIT_CARD.id
  }

  private fun handleBonusVisibility(selectedPaymentMethod: String) {
    if (selectedPaymentMethod == paymentMethodsMapper.map(
            PaymentMethodsView.SelectedPaymentMethod.EARN_APPC)) {
      view.replaceBonus()
    } else {
      view.showBonus()
    }
  }

  private fun handlePositiveButtonText(selectedPaymentMethod: String) {
    if (selectedPaymentMethod == paymentMethodsMapper.map(
            PaymentMethodsView.SelectedPaymentMethod.MERGED_APPC) || selectedPaymentMethod == paymentMethodsMapper.map(
            PaymentMethodsView.SelectedPaymentMethod.EARN_APPC)) {
      view.showNext()
    } else {
      view.showBuy()
    }
  }

}

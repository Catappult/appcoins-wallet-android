package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.TransferParser
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AppcoinsRewardsBuyPresenter(private val view: AppcoinsRewardsBuyView,
                                  private val rewardsManager: RewardsManager,
                                  private val viewScheduler: Scheduler,
                                  private val networkScheduler: Scheduler,
                                  private val disposables: CompositeDisposable,
                                  private val amount: BigDecimal,
                                  private val uri: String,
                                  private val packageName: String,
                                  private val transferParser: TransferParser,
                                  private val isBds: Boolean,
                                  private val analytics: BillingAnalytics,
                                  private val transactionBuilder: TransactionBuilder,
                                  private val formatter: CurrencyFormatUtils,
                                  private val gamificationLevel: Int,
                                  private val appcoinsRewardsBuyInteract: AppcoinsRewardsBuyInteract,
                                  private val logger: Logger) {
  fun present() {
    view.lockRotation()
    handleBuyClick()
    handleOkErrorClick()
    handleSupportClicks()
  }

  private fun handleOkErrorClick() {
    disposables.add(view.getOkErrorClick()
        .doOnNext { view.errorClose() }
        .subscribe({}, { view.errorClose() }))
  }

  private fun handleBuyClick() {
    disposables.add(transferParser.parse(uri)
        .flatMapCompletable { transaction: TransactionBuilder ->
          rewardsManager.pay(transaction.skuId, amount, transaction.toAddress(), packageName,
              getOrigin(isBds, transaction), transaction.type, transaction.payload,
              transaction.callbackUrl, transaction.orderReference, transaction.referrerUrl)
              .andThen(rewardsManager.getPaymentStatus(packageName, transaction.skuId,
                  transaction.amount()))
              .observeOn(viewScheduler)
              .flatMapCompletable { paymentStatus: RewardPayment ->
                handlePaymentStatus(paymentStatus, transaction.skuId, transaction.amount())
              }
        }
        .doOnSubscribe { view.showLoading() }
        .subscribe({}, {
          view.showError(null)
          logger.log("AppcoinsRewardsBuyPresenter", it)
        }))
  }

  private fun getOrigin(isBds: Boolean, transaction: TransactionBuilder): String? {
    return if (transaction.origin == null) {
      if (isBds) "BDS" else null
    } else {
      transaction.origin
    }
  }

  private fun handlePaymentStatus(transaction: RewardPayment, sku: String?,
                                  amount: BigDecimal): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      Status.PROCESSING -> Completable.fromAction { view.showLoading() }
      Status.COMPLETED -> {
        if (isBds && isManagedPaymentType(transactionBuilder.type)) {
          val billingType = BillingSupportedType.valueOfProductType(transactionBuilder.type)
          rewardsManager.getPaymentCompleted(packageName, sku, billingType)
              .flatMapCompletable { purchase ->
                Completable.fromAction { view.showTransactionCompleted() }
                    .subscribeOn(viewScheduler)
                    .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
                    .andThen(
                        Completable.fromAction { appcoinsRewardsBuyInteract.removeAsyncLocalPayment() })
                    .andThen(Completable.fromAction {
                      view.finish(purchase, transaction.orderReference)
                    })
              }
              .observeOn(viewScheduler)
              .onErrorResumeNext {
                Completable.fromAction {
                  it.printStackTrace()
                  view.showError(null)
                  view.hideLoading()
                }
              }
        } else rewardsManager.getTransaction(packageName, sku, amount)
            .firstOrError()
            .map(Transaction::txId)
            .doOnSuccess { view.finish(it) }
            .ignoreElement()
      }
      Status.ERROR -> Completable.fromAction { view.showError(null) }
      Status.FORBIDDEN -> Completable.fromAction {
        handleFraudFlow()
      }
      Status.NO_NETWORK -> Completable.fromAction {
        view.showNoNetworkError()
        view.hideLoading()
      }
    }
  }

  private fun handleFraudFlow() {
    disposables.add(
        appcoinsRewardsBuyInteract.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(networkScheduler)
            .flatMap { blocked ->
              if (blocked) {
                appcoinsRewardsBuyInteract.isWalletVerified()
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      if (it) view.showError(R.string.purchase_wallet_error_contact_us)
                      else view.showWalletValidation(R.string.unknown_error)
                    }
              } else {
                Single.just(true)
                    .observeOn(viewScheduler)
                    .doOnSuccess { view.showError(R.string.purchase_wallet_error_contact_us) }
              }
            }
            .observeOn(viewScheduler)
            .subscribe({}, {
              it.printStackTrace()
              view.showError(R.string.purchase_wallet_error_contact_us)
            })
    )
  }

  fun stop() = disposables.clear()

  fun sendPaymentEvent() {
    analytics.sendPaymentEvent(packageName, transactionBuilder.skuId,
        transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.type)
  }

  fun sendRevenueEvent() {
    analytics.sendRevenueEvent(formatter.scaleFiat(appcoinsRewardsBuyInteract.convertToFiat(
        transactionBuilder.amount()
            .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
        .blockingGet()
        .amount)
        .toString())
  }

  fun sendPaymentSuccessEvent() {
    analytics.sendPaymentSuccessEvent(packageName, transactionBuilder.skuId,
        transactionBuilder.amount()
            .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.type)
  }

  private fun sendPaymentErrorEvent(transaction: RewardPayment) {
    val status = transaction.status
    if (status === Status.ERROR || status === Status.NO_NETWORK || status === Status.FORBIDDEN) {
      if (transaction.errorCode == null && transaction.errorMessage == null) {
        analytics.sendPaymentErrorEvent(packageName, transactionBuilder.skuId,
            transactionBuilder.amount()
                .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.type,
            status.toString())
      } else {
        analytics.sendPaymentErrorWithDetailsEvent(packageName, transactionBuilder.skuId,
            transactionBuilder.amount()
                .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.type,
            transaction.errorCode.toString(), transaction.errorMessage.toString())
      }
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClick(),
        view.getSupportLogoClick())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { appcoinsRewardsBuyInteract.showSupport(gamificationLevel) }
        .subscribe())
  }

  private fun isManagedPaymentType(type: String): Boolean {
    return type == BillingSupportedType.INAPP.name || type == BillingSupportedType.INAPP_SUBSCRIPTION.name
  }
}
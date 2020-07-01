package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.billing.repository.entity.TransactionData
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.RewardsManager.RewardPayment
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
                                  private val appcoinsRewardsBuyInteract: AppcoinsRewardsBuyInteract) {
  fun present() {
    view.lockRotation()
    handleBuyClick()
    handleOkErrorClick()
    handleSupportClicks()
  }

  private fun handleOkErrorClick() {
    disposables.add(
        view.getOkErrorClick()
            .subscribe { view.errorClose() }
    )
  }

  private fun handleBuyClick() {
    disposables.add(transferParser.parse(uri)
        .flatMapCompletable { transaction: TransactionBuilder ->
          Log.e("TESTE", transaction.toString())
          rewardsManager.pay(transaction.skuId, amount,
              transaction.toAddress(), packageName, getOrigin(isBds, transaction),
              transaction.type, transaction.payload, transaction.callbackUrl,
              transaction.orderReference, transaction.referrerUrl)
              .andThen(
                  rewardsManager.getPaymentStatus(packageName, transaction.skuId,
                      transaction.amount()))
              .observeOn(viewScheduler)
              .flatMapCompletable { paymentStatus: RewardPayment ->
                handlePaymentStatus(paymentStatus, transaction.skuId,
                    transaction.amount())
              }
        }
        .doOnSubscribe { view.showLoading() }
        .subscribe())
  }

  private fun getOrigin(isBds: Boolean, transaction: TransactionBuilder): String? {
    return if (transaction.origin == null) {
      if (isBds) "BDS" else null
    } else {
      transaction.origin
    }
  }

  private fun handlePaymentStatus(transaction: RewardPayment, sku: String,
                                  amount: BigDecimal): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      RewardPayment.Status.PROCESSING -> Completable.fromAction { view.showLoading() }
      RewardPayment.Status.COMPLETED -> {
        if (isBds && transactionBuilder.type.equals(TransactionData.TransactionType.INAPP.name,
                ignoreCase = true)) {
          rewardsManager.getPaymentCompleted(packageName, sku)
              .flatMapCompletable { purchase: Purchase ->
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
              .onErrorResumeNext { throwable: Throwable ->
                Completable.fromAction {
                  Log.e("TESTE", transaction.toString())
                  throwable.printStackTrace()
                  view.showGenericError()
                  view.hideLoading()
                }
              }
        } else rewardsManager.getTransaction(packageName, sku, amount)
            .firstOrError()
            .map<String>(Transaction::txId)
            .doOnSuccess { uid: String -> view.finish(uid) }
            .ignoreElement()
      }
      RewardPayment.Status.ERROR -> Completable.fromAction {
        if (true) {
          handleFraudFlow()
        }
      }
      RewardPayment.Status.NO_NETWORK -> Completable.fromAction {
        view.showNoNetworkError()
        view.hideLoading()
      }
      else -> Completable.error(UnsupportedOperationException(
          "Transaction status " + transaction.status + " not supported"))
    }
  }

  private fun handleFraudFlow() {
    disposables.add(
        appcoinsRewardsBuyInteract.isWalletBlocked()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .observeOn(networkScheduler)
            .flatMap { blocked ->
              if (blocked) {
                appcoinsRewardsBuyInteract.isWalletVerified()
                    .observeOn(viewScheduler)
                    .doOnSuccess {
                      if (it) view.showGenericError()
                      else view.showWalletValidation()
                    }
              } else {

                Single.just(true)
                    .observeOn(viewScheduler)
                    .doOnSuccess { view.showGenericError() }
              }
            }
            .observeOn(viewScheduler)
            .subscribe({}, {
              it.printStackTrace()
              view.showGenericError()
            })
    )
  }

  fun stop() {
    disposables.clear()
  }

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
    if (transaction.status == RewardPayment.Status.ERROR || transaction.status == RewardPayment.Status.NO_NETWORK) {
      analytics.sendPaymentErrorEvent(packageName, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_REWARDS, transactionBuilder.type,
          transaction.status.toString())
    }
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClick(),
        view.getSupportLogoClick())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable {
          appcoinsRewardsBuyInteract.showSupport(gamificationLevel)
        }
        .subscribe())
  }

}
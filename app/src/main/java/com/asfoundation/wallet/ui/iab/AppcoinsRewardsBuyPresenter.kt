package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.appcoins.rewards.Transaction
import com.appcoins.wallet.core.network.microservices.model.BillingSupportedType
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class AppcoinsRewardsBuyPresenter(
  private val view: AppcoinsRewardsBuyView,
  private val rewardsManager: RewardsManager,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val disposables: CompositeDisposable,
  private val packageName: String,
  private val isBds: Boolean,
  private val isPreSelected: Boolean,
  private val analytics: BillingAnalytics,
  private val paymentAnalytics: PaymentMethodsAnalytics,
  private val transactionBuilder: TransactionBuilder,
  private val formatter: CurrencyFormatUtils,
  private val gamificationLevel: Int,
  private val appcoinsRewardsBuyInteract: AppcoinsRewardsBuyInteract,
  private val logger: Logger
) {

  companion object {
    private val TAG = AppcoinsRewardsBuyPresenter::class.java.name
  }

  fun present() {
    view.lockRotation()
    handleBuyClick()
    handleOkErrorClick()
    handleSupportClicks()
  }

  private fun handleOkErrorClick() {
    disposables.add(view.getOkErrorClick()
      .doOnNext {
        view.showPaymentMethods()
      }
      .subscribe({}, {
        logger.log(TAG, "Ok error click", it)
        view.errorClose()
      })
    )
  }

  private fun handleBuyClick() {
    disposables.add(rewardsManager
      .pay(
        transactionBuilder.skuId,
        transactionBuilder.amount(),
        transactionBuilder.toAddress(),
        packageName,
        getOrigin(isBds, transactionBuilder),
        transactionBuilder.type,
        transactionBuilder.payload,
        transactionBuilder.callbackUrl,
        transactionBuilder.orderReference,
        transactionBuilder.referrerUrl,
        transactionBuilder.productToken
      )
      .andThen(
        rewardsManager.getPaymentStatus(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount()
        )
      )
      .subscribeOn(networkScheduler)
      .flatMapCompletable {
        handlePaymentStatus(it, transactionBuilder.skuId, transactionBuilder.amount())
      }
      .observeOn(viewScheduler)
      .doOnSubscribe {
        paymentAnalytics.startTimingForPurchaseEvent()
        view.showLoading()
      }
      .doOnError {
        logger.log(TAG, it)
        view.showError(null)
      }
      .subscribe({}, {})
    )
  }

  private fun getOrigin(isBds: Boolean, transaction: TransactionBuilder): String? =
    if (transaction.origin == null) {
      if (isBds) "BDS" else null
    } else {
      transaction.origin
    }

  private fun handlePaymentStatus(
    transaction: RewardPayment,
    sku: String?,
    amount: BigDecimal
  ): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      Status.PROCESSING -> Completable.fromAction { view.showLoading() }.subscribeOn(viewScheduler)
      Status.COMPLETED -> {
        if (isBds && isManagedPaymentType(transactionBuilder.type)) {
          val billingType = BillingSupportedType.valueOfProductType(transactionBuilder.type)
          rewardsManager.getPaymentCompleted(packageName, sku, transaction.purchaseUid, billingType)
            .flatMapCompletable { purchase ->
              Completable.fromAction { view.showTransactionCompleted() }
                .subscribeOn(viewScheduler)
                .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS, viewScheduler))
                .andThen(Completable.fromAction { appcoinsRewardsBuyInteract.removeAsyncLocalPayment() })
                .andThen(Completable.fromAction {
                  view.finish(purchase, transaction.orderReference)
                })
            }
            .observeOn(viewScheduler)
            .onErrorResumeNext {
              Completable.fromAction {
                logger.log(TAG, "Error after completing the transaction", it)
                view.showError(null)
                view.hideLoading()
              }
            }
        } else {
          rewardsManager.getTransaction(packageName, sku, amount).firstOrError()
            .map(Transaction::txId).flatMapCompletable { transactionId ->
              Completable.fromAction { view.showTransactionCompleted() }
                .subscribeOn(viewScheduler)
                .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS, viewScheduler))
                .andThen(Completable.fromAction { view.finish(transactionId) })
            }
        }
      }
      Status.ERROR -> Completable.fromAction {
        logger.log(TAG, "Credits error: ${transaction.errorMessage}")
        view.showError(null)
      }.subscribeOn(viewScheduler)
      Status.FORBIDDEN -> Completable.fromAction {
        logger.log(TAG, "Forbidden")
        handleFraudFlow()
      }
      Status.SUB_ALREADY_OWNED -> Completable.fromAction {
        logger.log(TAG, "Sub already owned")
        view.showError(R.string.subscriptions_error_already_subscribed)
      }.subscribeOn(viewScheduler)
      Status.NO_NETWORK -> Completable.fromAction {
        logger.log(TAG, Exception("PaymentStatus no network"))
        view.showNoNetworkError()
        view.hideLoading()
      }.subscribeOn(viewScheduler)
    }
  }

  private fun handleFraudFlow() {
    disposables.add(
      appcoinsRewardsBuyInteract.isWalletBlocked()
        .subscribeOn(networkScheduler)
        .observeOn(networkScheduler)
        .flatMap { blocked ->
          if (blocked) {
            appcoinsRewardsBuyInteract
              .isWalletVerified()
              .observeOn(viewScheduler)
              .doOnSuccess {
                if (it) {
                  logger.log(TAG, Exception("FraudFlow blocked"))
                  view.showError(R.string.purchase_error_wallet_block_code_403)
                } else {
                  view.showVerification()
                }
              }
          } else {
            Single.just(true)
              .observeOn(viewScheduler)
              .doOnSuccess {
                logger.log(TAG, Exception("FraudFlow not blocked"))
                view.showError(R.string.purchase_error_wallet_block_code_403)
              }
          }
        }
        .observeOn(viewScheduler)
        .subscribe({}, {
          logger.log(TAG, it)
          view.showError(R.string.purchase_error_wallet_block_code_403)
        })
    )
  }

  fun stop() = disposables.clear()

  fun sendPaymentEvent() {
    analytics.sendPaymentEvent(
      packageName,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      BillingAnalytics.PAYMENT_METHOD_REWARDS,
      transactionBuilder.type
    )
  }

  fun sendRevenueEvent() {
    analytics.sendRevenueEvent(
      formatter.scaleFiat(
        appcoinsRewardsBuyInteract.convertToFiat(
          transactionBuilder.amount().toDouble(),
          BillingAnalytics.EVENT_REVENUE_CURRENCY
        ).blockingGet()
          .amount
      ).toString()
    )
  }

  fun sendPaymentSuccessEvent(txId: String) {
    paymentAnalytics.stopTimingForPurchaseEvent(
      PaymentMethodsAnalytics.PAYMENT_METHOD_APPC,
      true,
      isPreSelected
    )
    analytics.sendPaymentSuccessEvent(
      packageName = packageName,
      skuDetails = transactionBuilder.skuId,
      value = transactionBuilder.amount().toString(),
      purchaseDetails = BillingAnalytics.PAYMENT_METHOD_REWARDS,
      transactionType = transactionBuilder.type,
      txId = txId,
      valueUsd = transactionBuilder.amountUsd.toString()
    )
  }

  private fun sendPaymentErrorEvent(transaction: RewardPayment) {
    val status = transaction.status
    if (isErrorStatus(status)) {
      paymentAnalytics.stopTimingForPurchaseEvent(
        PaymentMethodsAnalytics.PAYMENT_METHOD_APPC,
        true,
        isPreSelected
      )
      if (transaction.errorCode == null && transaction.errorMessage == null) {
        analytics.sendPaymentErrorEvent(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_REWARDS,
          transactionBuilder.type,
          status.toString()
        )
      } else {
        analytics.sendPaymentErrorWithDetailsEvent(
          packageName,
          transactionBuilder.skuId,
          transactionBuilder.amount().toString(),
          BillingAnalytics.PAYMENT_METHOD_REWARDS,
          transactionBuilder.type,
          transaction.errorCode.toString(),
          transaction.errorMessage.toString()
        )
      }
    }
  }

  private fun isErrorStatus(status: Status): Boolean =
    status === Status.ERROR || status === Status.NO_NETWORK || status === Status.FORBIDDEN || status === Status.SUB_ALREADY_OWNED

  private fun handleSupportClicks() {
    disposables.add(
      Observable.merge(view.getSupportIconClick(), view.getSupportLogoClick())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { appcoinsRewardsBuyInteract.showSupport(gamificationLevel) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun isManagedPaymentType(type: String): Boolean =
    type == BillingSupportedType.INAPP.name || type == BillingSupportedType.INAPP_SUBSCRIPTION.name
}
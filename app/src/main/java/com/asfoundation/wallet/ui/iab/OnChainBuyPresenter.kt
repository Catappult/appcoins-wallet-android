package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.billing.BillingMessagesMapper
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.AsfInAppPurchaseInteractor.CurrentPaymentStep
import com.asfoundation.wallet.util.UnknownTokenException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class OnChainBuyPresenter(private val view: OnChainBuyView,
                          private val viewScheduler: Scheduler,
                          private val networkScheduler: Scheduler,
                          private val disposables: CompositeDisposable,
                          private val billingMessagesMapper: BillingMessagesMapper,
                          private val isBds: Boolean,
                          private val analytics: BillingAnalytics,
                          private val appPackage: String,
                          private val uriString: String?,
                          private val gamificationLevel: Int,
                          private val logger: Logger,
                          private val onChainBuyInteract: OnChainBuyInteract) {

  private val transactionBuilder = onChainBuyInteract.parseTransaction(uriString, isBds)
  private var statusDisposable: Disposable? = null

  fun present(productName: String?, amount: BigDecimal, developerPayload: String?) {
    setupUi(amount, developerPayload)
    handleOkErrorClick()
    handleBuyEvent(productName, developerPayload, isBds)
    handleSupportClick()
  }

  private fun showTransactionState() {
    if (statusDisposable != null && !statusDisposable!!.isDisposed) {
      statusDisposable!!.dispose()
    }
    statusDisposable = onChainBuyInteract.getTransactionState(uriString)
        .observeOn(viewScheduler)
        .flatMapCompletable { showPendingTransaction(it) }
        .subscribe({}) { showError(it) }
  }

  private fun handleBuyEvent(productName: String?, developerPayload: String?, isBds: Boolean) {
    showTransactionState()
    disposables.add(
        onChainBuyInteract.send(uriString, AsfInAppPurchaseInteractor.TransactionType.NORMAL,
            appPackage, productName, developerPayload, isBds)
            .observeOn(viewScheduler)
            .doOnError { showError(it) }
            .subscribe({}, { showError(it) }))
  }

  private fun handleOkErrorClick() {
    disposables.add(view.getOkErrorClick()
        .flatMapSingle { onChainBuyInteract.parseTransaction(uriString, isBds) }
        .subscribe({ close() }, { close() }))
  }

  private fun handleSupportClick() {
    disposables.add(Observable.merge(view.getSupportIconClick(), view.getSupportLogoClick())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .flatMapCompletable { onChainBuyInteract.showSupport(gamificationLevel) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun setupUi(appcAmount: BigDecimal, developerPayload: String?) {
    disposables.add(onChainBuyInteract.parseTransaction(uriString, isBds)
        .flatMapCompletable { transaction: TransactionBuilder ->
          onChainBuyInteract.getCurrentPaymentStep(appPackage, transaction)
              .flatMapCompletable { currentPaymentStep: CurrentPaymentStep ->
                when (currentPaymentStep) {
                  CurrentPaymentStep.PAUSED_ON_CHAIN -> onChainBuyInteract.resume(uriString,
                      AsfInAppPurchaseInteractor.TransactionType.NORMAL, appPackage,
                      transaction.skuId, developerPayload, isBds, transaction.type)

                  CurrentPaymentStep.READY -> Completable.fromAction { setup(appcAmount) }
                      .subscribeOn(viewScheduler)

                  CurrentPaymentStep.NO_FUNDS -> Completable.fromAction { view.showNoFundsError() }
                      .subscribeOn(viewScheduler)

                  CurrentPaymentStep.PAUSED_CC_PAYMENT, CurrentPaymentStep.PAUSED_LOCAL_PAYMENT, CurrentPaymentStep.PAUSED_CREDITS ->
                    Completable.error(UnsupportedOperationException(
                        "Cannot resume from " + currentPaymentStep.name + " status"))
                }
              }
        }
        .subscribe({}) { showError(it) })
  }

  private fun close() = view.close(billingMessagesMapper.mapCancellation())

  private fun showError(throwable: Throwable?, message: String? = null) {
    logger.log(TAG, message, throwable)
    if (throwable is UnknownTokenException) view.showWrongNetworkError()
    else view.showError()
  }

  private fun showPendingTransaction(transaction: Payment): Completable {
    sendPaymentErrorEvent(transaction)
    return when (transaction.status) {
      Payment.Status.COMPLETED -> {
        view.lockRotation()
        onChainBuyInteract.getCompletedPurchase(transaction, isBds)
            .observeOn(viewScheduler)
            .map { buildBundle(it, transaction.orderReference) }
            .flatMapCompletable { bundle -> handleSuccessTransaction(bundle) }
            .onErrorResumeNext { Completable.fromAction { showError(it) } }
      }
      Payment.Status.NO_FUNDS -> Completable.fromAction { view.showNoFundsError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.NETWORK_ERROR -> Completable.fromAction { view.showWrongNetworkError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.NO_TOKENS -> Completable.fromAction { view.showNoTokenFundsError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.NO_ETHER -> Completable.fromAction { view.showNoEtherFundsError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.NO_INTERNET -> Completable.fromAction { view.showNoNetworkError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.NONCE_ERROR -> Completable.fromAction { view.showNonceError() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.APPROVING -> {
        view.lockRotation()
        Completable.fromAction { view.showApproving() }
      }

      Payment.Status.BUYING -> {
        view.lockRotation()
        Completable.fromAction { view.showBuying() }
      }
      Payment.Status.FORBIDDEN -> Completable.fromAction { handleFraudFlow() }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      Payment.Status.ERROR -> Completable.fromAction {
        showError(null, "Payment status: ${transaction.status.name}")
      }
          .andThen(onChainBuyInteract.remove(transaction.uri))

      else -> Completable.fromAction {
        showError(null, "Payment status: UNKNOWN")
      }
          .andThen(onChainBuyInteract.remove(transaction.uri))
    }
  }

  private fun handleSuccessTransaction(bundle: Bundle): Completable {
    return Completable.fromAction { view.showTransactionCompleted() }
        .subscribeOn(viewScheduler)
        .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
        .andThen(Completable.fromRunnable { view.finish(bundle) })
  }

  private fun buildBundle(payment: Payment, orderReference: String?): Bundle {
    return if (payment.uid != null && payment.signature != null && payment.signatureData != null) {
      billingMessagesMapper.mapPurchase(payment.uid!!, payment.signature!!,
          payment.signatureData!!, orderReference)
    } else {
      Bundle().also {
        it.putInt(IabActivity.RESPONSE_CODE, 0)
        it.putString(IabActivity.TRANSACTION_HASH, payment.buyHash)
      }
    }
  }

  fun stop() = disposables.clear()

  private fun setup(amount: BigDecimal) =
      view.showRaidenChannelValues(onChainBuyInteract.getTopUpChannelSuggestionValues(amount))

  fun sendPaymentEvent() {
    disposables.add(transactionBuilder.subscribe { transactionBuilder: TransactionBuilder ->
      analytics.sendPaymentEvent(appPackage, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transactionBuilder.type)
    })
  }

  fun resume() = showTransactionState()

  fun pause() = statusDisposable?.dispose()

  fun sendRevenueEvent() {
    disposables.add(transactionBuilder.flatMap { transaction ->
      onChainBuyInteract.convertToFiat(transaction.amount()
          .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
    }
        .doOnSuccess { (amount) -> analytics.sendRevenueEvent(amount.toString()) }
        .subscribe({ }, { it.printStackTrace() }))
  }

  fun sendPaymentSuccessEvent() {
    disposables.add(transactionBuilder.observeOn(networkScheduler)
        .subscribe { transaction ->
          analytics.sendPaymentSuccessEvent(appPackage, transaction.skuId, transaction.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transaction.type)
        })
  }

  private fun sendPaymentErrorEvent(payment: Payment) {
    val status = payment.status
    if (isError(status)) {
      if (payment.errorCode == null && payment.errorMessage == null) {
        disposables.add(transactionBuilder.observeOn(networkScheduler)
            .subscribe { transaction ->
              analytics.sendPaymentErrorEvent(appPackage, transaction.skuId, transaction.amount()
                  .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transaction.type,
                  status.name)
            })
      } else {
        disposables.add(transactionBuilder.observeOn(networkScheduler)
            .subscribe { transaction ->
              analytics.sendPaymentErrorWithDetailsEvent(appPackage, transaction.skuId,
                  transaction.amount()
                      .toString(), BillingAnalytics.PAYMENT_METHOD_APPC, transaction.type,
                  payment.errorCode.toString(), payment.errorMessage.toString())
            })
      }
    }
  }

  private fun handleFraudFlow() {
    disposables.add(onChainBuyInteract.isWalletBlocked()
        .subscribeOn(networkScheduler)
        .observeOn(networkScheduler)
        .flatMap { blocked ->
          if (blocked) {
            onChainBuyInteract.isWalletVerified()
                .observeOn(viewScheduler)
                .doOnSuccess { verified ->
                  if (verified) {
                    view.showForbiddenError()
                  } else {
                    view.showWalletValidation(R.string.purchase_error_wallet_block_code_403)
                  }
                }
          } else {
            Single.just(true)
                .observeOn(viewScheduler)
                .doOnSuccess { view.showForbiddenError() }
          }
        }
        .observeOn(viewScheduler)
        .subscribe({}, {
          logger.log(TAG, it)
          view.showForbiddenError()
        }))
  }


  private fun isError(status: Payment.Status): Boolean {
    return status == Payment.Status.ERROR || status == Payment.Status.NO_FUNDS ||
        status == Payment.Status.NONCE_ERROR || status == Payment.Status.NO_ETHER ||
        status == Payment.Status.NO_INTERNET || status == Payment.Status.NO_TOKENS ||
        status == Payment.Status.NETWORK_ERROR || status == Payment.Status.FORBIDDEN
  }

  companion object {
    private val TAG = OnChainBuyPresenter::class.java.simpleName
  }
}
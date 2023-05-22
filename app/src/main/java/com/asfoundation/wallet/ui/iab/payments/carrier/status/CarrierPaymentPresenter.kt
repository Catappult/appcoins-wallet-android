package com.asfoundation.wallet.ui.iab.payments.carrier.status

import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.appcoins.wallet.core.network.microservices.model.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class CarrierPaymentPresenter(private val disposables: CompositeDisposable,
                              private val view: CarrierPaymentView,
                              private val data: CarrierPaymentData,
                              private val navigator: CarrierPaymentNavigator,
                              private val carrierInteractor: CarrierInteractor,
                              private val billingAnalytics: BillingAnalytics,
                              private val logger: Logger,
                              private val viewScheduler: Scheduler,
                              private val ioScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleTransactionResult()
  }

  private fun initializeView() {
    view.initializeView(data.bonusAmount, data.currency)
    navigator.navigateToPaymentWebView(data.paymentUrl)
  }

  private fun handleTransactionResult() {
    disposables.add(navigator.uriResults()
        .doOnNext { view.setLoading() }
        .flatMapSingle { uri ->
          carrierInteractor.getFinishedPayment(uri, data.domain)
              .subscribeOn(ioScheduler)
        }
        .flatMap { payment ->
          var action = Completable.complete()
          when {
            isErrorStatus(payment.status) -> action = handleErrorStatus(payment)
            payment.status == TransactionStatus.COMPLETED -> action = handleCompletedStatus(payment)
            else -> Unit
          }
          action.andThen(Observable.just(payment))
        }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleErrorStatus(payment: CarrierPaymentModel): Completable {
    logger.log(CarrierPaymentFragment.TAG,
        "Transaction came with error status: ${payment.status}")
    return sendPaymentErrorEvent(payment.error.errorCode,
        payment.error.errorMessage)
        .observeOn(viewScheduler)
        .andThen(
            if (isUnauthorizedCode(payment.error.errorCode)) {
              handleFraudFlow()
            } else {
              Completable.fromAction {
                navigator.navigateToError(R.string.activity_iab_error_message)
              }
            }
        )
  }

  private fun handleCompletedStatus(payment: CarrierPaymentModel): Completable {
    return sendPaymentSuccessEvents()
        .andThen(carrierInteractor.savePhoneNumber(data.phoneNumber))
        .observeOn(viewScheduler)
        .andThen(Completable.fromAction { view.showFinishedTransaction() }
            .andThen(Completable.timer(view.getFinishedDuration(), TimeUnit.MILLISECONDS, viewScheduler))
            .andThen(finishPayment(payment)))
  }

  private fun isUnauthorizedCode(errorCode: Int?): Boolean {
    return errorCode == 403
  }

  private fun isErrorStatus(status: TransactionStatus) =
      status == TransactionStatus.FAILED ||
          status == TransactionStatus.CANCELED ||
          status == TransactionStatus.INVALID_TRANSACTION

  private fun handleError(throwable: Throwable) {
    logger.log(CarrierPaymentFragment.TAG, throwable)
    navigator.navigateToError(R.string.activity_iab_error_message)
  }

  private fun finishPayment(payment: CarrierPaymentModel): Completable {
    return carrierInteractor.getCompletePurchaseBundle(data.transactionType, data.domain,
        data.skuId, payment.purchaseUid, payment.reference, payment.hash, ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { bundleModel -> navigator.finishPayment(bundleModel.bundle) }
        .subscribeOn(ioScheduler)
        .ignoreElement()
  }

  private fun sendPaymentErrorEvent(refusalCode: Int?, refusalReason: String?): Completable {
    return Completable.fromAction {
      val code: String = if (refusalCode == -1) "ERROR" else refusalCode.toString()
      billingAnalytics.sendPaymentErrorWithDetailsEvent(data.domain, data.skuId,
          data.appcAmount.toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, data.transactionType,
          code, refusalReason)
    }
  }

  private fun sendPaymentSuccessEvents(): Completable {
    return carrierInteractor.convertToFiat(data.appcAmount
        .toDouble(), BillingAnalytics.EVENT_REVENUE_CURRENCY)
        .doOnSuccess { fiatValue ->
          billingAnalytics.sendPaymentSuccessEvent(data.domain, data.skuId,
              data.appcAmount
                  .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, data.transactionType)
          billingAnalytics.sendPaymentEvent(data.domain, data.skuId,
              data.appcAmount
                  .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, data.transactionType)
          billingAnalytics.sendRevenueEvent(fiatValue.amount.setScale(2, BigDecimal.ROUND_UP)
              .toString())
        }
        .ignoreElement()
        .subscribeOn(ioScheduler)
  }

  private fun handleFraudFlow(): Completable {
    return carrierInteractor.getWalletStatus()
        .observeOn(viewScheduler)
        .doOnSuccess { walletStatus ->
          if (walletStatus.blocked) {
            if (walletStatus.verified) {
              navigator.navigateToError(R.string.purchase_error_wallet_block_code_403)
            } else {
              navigator.navigateToVerification()
            }
          } else {
            navigator.navigateToError(R.string.purchase_error_wallet_block_code_403)
          }
        }
        .ignoreElement()
  }

  fun stop() = disposables.clear()
}
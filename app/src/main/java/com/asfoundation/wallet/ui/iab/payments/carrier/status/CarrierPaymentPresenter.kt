package com.asfoundation.wallet.ui.iab.payments.carrier.status

import com.appcoins.wallet.billing.carrierbilling.CarrierPaymentModel
import com.appcoins.wallet.billing.common.response.TransactionStatus
import com.asf.wallet.R
import com.asfoundation.wallet.analytics.FacebookEventLogger
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.ui.iab.payments.carrier.CarrierInteractor
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
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

  private lateinit var transactionBuilder: TransactionBuilder

  companion object {
    private val TAG = CarrierPaymentPresenter::class.java.simpleName
  }

  fun present() {
    initializePayment()
    handleTransactionResult()
  }

  private fun initializePayment() {
    disposables.add(carrierInteractor.getTransactionBuilder(data.transactionData)
        .doOnSuccess { transactionBuilder ->
          this.transactionBuilder = transactionBuilder
          navigator.navigateToPaymentWebView(data.paymentUrl)
        }
        .subscribe({}, { e -> e.printStackTrace() }))
  }

  private fun handleTransactionResult() {
    disposables.add(navigator.uriResults()
        .doOnNext {
          view.setLoading()
        }
        .flatMapSingle { uri ->
          carrierInteractor.getFinishedPayment(uri, data.domain)
              .subscribeOn(ioScheduler)
        }
        .flatMap { payment ->
          when {
            isErrorStatus(payment.status) -> {
              val code =
                  if (payment.networkError.code == -1) "ERROR" else payment.networkError.code.toString()
              logger.log(TAG, "Transaction came with error status: ${payment.status}")
              return@flatMap sendPaymentErrorEvent(code,
                  payment.networkError.message)
                  .observeOn(viewScheduler)
                  .doOnComplete {
                    navigator.navigateToError(R.string.activity_iab_error_message, false)
                  }
                  .andThen(Observable.just(Unit))
            }
            payment.status == TransactionStatus.COMPLETED -> {
              return@flatMap sendPaymentSuccessEvents()
                  .observeOn(viewScheduler)
                  .andThen(
                      Completable.fromAction { view.showFinishedTransaction() }
                          .andThen(
                              Completable.timer(view.getFinishedDuration(), TimeUnit.MILLISECONDS))
                          .andThen(finishPayment(payment))
                  )
                  .andThen(Observable.just(Unit))
            }
            else -> Observable.just(Unit)
          }
        }
        .onErrorReturn { e -> handleError(e).andThen(Observable.just(Unit)) }
        .subscribe({}, { e -> e.printStackTrace() }))
  }

  private fun isErrorStatus(status: TransactionStatus) =
      status == TransactionStatus.FAILED ||
          status == TransactionStatus.CANCELED ||
          status == TransactionStatus.INVALID_TRANSACTION

  private fun handleError(throwable: Throwable): Completable {
    logger.log(TAG, throwable)
    return if (throwable is HttpException) {
      sendPaymentErrorEvent(throwable.code()
          .toString(), throwable.message())
          .andThen(
              if (throwable.code() == 403) {
                handleFraudFlow()
              } else {
                Completable.complete()
              })
    } else {
      Completable.fromAction {
        navigator.navigateToError(R.string.unknown_error, false)
      }
          .subscribeOn(viewScheduler)
    }

  }

  private fun finishPayment(payment: CarrierPaymentModel): Completable {
    return carrierInteractor.getCompletePurchaseBundle(transactionBuilder.type,
        transactionBuilder.domain, transactionBuilder.skuId, payment.reference, payment.hash,
        ioScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { bundle ->
          navigator.finishPayment(bundle)
        }
        .subscribeOn(ioScheduler)
        .ignoreElement()
  }


  private fun sendPaymentErrorEvent(refusalCode: String?, refusalReason: String?): Completable {
    return Completable.fromAction {
      billingAnalytics.sendPaymentErrorWithDetailsEvent(data.domain, transactionBuilder.skuId,
          transactionBuilder.amount()
              .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, transactionBuilder.type,
          refusalCode.toString(), refusalReason)
    }
  }

  private fun sendPaymentSuccessEvents(): Completable {
    return carrierInteractor.convertToFiat(transactionBuilder.amount()
        .toDouble(), FacebookEventLogger.EVENT_REVENUE_CURRENCY)
        .doOnSuccess { fiatValue ->
          billingAnalytics.sendPaymentSuccessEvent(data.domain, transactionBuilder.skuId,
              transactionBuilder.amount()
                  .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, transactionBuilder.type)
          billingAnalytics.sendPaymentEvent(data.domain, transactionBuilder.skuId,
              transactionBuilder.amount()
                  .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER, transactionBuilder.type)
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
              navigator.navigateToError(
                  R.string.purchase_error_wallet_block_code_403, false)
            } else {
              navigator.navigateToWalletValidation(
                  R.string.purchase_error_wallet_block_code_403)
            }
          } else {
            navigator.navigateToError(R.string.purchase_error_wallet_block_code_403, false)
          }
        }
        .ignoreElement()
  }


  fun stop() = disposables.clear()

}
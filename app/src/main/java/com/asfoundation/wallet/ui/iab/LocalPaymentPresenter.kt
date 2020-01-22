package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import com.asfoundation.wallet.GlideApp
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val originalAmount: String?,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String?,
                            private val paymentId: String,
                            private val developerAddress: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val navigator: FragmentNavigator,
                            private val type: String,
                            private val amount: BigDecimal,
                            private val analytics: LocalPaymentAnalytics,
                            private val savedInstance: Bundle?,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val disposables: CompositeDisposable,
                            private val callbackUrl: String?,
                            private val orderReference: String?,
                            private val payload: String?,
                            private val context: Context?,
                            private val paymentMethodIcon: String?) {

  private var waitingResult: Boolean = false

  fun present() {
    if (savedInstance != null) {
      waitingResult = savedInstance.getBoolean(WAITING_RESULT)
    }
    onViewCreatedRequestLink()
    handlePaymentRedirect()
    handleOkErrorButtonClick()
    handleOkBuyButtonClick()
  }

  fun handleStop() {
    waitingResult = false
    disposables.clear()
  }

  fun preparePendingUserPayment() {
    disposables.add(
        Observable.fromCallable {
          GlideApp.with(context!!)
              .asBitmap()
              .load(paymentMethodIcon)
              .override(getWidth(), getHeight())
              .centerCrop()
              .submit()
              .get()
        }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .subscribe({ view.showPendingUserPayment(it) }, { showError(it) }))
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
        localPaymentInteractor.getPaymentLink(domain, skuId, originalAmount, currency,
            paymentId, developerAddress, callbackUrl, orderReference,
            payload).filter { !waitingResult }.observeOn(
            viewScheduler).doOnSuccess {
          analytics.sendPaymentMethodDetailsEvent(domain, skuId, amount.toString(), type, paymentId)
          navigator.navigateToUriForResult(it)
          waitingResult = true
        }.subscribeOn(networkScheduler).observeOn(viewScheduler)
            .subscribe({ }, { showError(it) }))
  }

  private fun handlePaymentRedirect() {
    disposables.add(navigator.uriResults()
        .doOnNext { view.showProcessingLoading() }
        .doOnNext { view.lockRotation() }
        .flatMap {
          localPaymentInteractor.getTransaction(it)
              .subscribeOn(networkScheduler)
        }
        .observeOn(viewScheduler)
        .flatMapCompletable { handleTransactionStatus(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handleOkErrorButtonClick() {
    disposables.add(view.getOkErrorClick().observeOn(
        viewScheduler).doOnNext { view.dismissError() }.subscribe())
  }

  private fun handleOkBuyButtonClick() {
    disposables.add(
        view.getGotItClick()
            .observeOn(viewScheduler)
            .doOnNext { view.close() }
            .subscribe()
    )
  }

  private fun handleTransactionStatus(transaction: Transaction): Completable {
    view.hideLoading()
    return when (transaction.status) {
      Status.COMPLETED -> {
        localPaymentInteractor.getCompletePurchaseBundle(type, domain, skuId,
            transaction.orderReference, transaction.hash, networkScheduler)
            .doOnSuccess {
              analytics.sendPaymentEvent(domain, skuId, amount.toString(), type, paymentId)
              analytics.sendRevenueEvent(disposables, amount)
            }
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .flatMapCompletable {
              Completable.fromAction {
                view.showCompletedPayment()
              }
                  .andThen(Completable.timer(view.getAnimationDuration(), TimeUnit.MILLISECONDS))
                  .andThen(Completable.fromAction { view.popView(it) })
            }
      }
      Status.PENDING_USER_PAYMENT -> Completable.fromAction {
        localPaymentInteractor.savePreSelectedPaymentMethod(paymentId)
        localPaymentInteractor.saveAsyncLocalPayment(paymentId)
        preparePendingUserPayment()
        analytics.sendPaymentEvent(domain, skuId, amount.toString(), type, paymentId)
      }.subscribeOn(viewScheduler)
      else -> Completable.fromAction {
        view.showError()
      }.subscribeOn(viewScheduler)
    }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }

  private fun getWidth(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 184.toFloat(),
        context?.resources?.displayMetrics)
        .toInt()
  }

  private fun getHeight(): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 80.toFloat(),
        context?.resources?.displayMetrics)
        .toInt()
  }

  companion object {
    private const val WAITING_RESULT = "WAITING_RESULT"
  }
}


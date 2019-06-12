package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction.Status
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

private val WAITING_RESULT = "WAITING_RESULT"
private var waitingResult: Boolean = false


class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val originalAmount: String?,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String?,
                            private val paymentId: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val navigator: FragmentNavigator,
                            private val savedInstance: Bundle?,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler,
                            private val disposables: CompositeDisposable) {

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

  private fun onViewCreatedRequestLink() {
    disposables.add(
        localPaymentInteractor.getPaymentLink(domain, skuId, originalAmount, currency,
            paymentId).filter { !waitingResult }.observeOn(
            viewScheduler).doOnSuccess {
          navigator.navigateToUriForResult(it, "", domain, skuId, null, "")
          waitingResult = true
        }.subscribeOn(networkScheduler).subscribe({ }, { showError(it) }))
  }

  private fun handlePaymentRedirect() {
    disposables.add(
        navigator.uriResults().doOnNext {
          view.showProcessingLoading()
        }.flatMap {
          localPaymentInteractor.getTransaction(it)
        }.firstElement()
            .observeOn(viewScheduler)
            .flatMapCompletable { handleTransactionStatus(it) }
            .subscribe({ }, { showError(it) }))
  }

  private fun handleOkErrorButtonClick() {
    disposables.add(view.getOkErrorClick().observeOn(
        viewScheduler).doOnNext { view.dismissError() }.subscribe())
  }

  private fun handleOkBuyButtonClick() {
    disposables.add(view.getOkBuyClick().observeOn(
        viewScheduler).doOnNext { view.close() }.subscribe())
  }

  private fun handleTransactionStatus(transactionStatus: Status): Completable {
    view.hideLoading()
    return when (transactionStatus) {
      Status.COMPLETED -> {
        Completable.fromAction {
          view.showCompletedPayment()
        }
      }
      Status.PENDING_USER_PAYMENT -> Completable.fromAction {
        view.showPendingUserPayment()
      }
      else -> Completable.fromAction {
        view.showError()
      }
    }
  }

  private fun showError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  fun onSaveInstanceState(outState: Bundle) {
    outState.putBoolean(WAITING_RESULT, waitingResult)
  }
}


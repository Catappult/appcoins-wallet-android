package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class LocalPaymentPresenter(private val view: LocalPaymentView,
                            private val amount: String?,
                            private val currency: String?,
                            private val domain: String,
                            private val skuId: String,
                            private val paymentId: String,
                            private val localPaymentInteractor: LocalPaymentInteractor,
                            private val navigator: FragmentNavigator,
                            private val disposables: CompositeDisposable) {
  fun present() {
    onViewCreatedRequestLink()
    handlePaymentRedirect()
    handleOkErrorButtonClick()
    handleOkBuyButtonClick()
  }


  fun handleStop() {
    disposables.clear()
  }

  private fun onViewCreatedRequestLink() {
    disposables.add(
        localPaymentInteractor.getPaymentLink(domain, skuId, amount, currency,
            paymentId).observeOn(AndroidSchedulers.mainThread()).doOnSuccess {
          navigator.navigateToUriForResult(it, "", domain, skuId, null, "")
        }.subscribeOn(Schedulers.io()).subscribe({ }, { showError(it) }))
  }

  private fun handlePaymentRedirect() {
    disposables.add(
        navigator.uriResults().doOnNext {
          view.showProcessingLoading()
        }.flatMap {
          localPaymentInteractor.getTransaction(it)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .flatMapCompletable { handleTransactionStatus(it) }
            .subscribe({ }, { showError(it) }))
  }

  private fun handleOkErrorButtonClick() {
    disposables.add(view.getOkErrorClick().observeOn(
        AndroidSchedulers.mainThread()).doOnNext { view.dismissError() }.subscribe())
  }

  private fun handleOkBuyButtonClick() {
    disposables.add(view.getOkBuyClick().observeOn(
        AndroidSchedulers.mainThread()).doOnNext { view.dismissError() }.subscribe())
  }

  private fun handleTransactionStatus(transactionStatus: Transaction.Status): Completable {
    return when (transactionStatus) {
      Transaction.Status.COMPLETED -> {
        Completable.fromAction {
          view.hideLoading()
        }
      }
      Transaction.Status.PENDING_USER_PAYMENT -> Completable.fromAction {
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
}


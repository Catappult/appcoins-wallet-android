package com.asfoundation.wallet.verification.ui.credit_card.error

import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class VerificationErrorPresenter(private val view: VerificationErrorView,
                                 private val data: VerificationErrorData,
                                 private val navigator: VerificationErrorNavigator,
                                 private val disposable: CompositeDisposable) {


  fun present() {
    initializeView()
    handleTryAgainClicks()
    handleMaybeLaterClicks()
  }

  private fun handleTryAgainClicks() {
    disposable.add(
        Observable.merge(view.getTryAgainClicks(), view.getTryAgainAttemptsClicks())
            .doOnNext {
              if (data.errorType == VerificationCodeResult.ErrorType.TOO_MANY_ATTEMPTS) {
                navigator.navigateToInitialWalletVerification()
              } else {
                navigator.navigateToCodeWalletVerification()
              }
            }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleMaybeLaterClicks() {
    disposable.add(
        view.getMaybeLaterClicks()
            .doOnNext { navigator.cancel() }
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun initializeView() {
    view.initializeView(data.errorType, data.amount, data.symbol)
  }

  fun stop() = disposable.clear()

}
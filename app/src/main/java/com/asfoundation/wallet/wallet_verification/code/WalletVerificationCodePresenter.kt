package com.asfoundation.wallet.wallet_verification.code

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationCodePresenter(private val view: WalletVerificationCodeView,
                                      private val disposable: CompositeDisposable,
                                      private val viewScheduler: Scheduler,
                                      private val ioScheduler: Scheduler,
                                      private val interactor: WalletVerificationCodeInteractor,
                                      private val navigator: WalletVerificationCodeNavigator) {

  fun present(savedInstanceState: Bundle?) {
    handleConfirmClicks()
    handleLaterClicks()
    handleAnotherCardClicks()
  }

  private fun handleAnotherCardClicks() {
    disposable.add(
        view.getChangeCardClicks()
            .subscribe()
    )
  }

  private fun handleLaterClicks() {
    disposable.add(
        view.getMaybeLaterClicks()
            .doOnNext { navigator.cancel() }
            .subscribe()
    )
  }

  private fun handleConfirmClicks() {
    disposable.add(
        view.getConfirmClicks()
            .doOnNext {
              view.hideKeyboard()
              view.lockRotation()
              view.showLoading()
            }
            .observeOn(ioScheduler)
            .flatMapSingle {
              interactor.confirmCode(it)
                  .observeOn(viewScheduler)
                  .doOnSuccess { view.unlockRotation() }
                  .doOnSuccess { result -> handleCodeConfirmationStatus(result) }
            }
            .subscribe()
    )
  }

  private fun handleCodeConfirmationStatus(codeResult: VerificationCodeResult) {

  }

  fun stop() = disposable.clear()
}

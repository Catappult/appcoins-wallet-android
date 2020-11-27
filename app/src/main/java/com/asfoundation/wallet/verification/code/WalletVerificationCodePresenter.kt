package com.asfoundation.wallet.verification.code

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationCodePresenter(private val view: WalletVerificationCodeView,
                                      private val disposable: CompositeDisposable,
                                      private val viewScheduler: Scheduler,
                                      private val ioScheduler: Scheduler,
                                      private val interactor: WalletVerificationCodeInteractor,
                                      private val navigator: WalletVerificationCodeNavigator,
                                      private val logger: Logger) {

  companion object {

    private val TAG = WalletVerificationCodePresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    handleConfirmClicks()
    handleLaterClicks()
    handleAnotherCardClicks()
  }

  fun loadInfo(savedInstance: Bundle?) {
    disposable.add(
        interactor.loadVerificationIntroModel()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.updateUi(it, savedInstance)
              view.hideLoading()
              view.unlockRotation()
            }
            .doOnSubscribe {
              view.lockRotation()
              view.showLoading()
            }
            .subscribe({}, {
              logger.log(TAG, it)
              view.showGenericError()
            })
    )
  }

  private fun handleAnotherCardClicks() {
    disposable.add(
        view.getChangeCardClicks()
            .doOnNext { navigator.navigateToInitialWalletVerification() }
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

  fun onAnimationEnd() = navigator.finish()

  fun stop() = disposable.clear()
}

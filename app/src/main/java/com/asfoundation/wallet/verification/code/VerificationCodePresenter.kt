package com.asfoundation.wallet.verification.code

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class VerificationCodePresenter(private val view: VerificationCodeView,
                                private val data: VerificationCodeData,
                                private val disposable: CompositeDisposable,
                                private val viewScheduler: Scheduler,
                                private val ioScheduler: Scheduler,
                                private val interactor: VerificationCodeInteractor,
                                private val navigator: VerificationCodeNavigator,
                                private val logger: Logger) {

  companion object {
    private val TAG = VerificationCodePresenter::class.java.name
  }

  fun present(savedInstanceState: Bundle?) {
    initializeView(savedInstanceState)
    handleConfirmClicks()
    handleLaterClicks()
    handleRetryClick()
    handleAnotherCardClicks()
  }

  private fun initializeView(savedInstanceState: Bundle?) {
    if (data.loaded) view.setupUi(data, savedInstanceState)
    else loadInfo(savedInstanceState)
  }

  private fun handleRetryClick() {
    disposable.add(Observable.merge(view.retryClick(), view.getTryAgainClicks())
        .observeOn(viewScheduler)
        .doOnNext {
          view.showVerificationCode()
          view.unlockRotation()
        }
        .subscribe({}, { it.printStackTrace() }))
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
            .observeOn(viewScheduler)
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
    if (codeResult.success && !codeResult.error.hasError) {
      view.showSuccess()
    } else {
      if (codeResult.error.hasError && codeResult.isCodeError) {
        view.hideLoading()
        view.showWrongCodeError()
      } else {
        logger.log("VerificationCodePresenter",
            "${codeResult.error.code}: ${codeResult.error.message}")
        view.showGenericError()
      }
    }
  }

  fun onAnimationEnd() = navigator.finish()

  fun stop() = disposable.clear()
}

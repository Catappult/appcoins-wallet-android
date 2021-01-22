package com.asfoundation.wallet.verification.code

import android.os.Bundle
import com.appcoins.wallet.billing.adyen.VerificationCodeResult
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.verification.VerificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class VerificationCodePresenter(private val view: VerificationCodeView,
                                private val data: VerificationCodeData,
                                private val disposable: CompositeDisposable,
                                private val viewScheduler: Scheduler,
                                private val ioScheduler: Scheduler,
                                private val interactor: VerificationCodeInteractor,
                                private val navigator: VerificationCodeNavigator,
                                private val logger: Logger,
                                private val analytics: VerificationAnalytics) {

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
    disposable.add(view.retryClick()
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
              navigator.navigateToError(VerificationCodeResult.ErrorType.OTHER, data.amount,
                  data.symbol)
            })
    )
  }

  private fun handleAnotherCardClicks() {
    disposable.add(
        view.getChangeCardClicks()
            .observeOn(viewScheduler)
            .doOnNext {
              analytics.sendConfirmEvent("try_with_another_card")
              navigator.navigateToInitialWalletVerification()
            }
            .subscribe()
    )
  }

  private fun handleLaterClicks() {
    disposable.add(
        view.getMaybeLaterClicks()
            .doOnNext {
              analytics.sendConfirmEvent("maybe_later")
              navigator.cancel()
            }
            .subscribe()
    )
  }

  private fun handleConfirmClicks() {
    disposable.add(
        view.getConfirmClicks()
            .doOnNext {
              analytics.sendConfirmEvent("confirm")
              view.hideKeyboard()
              view.lockRotation()
              view.showLoading()
            }
            .observeOn(ioScheduler)
            .flatMapSingle {
              interactor.confirmCode(it)
                  .observeOn(viewScheduler)
                  .doOnSuccess { view.unlockRotation() }
                  .doOnSuccess { result ->
                    handleCodeConfirmationStatus(result)
                    analytics.sendConclusionEvent(result.success,
                        result.error.code?.toString(), result.error.message)
                  }
            }
            .subscribe()
    )
  }

  private fun handleCodeConfirmationStatus(codeResult: VerificationCodeResult) {
    view.hideLoading()
    if (codeResult.success && !codeResult.error.hasError) {
      view.showSuccess()
    } else {
      logger.log("VerificationCodePresenter",
          "${codeResult.error.code}: ${codeResult.error.message}")
      when (codeResult.errorType) {
        VerificationCodeResult.ErrorType.WRONG_CODE -> {
          view.showWrongCodeError()
        }
        else -> {
          val errorCode = codeResult.errorType ?: VerificationCodeResult.ErrorType.OTHER
          navigator.navigateToError(errorCode, data.amount, data.symbol)
        }
      }
    }
  }

  fun onAnimationEnd() = navigator.finish()

  fun stop() = disposable.clear()
}

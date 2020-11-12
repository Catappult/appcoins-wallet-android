package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler,
                          private val networkScheduler: Scheduler,
                          private val walletCreated: ReplaySubject<Boolean>,
                          private val logger: Logger) {

  private var hasShowedWarning = false

  fun present() {
    handleAvailablePaymentMethods()
    handleSkipClicks()
    handleSkippedOnboarding()
    handleLinkClick()
    handleCreateWallet()
    handleNextButtonClicks()
    handleWarningText()
  }

  private fun handleAvailablePaymentMethods() {
    disposables.add(onboardingInteract.getPaymentMethodsIcons()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setPaymentMethodsIcons(it) }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  fun markedWarningTextAsShowed() {
    hasShowedWarning = true
  }

  private fun handleWarningText() {
    disposables.add(Observable.timer(5, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.showWarningText() }
        .repeatUntil { hasShowedWarning }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun stop() = disposables.clear()

  private fun handleSkipClicks() {
    disposables.add(view.getSkipClicks()
        .doOnNext { view.showViewPagerLastPage() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun isWalletCreated() = walletCreated.filter { it }

  private fun handleWalletCreation() {
    if (walletCreated.hasValue()) {
      handleFinishNavigation(false, 0)
    } else {
      view.showLoading()
      handleFinishNavigation(true, 1)
    }
  }

  private fun handleFinishNavigation(showAnimation: Boolean, delay: Long) {
    disposables.add(isWalletCreated()
        .flatMapSingle { onboardingInteract.getWalletAddress() }
        .delay(delay, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { finishOnBoarding(showAnimation) }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handleNextButtonClicks() {
    disposables.add(view.getNextButtonClick()
        .doOnNext { handleWalletCreation() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleCreateWallet() {
    disposables.add(onboardingInteract.getWalletAddress()
        .observeOn(viewScheduler)
        .flatMapCompletable { Completable.fromAction { walletCreated.onNext(true) } }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handleSkippedOnboarding() {
    disposables.add(Observable.zip(isWalletCreated(),
        Observable.fromCallable { onboardingInteract.hasClickedSkipOnboarding() }
            .filter { clicked -> clicked },
        Observable.fromCallable { onboardingInteract.hasOnboardingCompleted() }
            .filter { clicked -> clicked },
        Function3 { _: Any, _: Any, _: Any -> }
    )
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { finishOnBoarding(true) }
        .subscribe({}, { logger.log(TAG, it) })
    )
  }

  private fun handleLinkClick() {
    disposables.add(view.getLinkClick()
        .doOnNext { uri -> view.navigateToBrowser(Uri.parse(uri)) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun markOnboardingCompleted() = onboardingInteract.finishOnboarding()

  private fun finishOnBoarding(showAnimation: Boolean) {
    onboardingInteract.clickSkipOnboarding()
    view.finishOnboarding(showAnimation)
  }

  companion object {
    private val TAG = OnboardingPresenter::class.java.name
  }
}
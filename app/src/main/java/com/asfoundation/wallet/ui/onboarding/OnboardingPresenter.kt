package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.util.scaleToString
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
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
    handleSetupUI()
    handleSkipClicks()
    handleSkippedOnboarding()
    handleLinkClick()
    handleCreateWallet()
    handleRedeemButtonClicks()
    handleNextButtonClicks()
    handleLaterClicks()
    handleRetryClicks()
    handleWarningText()
  }

  private fun handleAvailablePaymentMethods() {
    disposables.add(onboardingInteract.getPaymentMethodsIcons()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setPaymentMethodsIcons(it) }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handleSetupUI() {
    disposables.add(onboardingInteract.getReferralInfo()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.updateUI(it.symbol + it.maxAmount.scaleToString(2), it.isActive) }
        .subscribe({}, { logger.log(TAG, it) })
    )
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

  private fun handleRetryClicks() {
    disposables.add(view.getRetryButtonClicks()
        .doOnNext { handleWalletCreation(skipValidation = false, showAnimation = false) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleLaterClicks() {
    disposables.add(view.getLaterButtonClicks()
        .doOnNext { handleValidationStatus(WalletValidationStatus.SUCCESS, false) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleRedeemButtonClicks() {
    disposables.add(view.getRedeemButtonClick()
        .observeOn(viewScheduler)
        .doOnNext { handleWalletCreation(skipValidation = false, showAnimation = true) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun handleWalletCreation(skipValidation: Boolean, showAnimation: Boolean) {
    if (walletCreated.hasValue() || !showAnimation) {
      handleFinishNavigation(skipValidation, false, 0)
    } else {
      view.showLoading()
      handleFinishNavigation(skipValidation, showAnimation, 1)
    }
  }

  private fun handleFinishNavigation(skipValidation: Boolean, showAnimation: Boolean, delay: Long) {
    disposables.add(isWalletCreated()
        .flatMapSingle { onboardingInteract.getWalletAddress() }
        .flatMapSingle {
          if (skipValidation) {
            Single.just(WalletValidationStatus.SUCCESS)
          } else {
            onboardingInteract.isAddressValid(it)
                .subscribeOn(networkScheduler)
          }
        }
        .delay(delay, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { handleValidationStatus(it, showAnimation) }
        .subscribe({}, { logger.log(TAG, it) }))
  }

  private fun handleValidationStatus(walletValidationStatus: WalletValidationStatus,
                                     showAnimation: Boolean) {
    if (walletValidationStatus == WalletValidationStatus.NO_NETWORK) {
      view.showNoInternetView()
    } else {
      finishOnBoarding(walletValidationStatus, showAnimation)
    }
  }

  private fun handleNextButtonClicks() {
    disposables.add(view.getNextButtonClick()
        .doOnNext { handleWalletCreation(skipValidation = true, showAnimation = true) }
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
        .doOnNext { handleValidationStatus(WalletValidationStatus.SUCCESS, true) }
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

  private fun finishOnBoarding(walletValidationStatus: WalletValidationStatus,
                               showAnimation: Boolean) {
    onboardingInteract.clickSkipOnboarding()
    view.finishOnboarding(walletValidationStatus, showAnimation)
  }

  companion object {
    private val TAG = OnboardingPresenter::class.java.name
  }
}
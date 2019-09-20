package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.referrals.ReferralInteractorContract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.subjects.ReplaySubject
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler,
                          private val smsValidationInteract: SmsValidationInteract,
                          private val networkScheduler: Scheduler,
                          private val walletCreated: ReplaySubject<Boolean>,
                          private val referralInteractor: ReferralInteractorContract) {

  private var hasShowedWarning = false

  fun present() {
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

  private fun handleSetupUI() {
    disposables.add(referralInteractor.getReferralInfo()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.updateUI(it.symbol + it.amount.multiply(BigDecimal(it.available))) }
        .subscribe({}, { throwable -> throwable.printStackTrace() })
    )
  }

  fun markedWarningTextAsShowed() {
    hasShowedWarning = true
  }

  private fun handleWarningText() {
    disposables.add(
        Observable.timer(5, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { view.showWarningText() }
            .repeatUntil { hasShowedWarning }
            .subscribe()
    )
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleSkipClicks() {
    disposables.add(
        view.getSkipClicks()
            .doOnNext { view.showViewPagerLastPage() }
            .subscribe()
    )
  }

  private fun isWalletCreated(): Observable<Boolean> {
    return walletCreated.filter { created -> created }
  }

  private fun handleRetryClicks() {
    disposables.add(
        view.getRetryButtonClicks()
            .doOnNext { handleWalletCreation(skipValidation = false, showAnimation = false) }
            .subscribe()
    )
  }

  private fun handleLaterClicks() {
    disposables.add(
        view.getLaterButtonClicks()
            .doOnNext {
              handleValidationStatus(WalletValidationStatus.SUCCESS, false)
            }.subscribe())
  }

  private fun handleRedeemButtonClicks() {
    disposables.add(
        view.getRedeemButtonClick()
            .observeOn(viewScheduler)
            .doOnNext {
              view.showLoading()
              handleWalletCreation(skipValidation = false, showAnimation = true)
            }
            .subscribe()
    )
  }

  private fun handleWalletCreation(skipValidation: Boolean, showAnimation: Boolean) {
    disposables.add(isWalletCreated()
        .flatMapSingle { onboardingInteract.getWalletAddress() }
        .flatMapSingle {
          if (skipValidation) {
            Single.just(WalletValidationStatus.SUCCESS)
          } else {
            smsValidationInteract.isValid(Wallet(it))
                .subscribeOn(networkScheduler)
          }
        }
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { handleValidationStatus(it, showAnimation) }
        .subscribe())
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
    disposables.add(
        view.getNextButtonClick()
            .doOnNext {
              view.showLoading()
              handleWalletCreation(skipValidation = true, showAnimation = true)
            }
            .subscribe()
    )
  }

  private fun handleCreateWallet() {
    disposables.add(
        onboardingInteract.getWalletAddress()
            .onErrorResumeNext {
              onboardingInteract.createWallet()
            }
            .flatMapCompletable { Completable.fromAction { walletCreated.onNext(true) } }
            .subscribe())
  }

  private fun handleSkippedOnboarding() {
    disposables.add(
        Observable.zip(isWalletCreated(),
            Observable.fromCallable { onboardingInteract.hasClickedSkipOnboarding() }.filter { clicked -> clicked },
            Observable.fromCallable { onboardingInteract.hasOnboardingCompleted() }.filter { clicked -> clicked },
            Function3 { _: Any, _: Any, _: Any -> }
        )
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { handleValidationStatus(WalletValidationStatus.SUCCESS, true) }
            .subscribe()
    )
  }

  private fun handleLinkClick() {
    disposables.add(
        view.getLinkClick()
            .doOnNext { uri -> view.navigateToBrowser(Uri.parse(uri)) }
            .subscribe()
    )
  }

  fun markOnboardingCompleted() {
    onboardingInteract.finishOnboarding()
  }

  private fun finishOnBoarding(walletValidationStatus: WalletValidationStatus,
                               showAnimation: Boolean) {
    onboardingInteract.clickSkipOnboarding()
    view.finishOnboarding(walletValidationStatus, showAnimation)
  }
}
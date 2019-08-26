package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler,
                          private val smsValidationInteract: SmsValidationInteract,
                          private val networkScheduler: Scheduler,
                          private val walletCreated: PublishSubject<Boolean>) {

  fun present() {
    view.setupUi()
    handleSkipClicks()
    handleSkippedOnboarding()
    handleLinkClick()
    handleCreateWallet()
    handleRedeemButtonClicks()
    handleNextButtonClicks()
    handleLaterClicks()
    handleRetryClicks()
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
        Observable.combineLatest(isWalletCreated(), view.getRetryButtonClicks(),
            BiFunction { _: Boolean, _: Any -> }
        )
            .flatMapSingle { onboardingInteract.getWalletAddress() }
            .flatMapSingle {
              smsValidationInteract.isValid(Wallet(it))
                  .subscribeOn(networkScheduler)
            }
            .observeOn(viewScheduler)
            .doOnNext { handleValidationStatus(it, false) }
            .subscribe()
    )
  }

  private fun handleLaterClicks() {
    disposables.add(
        view.getLaterButtonClicks()
            .doOnNext {
              finishOnBoarding(WalletValidationStatus.SUCCESS, false)
            }.subscribe())
  }

  private fun handleRedeemButtonClicks() {
    disposables.add(
        Observable.zip(isWalletCreated(), view.getRedeemButtonClick(),
            BiFunction { _: Boolean, _: Any -> }
        )
            .observeOn(viewScheduler)
            .doOnNext { view.showLoading() }
            .flatMapSingle { onboardingInteract.getWalletAddress() }
            .flatMapSingle {
              smsValidationInteract.isValid(Wallet(it))
                  .subscribeOn(networkScheduler)
            }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { handleValidationStatus(it, true) }
            .subscribe()
    )
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
        Observable.zip(isWalletCreated(), view.getNextButtonClick(),
            BiFunction { _: Boolean, _: Any -> }
        )
            .observeOn(viewScheduler)
            .doOnNext { view.showLoading() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .doOnNext { finishOnBoarding(WalletValidationStatus.SUCCESS, true) }
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
            .doOnNext { finishOnBoarding(WalletValidationStatus.SUCCESS, true) }
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
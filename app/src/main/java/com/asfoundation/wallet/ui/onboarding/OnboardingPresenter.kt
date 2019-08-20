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
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler,
                          private val smsValidationInteract: SmsValidationInteract,
                          private val networkScheduler: Scheduler) {

  private var walletCreated: PublishSubject<Boolean> = PublishSubject.create()

  fun present() {
    handleSkipedOnboarding()
    handleLinkClick()
    handleCreateWallet()
    handleRedeemButtonClicks()
    handleNextButtonClicks()
  }

  fun stop() {
    disposables.clear()
  }

  private fun isWalletCreated(): Observable<Boolean> {
    return walletCreated.filter { created -> created }
  }

  private fun handleRedeemButtonClicks() {
    disposables.add(
        Observable.zip(isWalletCreated(), view.getRedeemButtonClick(),
            BiFunction { _: Boolean, _: Any -> }
        )
            .doOnEach { view.showLoading() }
            .flatMapSingle { onboardingInteract.getWalletAddress() }
            .flatMapSingle {
              smsValidationInteract.isValid(Wallet(it))
                  .subscribeOn(networkScheduler)
            }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .subscribe({ finishOnBoarding(it) }, {})
    )
  }

  private fun handleNextButtonClicks() {
    disposables.add(
        Observable.zip(isWalletCreated(), view.getNextButtonClick(),
            BiFunction { _: Boolean, _: Any -> }
        )
            .doOnEach { view.showLoading() }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .subscribe({ finishOnBoarding(null) }, {})
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

  private fun handleSkipedOnboarding() {
    disposables.add(
        Observable.zip(isWalletCreated(),
            Observable.fromCallable { onboardingInteract.hasClickedSkipOnboarding() }.filter { clicked -> clicked },
            BiFunction { _: Boolean, _: Any -> }
        )
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .subscribe({ finishOnBoarding(null) }, {})
    )
  }

  private fun handleLinkClick() {
    view.getLinkClick()
        ?.doOnNext { uri ->
          view.navigateToBrowser(Uri.parse(uri))
        }
        ?.subscribe()
        ?.let { disposables.add(it) }
  }

  private fun finishOnBoarding(walletValidationStatus: WalletValidationStatus?) {
    onboardingInteract.clickSkipOnboarding()
    onboardingInteract.finishOnboarding()
    view.finishOnboarding(walletValidationStatus)
  }
}
package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import com.asfoundation.wallet.entity.Wallet
import com.asfoundation.wallet.interact.SmsValidationInteract
import com.asfoundation.wallet.poa_wallet_validation.WalletValidationStatus
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler,
                          private val smsValidationInteract: SmsValidationInteract,
                          private val networkScheduler: Scheduler) {

  fun present() {
    handleOnBoardingFinish()
    handleLinkClick()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleNext(): Observable<Any> {
    return Observable.fromCallable { onboardingInteract.hasClickedSkipOnboarding() }
        .flatMap {
          if (it) {
            view.showLoading()
            return@flatMap Observable.just(true)
          }
          return@flatMap handleNextButtonClick()
        }
  }

  private fun handleNextButtonClick(): Observable<Any> {
    return view.getNextButtonClick()
        .doOnNext {
          onboardingInteract.clickSkipOnboarding()
          view.showLoading()
        }
  }

  private fun handleLinkClick() {
    view.getLinkClick()
        ?.doOnNext { uri ->
          view.navigateToBrowser(Uri.parse(uri))
        }
        ?.subscribe()
        ?.let { disposables.add(it) }
  }

  private fun handleOnBoardingFinish() {
    disposables.add(
        Observable.zip(handleGetWalletAddress(), handleNext(),
            BiFunction { walletAddress: String, _: Any -> walletAddress }
        )
            .flatMapSingle { smsValidationInteract.isValid(Wallet(it)) }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .subscribeOn(networkScheduler)
            .subscribe({ finishOnBoarding(it) }, {})
    )
  }

  private fun handleGetWalletAddress(): Observable<String> {
    return onboardingInteract.getWalletAddress()
        .onErrorResumeNext {
          onboardingInteract.createWallet()
        }
        .toObservable()
  }

  private fun finishOnBoarding(walletValidationStatus: WalletValidationStatus) {
    onboardingInteract.finishOnboarding()
    view.finishOnboarding(walletValidationStatus)
  }
}
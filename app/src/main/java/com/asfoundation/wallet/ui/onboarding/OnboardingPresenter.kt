package com.asfoundation.wallet.ui.onboarding

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val onboardingInteract: OnboardingInteract,
                          private val viewScheduler: Scheduler) {

  fun present() {
    handleOnBoardingFinish()
    handleLinkClick()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleSkip(): Observable<Any> {
    return Observable.fromCallable { onboardingInteract.hasClickedSkipOnboarding() }
        .flatMap {
          if (it) {
            view.showLoading()
            return@flatMap Observable.just(true)
          }
          return@flatMap handleSkipButtonClick()
        }
  }

  private fun handleSkipButtonClick(): Observable<Any> {
    return view.getSkipButtonClick()
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
        Observable.zip(handleGetWalletAddress(), handleSkip(),
            BiFunction { _: String, _: Any -> true })
            .delay(1, TimeUnit.SECONDS)
            .observeOn(viewScheduler)
            .subscribe({ finishOnBoarding() }, {})
    )
  }

  private fun handleGetWalletAddress(): Observable<String> {
    return onboardingInteract.getWalletAddress()
        .onErrorResumeNext {
          onboardingInteract.createWallet()
        }
        .toObservable()
  }

  private fun finishOnBoarding() {
    onboardingInteract.finishOnboarding()
    view.finishOnboarding()
  }
}
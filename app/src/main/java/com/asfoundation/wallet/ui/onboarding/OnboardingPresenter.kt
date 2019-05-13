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
    view.setupUi()
    handleOnBoardingFinish()
    handleLinkClick()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleSkipButtonClick(): Observable<Any> {
    return view.getSkipButtonClick().doOnNext {
      view.showLoading()
    }.delay(1, TimeUnit.SECONDS)
  }

  private fun handleLinkClick() {
    view.getLinkClick()?.doOnNext { uri ->
      view.navigateToBrowser(Uri.parse(uri))
    }?.subscribe()?.let { disposables.add(it) }
  }

  private fun handleOnBoardingFinish() {
    disposables.add(Observable.zip(handleGetWalletAddress().observeOn(viewScheduler),
        handleSkipButtonClick().observeOn(viewScheduler),
        BiFunction { walletAddress: String, _: Any ->
          if (!walletAddress.isEmpty()) {
            onboardingInteract.finishOnboarding()
            view.finishOnboarding()
          }
        }).subscribe())
  }

  private fun handleGetWalletAddress(): Observable<String> {
    return onboardingInteract.getWalletAddress()
        .onErrorResumeNext {
          onboardingInteract.createWallet()
        }
        .toObservable()
  }
}
package com.asfoundation.wallet.ui.onboarding

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.interact.CreateWalletInteract
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit

class OnboardingPresenter(private val disposables: CompositeDisposable,
                          private val view: OnboardingView,
                          private val walletCreateInteractor: CreateWalletInteract,
                          private val walletService: WalletService,
                          private val viewScheduler: Scheduler) {

  fun present() {
    view.setupUi()
    handleOnBoardingFinish()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleSkipButtonClick(): Observable<Any> {
    return view.getSkipButtonClick().doOnNext {
      view.showLoading()
    }.delay(1, TimeUnit.SECONDS)
  }

  private fun handleOnBoardingFinish() {
    disposables.add(Observable.zip(handleGetWalletAddress().observeOn(viewScheduler),
        handleSkipButtonClick().observeOn(viewScheduler),
        BiFunction { walletAddress: String, _: Any ->
          if (!walletAddress.isEmpty()) {
            view.finishOnboarding()
          }
        }).subscribe())
  }

  private fun handleGetWalletAddress(): Observable<String> {
    return walletService.getWalletAddress()
        .onErrorResumeNext {
          walletCreateInteractor.create()
              .map { wallet -> wallet.address }
        }
        .toObservable()
  }
}
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
    handleCheckboxClick()
    handlePageScroll()
    handleOnBoardingFinish()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleSkipClick(): Observable<Any> {
    return view.getSkipClick().doOnNext {
      view.showLoading()
    }.delay(1, TimeUnit.SECONDS)
  }

  private fun handleCheckboxClick() {
    disposables.add(view.getCheckboxClick().subscribe())
  }

  private fun handlePageScroll() {

  }

  private fun handleOnBoardingFinish() {
    disposables.add(Observable.zip(handleGetWalletAddress().observeOn(viewScheduler),
        handleSkipClick().observeOn(viewScheduler),
        BiFunction { walletAddress: String, _: Any ->
          if (!walletAddress.isNullOrEmpty()) {
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
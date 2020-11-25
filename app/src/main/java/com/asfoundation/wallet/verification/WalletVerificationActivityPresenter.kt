package com.asfoundation.wallet.verification

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationActivityPresenter(
    private val navigator: WalletVerificationActivityNavigator,
    private val interactor: WalletVerificationActivityInteractor,
    private val viewScheduler: Scheduler,
    private val ioScheduler: Scheduler,
    private val disposable: CompositeDisposable
) {


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) handleVerificationStatus()
  }

  private fun handleVerificationStatus() {
    disposable.add(
        interactor.getVerificationStatus()
            .subscribeOn(ioScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess { onVerificationStatusSuccess(it) }
            .subscribe()
    )
  }

  private fun onVerificationStatusSuccess(walletVerificationStatus: WalletVerificationStatus) {
    when (walletVerificationStatus) {
      WalletVerificationStatus.UNVERIFIED -> navigator.navigateToWalletVerificationIntro()
      WalletVerificationStatus.CODE_REQUESTED -> navigator.navigateToWalletVerificationCode()
      WalletVerificationStatus.VERIFIED -> navigator.finish()
    }
  }


}
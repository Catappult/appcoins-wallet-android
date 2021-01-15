package com.asfoundation.wallet.verification

import android.os.Bundle
import com.asfoundation.wallet.verification.network.VerificationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class VerificationActivityPresenter(
    private val navigator: VerificationActivityNavigator,
    private val interactor: VerificationActivityInteractor,
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
            .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun onVerificationStatusSuccess(verificationStatus: VerificationStatus) {
    when (verificationStatus) {
      VerificationStatus.UNVERIFIED -> navigator.navigateToWalletVerificationIntro()
      VerificationStatus.CODE_REQUESTED -> navigator.navigateToWalletVerificationCode()
      VerificationStatus.VERIFIED -> navigator.finish()
    }
  }
}
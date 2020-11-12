package com.asfoundation.wallet.wallet_verification.code

import android.os.Bundle
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationCodePresenter(private val view: WalletVerificationCodeView,
                                      private val disposable: CompositeDisposable,
                                      private val viewScheduler: Scheduler,
                                      private val computationScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {

  }

  fun stop() = disposable.clear()
}

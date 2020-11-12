package com.asfoundation.wallet.wallet_verification.intro

import android.os.Bundle
import com.asfoundation.wallet.logging.Logger
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationIntroPresenter(private val view: WalletVerificationIntroView,
                                       private val disposable: CompositeDisposable,
                                       private val navigator: WalletVerificationIntroNavigator,
                                       private val logger: Logger,
                                       private val viewScheduler: Scheduler,
                                       private val computationScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {

  }

  fun stop() = disposable.clear()
}

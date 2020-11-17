package com.asfoundation.wallet.wallet_verification.code

import android.os.Bundle
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletVerificationCodePresenter(private val view: WalletVerificationCodeView,
                                      private val disposable: CompositeDisposable,
                                      private val viewScheduler: Scheduler,
                                      private val ioScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {

  }

  fun stop() = disposable.clear()
}

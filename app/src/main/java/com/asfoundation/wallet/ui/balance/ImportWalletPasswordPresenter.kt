package com.asfoundation.wallet.ui.balance

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ImportWalletPasswordPresenter(private val view: ImportWalletPasswordView,
                                    private val disposable: CompositeDisposable,
                                    private val viewScheduler: Scheduler) {

  fun present() {

  }

  fun stop() {
    disposable.clear()
  }
}

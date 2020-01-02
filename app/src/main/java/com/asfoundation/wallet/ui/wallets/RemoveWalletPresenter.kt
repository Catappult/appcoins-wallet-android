package com.asfoundation.wallet.ui.wallets

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RemoveWalletPresenter(private val view: RemoveWalletView,
                            private val disposable: CompositeDisposable,
                            private val viewScheduler: Scheduler) {

  fun present() {
    handleBackUpClick()
    handleNoBackUpClick()
  }

  private fun handleBackUpClick() {
    disposable.add(view.backUpWalletClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToBackUp() }
        .subscribe())
  }

  private fun handleNoBackUpClick() {
    disposable.add(view.noBackUpWalletClick()
        .observeOn(viewScheduler)
        .doOnNext { view.proceedWithRemoveWallet() }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }
}

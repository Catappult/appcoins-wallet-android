package com.asfoundation.wallet.wallet_blocked

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletBlockedPresenter(
    private val view: WalletBlockedView,
    private val disposables: CompositeDisposable,
    private val viewScheduler: Scheduler
) {

  fun present() {
    handleDismissCLicks()
    handleEmailClicks()
  }

  fun stop() {
    disposables.clear()
  }

  private fun handleDismissCLicks() {
    disposables.add(
        view.getDismissCLicks()
            .observeOn(viewScheduler)
            .doOnNext { view.dismiss() }
            .subscribe()
    )
  }

  private fun handleEmailClicks() {
    disposables.add(
        view.getEmailClicks()
            .observeOn(viewScheduler)
            .doOnNext { view.openEmail() }
            .subscribe()
    )
  }

}
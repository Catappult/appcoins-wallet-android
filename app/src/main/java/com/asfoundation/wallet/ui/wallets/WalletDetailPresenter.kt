package com.asfoundation.wallet.ui.wallets

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletDetailPresenter(
    private val view: WalletDetailView,
    private val interactor: WalletDetailInteractor,
    private val walletAddress: String,
    private val disposable: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler) {

  fun present() {
    populateUi()
  }

  private fun populateUi() {
    disposable.add(interactor.getBalanceModel(walletAddress)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext { view.populateUi(it) }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }
}

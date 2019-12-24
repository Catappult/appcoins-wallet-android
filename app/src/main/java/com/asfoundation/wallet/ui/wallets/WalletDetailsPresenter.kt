package com.asfoundation.wallet.ui.wallets

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletDetailsPresenter(
    private val view: WalletDetailsView,
    private val interactor: WalletDetailsInteractor,
    private val walletAddress: String,
    private val disposable: CompositeDisposable,
    private val viewScheduler: Scheduler,
    private val networkScheduler: Scheduler) {

  fun present() {
    populateUi()
    handleCopyClick()
    handleShareClick()
    handleMakeWalletActiveClick()
    handleBackupClick()
    handleRemoveWalletClick()
  }

  private fun handleRemoveWalletClick() {
    disposable.add(view.removeWalletClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToRemoveWalletView(walletAddress) }
        .subscribe())
  }

  private fun handleBackupClick() {
    disposable.add(view.backupWalletClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToBackupView(walletAddress) }
        .subscribe())
  }

  private fun handleMakeWalletActiveClick() {
    disposable.add(view.makeWalletActiveClick()
        .flatMapCompletable {
          interactor.setActiveWallet(walletAddress)
              .observeOn(viewScheduler)
              .andThen { view.navigateToBalanceView() }
        }
        .subscribe())
  }

  private fun handleCopyClick() {
    disposable.add(view.copyClick()
        .observeOn(viewScheduler)
        .doOnNext { view.setAddressToClipBoard(walletAddress) }
        .subscribe())
  }

  private fun handleShareClick() {
    disposable.add(view.shareClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showShare(walletAddress) }
        .subscribe())
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

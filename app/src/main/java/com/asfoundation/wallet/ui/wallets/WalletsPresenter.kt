package com.asfoundation.wallet.ui.wallets

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletsPresenter(private val view: WalletsFragment,
                       private val walletsInteract: WalletsInteract,
                       private val disposable: CompositeDisposable,
                       private val viewScheduler: Scheduler,
                       private val networkScheduler: Scheduler) {
  fun present() {
    retrieveViewInformation()
    handleActiveWalletCardClick()
    handleOtherWalletCardClick()
    handleCreateNewWalletClick()
  }

  private fun handleCreateNewWalletClick() {
    disposable.add(view.createNewWalletClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.showCreatingAnimation() }
        .flatMapCompletable {
          walletsInteract.createWallet()
              .observeOn(viewScheduler)
              .andThen { view.showWalletCreatedAnimation() }
        }
        .subscribe())
  }

  private fun handleOtherWalletCardClick() {
    disposable.add(view.otherWalletCardClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, false) }
        .subscribe())
  }

  private fun handleActiveWalletCardClick() {
    disposable.add(view.activeWalletCardClicked()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToWalletDetailView(it, true) }
        .subscribe())
  }

  private fun retrieveViewInformation() {
    disposable.add(walletsInteract.retrieveWalletsModel()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setupUi(it.totalWallets, it.totalBalance, it.walletsBalance) }
        .subscribe())
  }

  fun stop() {
    disposable.clear()
  }

}

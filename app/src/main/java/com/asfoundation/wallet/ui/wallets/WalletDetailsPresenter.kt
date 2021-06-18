package com.asfoundation.wallet.ui.wallets

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class WalletDetailsPresenter(
    private val view: WalletDetailsView,
    private val interactor: WalletDetailsInteractor,
    private val walletsEventSender: WalletsEventSender,
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
    handleBackPress()
  }

  private fun handleRemoveWalletClick() {
    disposable.add(view.removeWalletClick()
        .observeOn(viewScheduler)
        .doOnNext { view.navigateToRemoveWalletView(walletAddress) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackupClick() {
    disposable.add(
        Observable.merge(view.backupInactiveWalletClick(), view.backupActiveWalletClick())
            .doOnNext {
              walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
                  WalletsAnalytics.CONTEXT_WALLET_DETAILS, WalletsAnalytics.STATUS_SUCCESS)
            }
            .doOnError {
              walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
                  WalletsAnalytics.CONTEXT_WALLET_DETAILS, WalletsAnalytics.STATUS_FAIL)
            }
            .observeOn(viewScheduler)
            .doOnNext { view.navigateToBackupView(walletAddress) }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleMakeWalletActiveClick() {
    disposable.add(view.makeWalletActiveClick()
        .flatMapCompletable {
          interactor.setActiveWallet(walletAddress)
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .doOnComplete { view.navigateToBalanceView() }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleCopyClick() {
    disposable.add(view.copyClick()
        .observeOn(viewScheduler)
        .doOnNext { view.setAddressToClipBoard(walletAddress) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShareClick() {
    disposable.add(view.shareClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showShare(walletAddress) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun populateUi() {
    disposable.add(interactor.getBalanceModel(walletAddress)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.populateUi(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackPress() {
    disposable.add(view.backPressed()
        .observeOn(viewScheduler)
        .doOnNext { view.handleBackPress() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}

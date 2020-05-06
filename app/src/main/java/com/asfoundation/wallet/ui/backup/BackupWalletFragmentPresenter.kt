package com.asfoundation.wallet.ui.backup

import com.asfoundation.wallet.ui.balance.BalanceInteract
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BackupWalletFragmentPresenter(private var balanceInteract: BalanceInteract,
                                    private var view: BackupWalletFragmentView,
                                    private var activityView: BackupActivityView,
                                    private var disposables: CompositeDisposable,
                                    private var dbScheduler: Scheduler,
                                    private var viewScheduler: Scheduler) {

  fun present(walletAddress: String) {
    handleBackupClick()
    retrieveStoredBalance(walletAddress)
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .doOnNext { activityView.showBackupCreationScreen() }
        .subscribe())
  }

  private fun retrieveStoredBalance(walletAddress: String) {
    disposables.add(balanceInteract.getStoredOverallBalance(walletAddress)
        .subscribeOn(dbScheduler)
        .observeOn(viewScheduler)
        .map { view.showBalance(it) }
        .subscribe())
  }

  fun stop() = disposables.clear()
}

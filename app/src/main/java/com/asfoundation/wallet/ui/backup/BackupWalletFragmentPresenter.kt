package com.asfoundation.wallet.ui.backup

import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.RoundingMode

class BackupWalletFragmentPresenter(private var balanceInteract: BalanceInteract,
                                    private var view: BackupWalletFragmentView,
                                    private var activityView: BackupActivityView,
                                    private var dbScheduler: Scheduler,
                                    private var viewScheduler: Scheduler) {
  private val disposables = CompositeDisposable()

  fun present(walletAddress: String) {
    handleBackupClick()

    disposables.add(balanceInteract.getStoredOverallBalance(walletAddress)
        .subscribeOn(dbScheduler)
        .observeOn(viewScheduler)
        .map {
          view.showBalance(
              FiatValue(it.amount.setScale(2, RoundingMode.DOWN), it.currency, it.symbol))
        }
        .subscribe())
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .doOnNext { activityView.showBackupCreationScreen() }
        .subscribe())
  }
}

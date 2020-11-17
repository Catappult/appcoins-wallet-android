package com.asfoundation.wallet.ui.backup.entry

import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class BackupWalletPresenter(private val balanceInteractor: BalanceInteractor,
                            private val view: BackupWalletFragmentView,
                            private val data: BackupWalletData,
                            private val navigator: BackupWalletNavigator,
                            private val currencyFormatUtils: CurrencyFormatUtils,
                            private val disposables: CompositeDisposable,
                            private val dbScheduler: Scheduler,
                            private val viewScheduler: Scheduler) {

  fun present() {
    handleBackupClick()
    retrieveStoredBalance()
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .doOnNext {
          view.hideKeyboard()
          navigator.showBackupCreationScreen(data.walletAddress, it)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun retrieveStoredBalance() {
    disposables.add(balanceInteractor.getStoredOverallBalance(data.walletAddress)
        .subscribeOn(dbScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.setupUi(data.walletAddress, it.symbol, currencyFormatUtils.formatCurrency(it.amount))
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onCheckedChanged(checked: Boolean) {
    if (checked) view.showPasswordFields()
    else view.hidePasswordFields()
  }

  fun stop() = disposables.clear()
}

package com.asfoundation.wallet.ui.backup.entry

import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

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
    handlePasswordTextChange()
    retrieveStoredBalance()
  }

  private fun handlePasswordTextChange() {
    disposables.add(view.onPasswordTextChanged()
        .observeOn(viewScheduler)
        .doOnNext {
          if (it.empty) view.clearErrors()
          if (!it.match) view.disableButton()
          if (it.match && !it.empty) {
            view.clearErrors()
            view.enableButton()
          }
        }
        .debounce(700, TimeUnit.MILLISECONDS, viewScheduler)
        .doOnNext { if (!it.match && !it.empty) view.showPasswordError() }
        .subscribe({}, { it.printStackTrace() }))
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

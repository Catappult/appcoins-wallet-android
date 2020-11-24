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
    initializeView()
    handleBackupClick()
    handlePasswordTextChange()
    handleOnCheckedChangeListener()
  }

  private fun handlePasswordTextChange() {
    disposables.add(view.onPasswordTextChanged()
        .observeOn(viewScheduler)
        .doOnNext { passwordFields: PasswordFields ->
          if (passwordFields.empty) view.clearErrors()
          if (!passwordFields.match) view.disableButton()
          if (passwordFields.match && !passwordFields.empty) {
            view.clearErrors()
            view.enableButton()
          }
        }
        .debounce(700, TimeUnit.MILLISECONDS, viewScheduler)
        .doOnNext { passwordFields -> if (!passwordFields.match && !passwordFields.empty) view.showPasswordError() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .doOnNext { passwordStatus: PasswordStatus ->
          view.hideKeyboard()
          val password = if (passwordStatus.wantsPassword) passwordStatus.password else ""
          navigator.showBackupCreationScreen(data.walletAddress, password)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleOnCheckedChangeListener() {
    disposables.add(view.onPasswordCheckedChanged()
        .doOnNext {
          if (it) view.showPasswordFields()
          else view.hidePasswordFields()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun initializeView() {
    disposables.add(balanceInteractor.getStoredOverallBalance(data.walletAddress)
        .subscribeOn(dbScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.setupUi(data.walletAddress, it.symbol, currencyFormatUtils.formatCurrency(it.amount))
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}

package com.asfoundation.wallet.ui.backup.entry

import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.wallets.usecases.GetWalletInfoUseCase
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class BackupWalletPresenter(private val view: BackupWalletFragmentView,
                            private val data: BackupWalletData,
                            private val getWalletInfoUseCase: GetWalletInfoUseCase,
                            private val navigator: BackupWalletNavigator,
                            private val currencyFormatUtils: CurrencyFormatUtils,
                            private val disposables: CompositeDisposable,
                            private val dbScheduler: Scheduler,
                            private val viewScheduler: Scheduler) {

  fun present() {
    initializeView()
    handleBackupClick()
    handleSkipClick()
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

  private fun handleSkipClick() {
    disposables.add(view.getSkipClick()
        .doOnNext {
          navigator.navigateToSkipScreen()
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
    disposables.add(getWalletInfoUseCase(data.walletAddress, cached = true, updateFiat = false)
        .subscribeOn(dbScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { walletInfo ->
          val overallBalanceFiat = walletInfo.walletBalance.overallFiat
          view.setupUi(data.walletAddress, overallBalanceFiat.symbol,
              currencyFormatUtils.formatCurrency(overallBalanceFiat.amount))
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.clear()
}

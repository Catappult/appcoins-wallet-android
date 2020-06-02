package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.wallets.WalletsModel
import com.asfoundation.wallet.wallet_validation.WalletValidationStatus
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val settingsInteract: SettingsInteract) {

  fun present() {
    view.setupPreferences()
    handleVerifyWalletPreferenceSummary()
    handleRedeemPreferenceSetup()
  }

  fun stop() = disposables.dispose()

  private fun handleVerifyWalletPreferenceSummary() {
    disposables.add(settingsInteract.isWalletValid()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          when (it.second) {
            WalletValidationStatus.SUCCESS -> view.setVerifiedWalletPreference()
            WalletValidationStatus.GENERIC_ERROR -> view.setUnverifiedWalletPreference()
            else -> handleValidationCache(it.first)
          }
        }
        .subscribe())
  }

  private fun handleValidationCache(address: String) {
    val isVerified = settingsInteract.isWalletValidated(address)
    if (isVerified) {
      view.setVerifiedWalletPreference()
    } else {
      view.setWalletValidationNoNetwork()
    }
  }

  private fun handleRedeemPreferenceSetup() {
    disposables.add(settingsInteract.findWallet()
        .subscribe { address ->
          view.setRedeemCodePreference(address)
        })
  }

  fun onBackupPreferenceClick() {
    disposables.add(settingsInteract.retrieveWallets()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleWalletModel(it) }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletsModel) {
    when (walletModel.totalWallets) {
      0 -> {
        view.showError()
      }
      1 -> {
        view.navigateToBackUp(walletModel.walletsBalance[0].walletAddress)
      }
      else -> {
        view.showWalletsBottomSheet(walletModel)
      }
    }
  }

}


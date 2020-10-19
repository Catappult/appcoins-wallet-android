package com.asfoundation.wallet.ui

import android.content.Intent
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val activityView: SettingsActivityView,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val settingsInteract: SettingsInteract) {

  fun present() {
    handleFingerPrintPreference()
  }

  fun onResume() {
    setupPreferences()
    handleRedeemPreferenceSetup()
    handleAuthenticationResult()
  }

  private fun setupPreferences() {
    view.setPermissionPreference()
    view.setSourceCodePreference()
    view.setIssueReportPreference()
    view.setTwitterPreference()
    view.setTelegramPreference()
    view.setFacebookPreference()
    view.setEmailPreference()
    view.setPrivacyPolicyPreference()
    view.setTermsConditionsPreference()
    view.setCreditsPreference()
    view.setVersionPreference()
    view.setRestorePreference()
    view.setBackupPreference()
  }

  private fun handleFingerPrintPreference() {
    if (settingsInteract.hasFingerPrintConfigured()) view.setFingerprintPreference()
    else view.removeFingerprintPreference()
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.authenticationResult()
        .filter { it }
        .doOnNext { view.disableFingerPrint() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.dispose()

  private fun handleRedeemPreferenceSetup() {
    disposables.add(settingsInteract.findWallet()
        .doOnSuccess { view.setRedeemCodePreference(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onBackupPreferenceClick() {
    disposables.add(settingsInteract.retrieveWallets()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleWalletModel(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleWalletModel(walletModel: WalletsModel) {
    when (walletModel.totalWallets) {
      0 -> {
        settingsInteract.sendCreateErrorEvent()
        view.showError()
      }
      1 -> {
        settingsInteract.sendCreateSuccessEvent()
        activityView.navigateToBackup(walletModel.walletsBalance[0].walletAddress)
      }
      else -> activityView.showWalletsBottomSheet(walletModel)
    }
  }

  fun onBugReportClicked() = settingsInteract.displaySupportScreen()

  fun redirectToStore() {
    disposables.add(Single.create<Intent> { it.onSuccess(settingsInteract.retriveUpdateIntent()) }
        .doOnSuccess { view.navigateToIntent(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }
}


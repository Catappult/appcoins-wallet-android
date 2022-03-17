package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import com.asfoundation.wallet.change_currency.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.ObserveSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.ResetSendLogsStateUseCase
import com.asfoundation.wallet.logging.send_logs.use_cases.SendLogsUseCase
import com.asfoundation.wallet.promo_code.use_cases.ObserveCurrentPromoCodeUseCase
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val navigator: SettingsNavigator,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val settingsInteractor: SettingsInteractor,
                        private val settingsData: SettingsData,
                        private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
                        private val observeSendLogsStateUseCase: ObserveSendLogsStateUseCase,
                        private val resetSendLogsStateUseCase: ResetSendLogsStateUseCase,
                        private val sendLogsUseCase: SendLogsUseCase,
                        private val observeCurrentPromoCodeUseCase: ObserveCurrentPromoCodeUseCase) {


  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) settingsInteractor.setHasBeenInSettings()
    handleAuthenticationResult()
    onFingerPrintPreferenceChange()
    if (settingsData.turnOnFingerprint && savedInstanceState == null) navigator.showAuthentication()
  }

  fun onResume() {
    updateFingerPrintPreference(settingsInteractor.retrievePreviousFingerPrintAvailability())
    setupPreferences()
    handleRedeemPreferenceSetup()
  }

  private fun setupPreferences() {
    view.setPermissionPreference()
    view.setWithdrawPreference()
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
    view.setManageSubscriptionsPreference()
    setCurrencyPreference()
    setSendLogsPreference()
    setPromoCodeState()
  }

  fun setFingerPrintPreference() {
    when (settingsInteractor.retrieveFingerPrintAvailability()) {
      BiometricManager.BIOMETRIC_SUCCESS -> view.setFingerprintPreference(
          settingsInteractor.hasAuthenticationPermission())
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
        settingsInteractor.changeAuthorizationPermission(false)
        view.removeFingerprintPreference()
      }
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
        view.toggleFingerprint(false)
        settingsInteractor.changeAuthorizationPermission(false)
        view.setDisabledFingerPrintPreference()
      }
    }
  }

  private fun updateFingerPrintPreference(previousAvailability: Int) {
    val newAvailability = settingsInteractor.retrieveFingerPrintAvailability()
    if (previousAvailability != newAvailability) {
      when (settingsInteractor.retrieveFingerPrintAvailability()) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
          if (previousAvailability == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            view.setFingerprintPreference(settingsInteractor.hasAuthenticationPermission())
          } else {
            view.updateFingerPrintListener(true)
          }
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
          settingsInteractor.changeAuthorizationPermission(false)
          view.removeFingerprintPreference()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
          view.toggleFingerprint(false)
          settingsInteractor.changeAuthorizationPermission(false)
          if (previousAvailability == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
            view.setDisabledFingerPrintPreference()
          } else {
            view.updateFingerPrintListener(false)
          }
        }
      }
    }
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.authenticationResult()
        .filter { it }
        .doOnNext {
          val hasPermission = settingsInteractor.hasAuthenticationPermission()
          settingsInteractor.changeAuthorizationPermission(!hasPermission)
          view.toggleFingerprint(!hasPermission)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.dispose()

  private fun handleRedeemPreferenceSetup() {
    disposables.add(settingsInteractor.findWallet()
        .doOnSuccess { view.setRedeemCodePreference(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onBackupPreferenceClick() {
    disposables.add(settingsInteractor.retrieveWallets()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleWalletModel(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleWalletModel(walletModel: WalletsModel) {
    when (walletModel.totalWallets) {
      0 -> {
        settingsInteractor.sendCreateErrorEvent()
        view.showError()
      }
      1 -> {
        settingsInteractor.sendCreateSuccessEvent()
        navigator.navigateToBackup(walletModel.wallets[0].walletAddress)
      }
      else -> navigator.showWalletsBottomSheet(walletModel)
    }
  }

  fun onPromoCodePreferenceClick() {
    navigator.showPromoCodeFragment()
  }

  fun onRecoverWalletPreferenceClick() {
    navigator.navigateToRecoverWalletActivity()
  }

  fun onBugReportClicked() = settingsInteractor.displaySupportScreen()

  fun redirectToStore() {
    disposables.add(
        Single.create<Intent> { it.onSuccess(settingsInteractor.retrieveUpdateIntent()) }
            .doOnSuccess { view.navigateToIntent(it) }
            .subscribe({}, { handleError(it) }))
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  private fun onFingerPrintPreferenceChange() {
    disposables.add(view.switchPreferenceChange()
        .doOnNext { navigator.showAuthentication() }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  fun onWithdrawClicked() {
    navigator.navigateToWithdrawScreen()
  }

  fun setCurrencyPreference() {
    disposables.add(getChangeFiatCurrencyModelUseCase()
        .observeOn(viewScheduler)
        .doOnSuccess {
          for (fiatCurrency in it.list) {
            if (fiatCurrency.currency == it.selectedCurrency) {
              view.setCurrencyPreference(fiatCurrency)
              break
            }
          }
        }
        .subscribeOn(networkScheduler)
        .subscribe())
  }

  fun setSendLogsPreference() {
    disposables.add(observeSendLogsStateUseCase()
        .observeOn(viewScheduler)
        .doOnNext {
          view.setSendLogsPreference(it)
        }
        .subscribe())
  }

  fun onSendLogsClicked() {
    disposables.add(sendLogsUseCase()
        .observeOn(viewScheduler)
        .subscribe())
  }

  fun resetSendLogsState() {
    resetSendLogsStateUseCase()
  }

  fun setPromoCodeState() {
    disposables.add(observeCurrentPromoCodeUseCase()
        .observeOn(viewScheduler)
        .doOnNext {
          view.setPromoCodePreference(it)
        }
        .subscribeOn(networkScheduler)
        .subscribe({}, { it.printStackTrace() }))
  }
}


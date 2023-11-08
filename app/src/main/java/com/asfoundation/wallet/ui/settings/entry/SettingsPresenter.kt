package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import androidx.navigation.NavController
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.github.michaelbull.result.get
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.rx2.rxSingle

class SettingsPresenter(
  private val view: SettingsView,
  private val navigator: SettingsNavigator,
  private val networkScheduler: Scheduler,
  private val viewScheduler: Scheduler,
  private val disposables: CompositeDisposable,
  private val settingsInteractor: SettingsInteractor,
  private val settingsData: SettingsData,
  private val buildUpdateIntentUseCase: BuildUpdateIntentUseCase,
  private val getChangeFiatCurrencyModelUseCase: GetChangeFiatCurrencyModelUseCase,
  private val displayChatUseCase: DisplayChatUseCase,
) {

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) settingsInteractor.setHasBeenInSettings()
    onFingerPrintPreferenceChange()
    if (settingsData.turnOnFingerprint && savedInstanceState == null) navigator.showAuthentication(
      view.authenticationResult()
    )
  }

  fun onResume() {
    updateFingerPrintPreference(settingsInteractor.retrievePreviousFingerPrintAvailability())
    setupPreferences()
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
    view.setManageWalletPreference()
    view.setAccountPreference()
    view.setManageSubscriptionsPreference()
    view.setFaqsPreference()
    setCurrencyPreference()
  }

  fun setFingerPrintPreference() {
    when (settingsInteractor.retrieveFingerPrintAvailability()) {
      BiometricManager.BIOMETRIC_SUCCESS -> view.setFingerprintPreference(settingsInteractor.hasAuthenticationPermission())

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

  fun stop() = disposables.dispose()

  fun onManageWalletPreferenceClick(navController: NavController) {
    navigator.navigateToManageWallet(navController)
  }

  fun onChangeCurrencyPreferenceClick(navController: NavController) {
    navigator.navigateToChangeCurrency(navController)
  }

  fun onBugReportClicked() = settingsInteractor.displaySupportScreen()

  fun redirectToStore() {
    disposables.add(
      Single.create<Intent> { it.onSuccess(buildUpdateIntentUseCase()) }
        .doOnSuccess { view.navigateToIntent(it) }
        .subscribe({}, { handleError(it) })
    )
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  private fun onFingerPrintPreferenceChange() {
    disposables.add(view.switchPreferenceChange()
      .doOnNext { navigator.showAuthentication(view.authenticationResult()) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun hasAuthenticationPermission() = settingsInteractor.hasAuthenticationPermission()

  fun changeAuthorizationPermission() =
    settingsInteractor.changeAuthorizationPermission(!hasAuthenticationPermission())

  private fun setCurrencyPreference() {
    disposables.add(rxSingle { getChangeFiatCurrencyModelUseCase() }
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe({ result ->
        result.get()?.let {
          for (fiatCurrency in it.list) {
            if (fiatCurrency.currency == it.selectedCurrency) {
              view.setCurrencyPreference(fiatCurrency)
              break
            }
          }
        }
      },
        { throwable ->
          handleError(throwable)
        }
      )
    )
  }

  fun displayChat() = displayChatUseCase()
}


package com.asfoundation.wallet.ui.settings.entry

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.os.Bundle
import androidx.navigation.NavController
import com.appcoins.wallet.core.utils.android_common.Log
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetChangeFiatCurrencyModelUseCase
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.manage_cards.usecases.GetStoredCardsUseCase
import com.asfoundation.wallet.ui.webview_login.usecases.GenerateWebLoginUrlUseCase
import com.asfoundation.wallet.update_required.use_cases.BuildUpdateIntentUseCase
import com.github.michaelbull.result.get
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
  private val getStoredCardsUseCase: GetStoredCardsUseCase,
  private val generateWebLoginUrlUseCase: GenerateWebLoginUrlUseCase,
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
    view.setDiscordPreference()
    view.setFacebookPreference()
    view.setEmailPreference()
    view.setPrivacyPolicyPreference()
    view.setTermsConditionsPreference()
    view.setCreditsPreference()
    view.setVersionPreference()
    view.setManageWalletPreference()
    view.setLoginPreference()
    view.setManageSubscriptionsPreference()
    view.setFaqsPreference()
    setCurrencyPreference()
    getCards()
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

  private fun getCards() {
    getStoredCardsUseCase()
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .doOnSubscribe {
        view.setSkeletonCardPreference()
      }
      .doOnSuccess { cards ->
        if (cards.isNullOrEmpty()) {
          view.setAddNewCardPreference()
        } else {
          view.setManageCardsPreference()
        }
      }
      .doOnError {
        view.setAddNewCardPreference()
      }
      .subscribe()
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

  fun onManageCardsPreferenceClick(navController: NavController) {
    navigator.navigateToManageCards(navController)
  }

  fun onAddCardsPreferenceClick(navController: NavController) {
    navigator.navigateToAddCards(navController)
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
    disposables.add(
      view.switchPreferenceChange()
      .doOnNext { navigator.showAuthentication(view.authenticationResult()) }
      .subscribe({}, { it.printStackTrace() })
    )
  }

  fun getLoginUrl(): String {
    return generateWebLoginUrlUseCase()
      .doOnError { error -> Log.d("getLoginUrl", "Error: ${error.message}") }
      .blockingGet()
  }

  fun hasAuthenticationPermission() = settingsInteractor.hasAuthenticationPermission()

  fun changeAuthorizationPermission() =
    settingsInteractor.changeAuthorizationPermission(!hasAuthenticationPermission())


  var currencyDisposable: Disposable? = null
  private fun setCurrencyPreference() {
    currencyDisposable?.dispose()
    currencyDisposable = rxSingle { getChangeFiatCurrencyModelUseCase() }
      .subscribeOn(networkScheduler)
      .observeOn(viewScheduler)
      .subscribe(
        { result ->
          val selectedFiatCurrency = result.get()?.let { model ->
            model.list.find { fiatCurrency -> fiatCurrency.currency == model.selectedCurrency }
          }
          view.setCurrencyPreference(selectedFiatCurrency)
        },
        { throwable ->
          view.setCurrencyPreference(null)
          handleError(throwable)
        }
      )
  }

  fun displayChat() = displayChatUseCase()
}


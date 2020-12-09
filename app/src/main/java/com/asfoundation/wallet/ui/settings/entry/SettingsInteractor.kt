package com.asfoundation.wallet.ui.settings.entry

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.fingerprint.FingerprintPreferencesRepositoryContract
import com.asfoundation.wallet.interact.AutoUpdateInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.FingerprintInteractor
import com.asfoundation.wallet.ui.wallets.WalletsInteract

class SettingsInteractor(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                         private val supportInteractor: SupportInteractor,
                         private val walletsInteract: WalletsInteract,
                         private val autoUpdateInteract: AutoUpdateInteract,
                         private val fingerprintInteractor: FingerprintInteractor,
                         private val walletsEventSender: WalletsEventSender,
                         private val preferenceRepository: PreferencesRepositoryType,
                         private val fingerprintPreferences: FingerprintPreferencesRepositoryContract) {

  private var fingerPrintAvailability: Int = -1

  fun findWallet() = findDefaultWalletInteract.find()
      .map { it.address }

  fun retrieveWallets() = walletsInteract.retrieveWalletsModel()

  fun sendCreateSuccessEvent() {
    walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
        WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_SUCCESS)
  }

  fun sendCreateErrorEvent() {
    walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
        WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL)
  }

  fun displaySupportScreen() = supportInteractor.displayChatScreen()

  fun retrieveUpdateIntent() = autoUpdateInteract.buildUpdateIntent()

  fun retrieveFingerPrintAvailability(): Int {
    fingerPrintAvailability = fingerprintInteractor.getDeviceCompatibility()
    return fingerPrintAvailability
  }

  fun retrievePreviousFingerPrintAvailability() = fingerPrintAvailability

  fun changeAuthorizationPermission(value: Boolean) =
      fingerprintPreferences.setAuthenticationPermission(value)

  fun hasAuthenticationPermission() = fingerprintPreferences.hasAuthenticationPermission()

  fun setHasBeenInSettings() = preferenceRepository.setBeenInSettings()
}

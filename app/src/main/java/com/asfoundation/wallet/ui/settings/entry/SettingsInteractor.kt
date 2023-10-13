package com.asfoundation.wallet.ui.settings.entry

import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsAnalytics
import com.appcoins.wallet.core.analytics.analytics.legacy.WalletsEventSender
import com.wallet.appcoins.feature.support.data.SupportInteractor
import com.asfoundation.wallet.ui.FingerprintInteractor
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.feature.walletInfo.data.wallet.FindDefaultWalletInteract
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class SettingsInteractor @Inject constructor(
        private val findDefaultWalletInteract: FindDefaultWalletInteract,
        private val supportInteractor: SupportInteractor,
        private val walletsInteract: WalletsInteract,
        private val fingerprintInteractor: FingerprintInteractor,
        private val walletsEventSender: WalletsEventSender,
        private val preferenceRepository: CommonsPreferencesDataSource,
        private val fingerprintPreferences: FingerprintPreferencesDataSource
) {

  private var fingerPrintAvailability: Int = -1

  fun findWallet() = findDefaultWalletInteract.find()
    .map { it.address }

  fun retrieveWallets() = walletsInteract.getWalletsModel()

  fun sendCreateErrorEvent() {
    walletsEventSender.sendCreateBackupEvent(
      WalletsAnalytics.ACTION_CREATE,
      WalletsAnalytics.CONTEXT_WALLET_SETTINGS, WalletsAnalytics.STATUS_FAIL
    )
  }

  fun displaySupportScreen() = supportInteractor.displayChatScreen()

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

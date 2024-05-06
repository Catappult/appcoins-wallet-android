package com.asfoundation.wallet.ui.settings.entry

import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletsInteract
import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import com.appcoins.wallet.sharedpreferences.FingerprintPreferencesDataSource
import com.asfoundation.wallet.home.usecases.DisplayChatUseCase
import com.asfoundation.wallet.ui.FingerprintInteractor
import javax.inject.Inject

class SettingsInteractor @Inject constructor(
  private val displayChatUseCase: DisplayChatUseCase,
  private val walletsInteract: WalletsInteract,
  private val fingerprintInteractor: FingerprintInteractor,
  private val preferenceRepository: CommonsPreferencesDataSource,
  private val fingerprintPreferences: FingerprintPreferencesDataSource
) {

  private var fingerPrintAvailability: Int = -1

  fun retrieveWallets() = walletsInteract.getWalletsModel()

  fun displaySupportScreen() = displayChatUseCase()

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

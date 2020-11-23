package com.asfoundation.wallet.ui.settings

import io.reactivex.Observable

interface SettingsActivityView {

  fun navigateToBackup(address: String)

  fun hideBottomSheet()

  fun authenticationResult(): Observable<Boolean>
}

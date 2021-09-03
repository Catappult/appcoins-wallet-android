package com.asfoundation.wallet.ui.settings

import io.reactivex.Observable

interface SettingsActivityView {

  fun authenticationResult(): Observable<Boolean>
}

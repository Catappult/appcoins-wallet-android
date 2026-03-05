package com.asfoundation.wallet.ui.webview_payment.usecases

import com.appcoins.wallet.sharedpreferences.CommonsPreferencesDataSource
import javax.inject.Inject

class GetCloudCountryCodeUseCase @Inject constructor(
  private val commonsPreferencesDataSource: CommonsPreferencesDataSource
) {
  operator fun invoke(): String? {
    return commonsPreferencesDataSource.getCountryCode()
  }
}
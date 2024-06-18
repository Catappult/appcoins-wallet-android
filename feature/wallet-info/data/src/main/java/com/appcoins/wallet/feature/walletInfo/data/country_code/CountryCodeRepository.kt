package com.appcoins.wallet.feature.walletInfo.data.country_code

import com.appcoins.wallet.core.network.backend.api.CountryApi
import com.appcoins.wallet.core.network.backend.model.CountryResponse
import com.appcoins.wallet.sharedpreferences.RefundDisclaimerPreferencesDataSource
import io.reactivex.Single
import javax.inject.Inject

class CountryCodeRepository @Inject constructor(
  private val refundDisclaimerPreferencesDataSource: RefundDisclaimerPreferencesDataSource,
  private val countryApi: CountryApi
) {


  fun getCountryCode(): Single<CountryResponse> {
    return countryApi.getCountryCodeForRefund()
  }

  fun getCachedShowRefundDisclaimer(): Boolean {
    return refundDisclaimerPreferencesDataSource.getRefundDisclaimer()
  }

  fun setShowRefundDisclaimer(showRefundDisClaimer: Boolean) {
    refundDisclaimerPreferencesDataSource.setRefundDisclaimer(showRefundDisClaimer)

  }

}
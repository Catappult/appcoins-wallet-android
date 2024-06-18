package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import javax.inject.Inject

class SetCachedShowRefundDisclaimerUseCase @Inject constructor(
  private val countryCodeRepository: CountryCodeRepository,
) {

  operator fun invoke(showRefundDisclaimer: Boolean) {
    countryCodeRepository.setShowRefundDisclaimer(showRefundDisclaimer)
  }

}
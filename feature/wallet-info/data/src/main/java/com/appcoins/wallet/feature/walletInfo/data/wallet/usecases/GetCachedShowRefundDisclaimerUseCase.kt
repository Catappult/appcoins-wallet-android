package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import javax.inject.Inject

class GetCachedShowRefundDisclaimerUseCase @Inject constructor(
  private val countryCodeRepository: CountryCodeRepository,
) {

  operator fun invoke(): Boolean {
    return countryCodeRepository.getCachedShowRefundDisclaimer()
  }

}
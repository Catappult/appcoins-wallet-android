package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCountryCodeUseCase @Inject constructor(
  private val countryCodeRepository: CountryCodeRepository
) {

  operator fun invoke(): Single<String> {
    return countryCodeRepository.getCountryCode()
      .map { countryResponse ->
        countryResponse.countryCode ?: ""
      }
  }

}

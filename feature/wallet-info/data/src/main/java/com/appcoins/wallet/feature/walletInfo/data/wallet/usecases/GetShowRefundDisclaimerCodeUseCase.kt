package com.appcoins.wallet.feature.walletInfo.data.wallet.usecases

import com.appcoins.wallet.core.network.backend.model.CountryResponse
import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import io.reactivex.Single
import javax.inject.Inject

class GetShowRefundDisclaimerCodeUseCase @Inject constructor(
  private val countryCodeRepository: CountryCodeRepository

) {

  operator fun invoke(): Single<CountryResponse> {
    return countryCodeRepository.getCountryCode()
  }

}
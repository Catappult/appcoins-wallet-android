package com.asfoundation.wallet.iab.domain.use_case

import com.appcoins.wallet.feature.walletInfo.data.country_code.CountryCodeRepository
import com.appcoins.wallet.ui.common.callAsync
import com.asfoundation.wallet.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetCountryCodeUseCase @Inject constructor(
  private val countryCodeProvider: CountryCodeRepository,
  @IoDispatcher private val networkDispatcher: CoroutineDispatcher,
  ) {

  suspend operator fun invoke() =
    countryCodeProvider.getCountryCode().callAsync(networkDispatcher)
}
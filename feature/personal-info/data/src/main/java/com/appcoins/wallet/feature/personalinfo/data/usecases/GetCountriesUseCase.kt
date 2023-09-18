package com.appcoins.wallet.feature.personalinfo.data.usecases

import com.appcoins.wallet.feature.personalinfo.data.PersonalInformationRepository
import javax.inject.Inject

class GetCountriesUseCase
@Inject constructor(private val personalInformationRepository: PersonalInformationRepository) {
  operator fun invoke() = personalInformationRepository.getCountries()
}
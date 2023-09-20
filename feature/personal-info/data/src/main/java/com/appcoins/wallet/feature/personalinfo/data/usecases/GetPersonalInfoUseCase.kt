package com.appcoins.wallet.feature.personalinfo.data.usecases

import com.appcoins.wallet.feature.personalinfo.data.PersonalInformationRepository
import javax.inject.Inject

class GetPersonalInfoUseCase
@Inject constructor(private val personalInformationRepository: PersonalInformationRepository) {
  operator fun invoke(ewt: String) = personalInformationRepository.getPersonalInfo(ewt = ewt)
}
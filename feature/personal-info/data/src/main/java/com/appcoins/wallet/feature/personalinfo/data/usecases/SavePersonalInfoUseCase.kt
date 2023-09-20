package com.appcoins.wallet.feature.personalinfo.data.usecases

import com.appcoins.wallet.core.network.backend.model.PersonalInformationRequest
import com.appcoins.wallet.feature.personalinfo.data.PersonalInformationRepository
import javax.inject.Inject

class SavePersonalInfoUseCase
@Inject constructor(private val personalInformationRepository: PersonalInformationRepository) {
  operator fun invoke(ewt: String, personalInformationRequest: PersonalInformationRequest) =
    personalInformationRepository.savePersonalInfo(
      personalInformationRequest = personalInformationRequest,
      ewt = ewt
    )
}
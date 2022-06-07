package com.asfoundation.wallet.update_required.use_cases

import com.asfoundation.wallet.repository.AutoUpdateRepository
import com.asfoundation.wallet.viewmodel.AutoUpdateModel
import io.reactivex.Single
import javax.inject.Inject

class GetAutoUpdateModelUseCase @Inject constructor(private val autoUpdateRepository: AutoUpdateRepository) {

  operator fun invoke(invalidateCache: Boolean = true) : Single<AutoUpdateModel> {
    return autoUpdateRepository.loadAutoUpdateModel(invalidateCache)

  }
}
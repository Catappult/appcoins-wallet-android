package com.asfoundation.wallet.main.usecases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class HasSeenPromotionTooltipUseCase @Inject constructor(
    val preferencesRepositoryType: PreferencesRepositoryType) {

  operator fun invoke(): Single<Boolean> =
      Single.just(preferencesRepositoryType.hasSeenPromotionTooltip())
}
package com.asfoundation.wallet.main.usecases

import com.asfoundation.wallet.repository.PreferencesRepositoryType
import io.reactivex.Single

class HasSeenPromotionTooltipUseCase(val preferencesRepositoryType: PreferencesRepositoryType) {

  operator fun invoke(): Single<Boolean> =
      Single.just(preferencesRepositoryType.hasSeenPromotionTooltip())
}
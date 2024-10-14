package com.appcoins.wallet.feature.promocode.data.use_cases

import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCurrentPromoCodeUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .firstOrError()
  }
}
package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Single

class GetCurrentPromoCodeUseCase(private val promoCodeRepository: PromoCodeRepository) {
  operator fun invoke(): Single<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
        .firstOrError()
  }
}
package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Observable

class ObserveCurrentPromoCodeUseCase(private val promoCodeRepository: PromoCodeRepository) {
  operator fun invoke(): Observable<PromoCodeEntity> {
    return promoCodeRepository.getCurrentPromoCode()
  }
}
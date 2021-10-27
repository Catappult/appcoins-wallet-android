package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Completable

class DeletePromoCodeUseCase(private val promoCodeRepository: PromoCodeRepository) {
  operator fun invoke(): Completable {
    return promoCodeRepository.removePromoCode()
  }
}
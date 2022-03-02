package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Completable
import javax.inject.Inject

class DeletePromoCodeUseCase @Inject constructor(
    private val promoCodeRepository: PromoCodeRepository) {

  operator fun invoke(): Completable {
    return promoCodeRepository.removePromoCode()
  }
}
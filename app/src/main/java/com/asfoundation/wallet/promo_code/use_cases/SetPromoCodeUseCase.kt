package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Completable

class SetPromoCodeUseCase(private val promoCodeRepository: PromoCodeRepository) {

  operator fun invoke(promoCodeString: String): Completable {
    return Completable.fromAction {
      promoCodeRepository.setPromoCode(promoCodeString)
    }
  }
}
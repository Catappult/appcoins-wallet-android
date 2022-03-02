package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Completable
import javax.inject.Inject

class SetPromoCodeUseCase @Inject constructor(
    private val promoCodeRepository: PromoCodeRepository) {

  operator fun invoke(promoCodeString: String): Completable {
    return promoCodeRepository.setPromoCode(promoCodeString)
  }
}
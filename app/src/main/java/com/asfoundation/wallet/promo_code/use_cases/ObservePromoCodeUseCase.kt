package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ObservePromoCodeUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Observable<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
  }
}
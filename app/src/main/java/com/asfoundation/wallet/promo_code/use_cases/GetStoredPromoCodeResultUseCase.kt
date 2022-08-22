package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.PromoCodeMapper
import com.asfoundation.wallet.promo_code.PromoCodeResult
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Observable
import javax.inject.Inject

class GetStoredPromoCodeResultUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Observable<PromoCodeResult> {
    return promoCodeRepository.observeCurrentPromoCode()
      .map { PromoCodeMapper().map(it) }
  }
}
package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeMapper
import com.asfoundation.wallet.promo_code.bottom_sheet.PromoCodeResult
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Observable
import javax.inject.Inject

class ObservePromoCodeResultUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Observable<PromoCodeResult> {
    return promoCodeRepository.observeCurrentPromoCode()
      .flatMap { PromoCodeMapper().map(it) }
  }
}
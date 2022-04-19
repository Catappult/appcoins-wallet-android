package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Observable
import javax.inject.Inject

class ObserveCurrentPromoCodeUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Observable<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
      .flatMap { promoCode ->
        if (promoCode.expired == false) {
          promoCode.code?.let {
            return@flatMap promoCodeRepository.setPromoCode(it)
              .andThen(Observable.just(promoCode))
          }
        }
        return@flatMap Observable.just(promoCode)
      }
  }
}
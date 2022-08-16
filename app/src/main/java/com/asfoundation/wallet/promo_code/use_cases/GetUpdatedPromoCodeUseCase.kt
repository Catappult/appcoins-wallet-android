package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUpdatedPromoCodeUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository,
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  operator fun invoke(): Single<PromoCode> {
    return getCurrentPromoCodeUseCase()
      .flatMap { promoCode ->
        promoCode.code?.let {
          if (promoCode.expired == false) {
            return@flatMap promoCodeRepository.setPromoCode(it)
//              .andThen(Single.just(promoCode))
          }
        }
        return@flatMap Single.just(promoCode)
      }
      .onErrorReturn {  //TODO alterar para ter o erro pelo sucesso?
        return@onErrorReturn PromoCode(null, null, null, null)
      }
  }
}
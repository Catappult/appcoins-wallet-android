package com.asfoundation.wallet.promo_code.use_cases

import com.asfoundation.wallet.promo_code.repository.PromoCode
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import com.asfoundation.wallet.promo_code.repository.ValidityState
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
          if (promoCode.validity == ValidityState.ACTIVE) {
            return@flatMap promoCodeRepository.verifyAndSavePromoCode(it)
          }
        }
        return@flatMap Single.just(promoCode)
      }
      .onErrorReturn {
        return@onErrorReturn PromoCode(null, null, ValidityState.ERROR, null)
      }
  }
}
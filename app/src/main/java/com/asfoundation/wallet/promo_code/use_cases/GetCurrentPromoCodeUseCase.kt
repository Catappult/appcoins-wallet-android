package com.asfoundation.wallet.promo_code.use_cases

import android.util.Log
import com.asfoundation.wallet.promo_code.repository.PromoCodeEntity
import com.asfoundation.wallet.promo_code.repository.PromoCodeRepository
import io.reactivex.Single

class GetCurrentPromoCodeUseCase(private val promoCodeRepository: PromoCodeRepository) {
  operator fun invoke(): Single<PromoCodeEntity> {
    Log.d("APPC-2709",
        "GetCurrentPromoCodeUseCase: invoke: code ${promoCodeRepository.getCurrentPromoCode()}")
    return promoCodeRepository.getCurrentPromoCode()
  }
}
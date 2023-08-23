package com.appcoins.wallet.feature.promocode.data.use_cases

import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeRepository
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class GetCurrentPromoCodeObservableUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(): Observable<PromoCode> {
    return promoCodeRepository.observeCurrentPromoCode()
  }
}
package com.appcoins.wallet.feature.promocode.data.use_cases

import com.appcoins.wallet.feature.promocode.data.PromoCodeMapper
import com.appcoins.wallet.feature.promocode.data.PromoCodeResult
import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeRepository
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
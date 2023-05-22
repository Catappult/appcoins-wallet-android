package com.appcoins.wallet.feature.promocode.data.use_cases

import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeRepository
import io.reactivex.Completable
import javax.inject.Inject

class DeletePromoCodeUseCase @Inject constructor(
    private val promoCodeRepository: PromoCodeRepository) {

  operator fun invoke(): Completable {
    return promoCodeRepository.removePromoCode()
  }
}
package com.appcoins.wallet.feature.promocode.data.use_cases

import com.appcoins.wallet.feature.promocode.data.PromoCodeMapper
import com.appcoins.wallet.feature.promocode.data.PromoCodeResult
import com.appcoins.wallet.feature.promocode.data.repository.PromoCodeRepository
import io.reactivex.Single
import javax.inject.Inject

class VerifyAndSavePromoCodeUseCase @Inject constructor(
  private val promoCodeRepository: PromoCodeRepository
) {

  operator fun invoke(promoCodeString: String): Single<PromoCodeResult> {
    return promoCodeRepository.verifyAndSavePromoCode(promoCodeString)
      .map { PromoCodeMapper().map(it) }
  }
}
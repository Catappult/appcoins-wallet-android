package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetUserLevelUseCase @Inject constructor(private val gamification: Gamification,
                                              private val findDefaultWalletUseCase: FindDefaultWalletUseCase,
                                              private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  operator fun invoke(): Single<Int> {
    return getCurrentPromoCodeUseCase()
        .flatMap { promoCode ->
          findDefaultWalletUseCase()
              .flatMap { gamification.getUserLevel(it.address, promoCode.code) }
        }
  }
}
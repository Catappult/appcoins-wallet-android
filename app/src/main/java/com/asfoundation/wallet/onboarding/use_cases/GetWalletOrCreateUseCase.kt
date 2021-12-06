package com.asfoundation.wallet.onboarding.use_cases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Single
import java.util.*

class GetWalletOrCreateUseCase(private val walletService: WalletService,
                               private val supportInteractor: SupportInteractor,
                               private val gamificationRepository: Gamification,
                               private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  operator fun invoke(): Single<String> {
    return walletService.getWalletOrCreate()
        .flatMap {
          getCurrentPromoCodeUseCase()
              .flatMap { promoCode ->
                val address = it.toLowerCase(Locale.ROOT)
                gamificationRepository.getUserLevel(address, promoCode.code)
                    .doOnSuccess { level -> supportInteractor.registerUser(level, address) }
                    .map { address }
              }
        }
  }

}
package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import com.asfoundation.wallet.promo_code.use_cases.GetCurrentPromoCodeUseCase
import com.asfoundation.wallet.support.SupportInteractor
import io.reactivex.Completable
import javax.inject.Inject

class WalletDetailsInteractor @Inject constructor(
    private val setDefaultWalletInteractor: SetDefaultWalletInteractor,
    private val supportInteractor: SupportInteractor,
    private val gamificationRepository: Gamification,
    private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase) {

  fun setActiveWallet(address: String): Completable {
    return getCurrentPromoCodeUseCase()
        .flatMapCompletable { promoCode ->
          setDefaultWalletInteractor.set(address)
              .andThen(gamificationRepository.getUserLevel(address, promoCode.code)
                  .doOnSuccess { supportInteractor.registerUser(it, address) }
                  .ignoreElement())
        }
  }
}

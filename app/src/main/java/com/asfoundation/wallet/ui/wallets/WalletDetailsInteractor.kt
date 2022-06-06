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
  private val getCurrentPromoCodeUseCase: GetCurrentPromoCodeUseCase
) {

  fun setActiveWallet(address: String): Completable = getCurrentPromoCodeUseCase()
    .flatMap { setDefaultWalletInteractor.set(address).toSingleDefault(it.code) }
    .flatMap { gamificationRepository.getUserLevel(address, it) }
    .doOnSuccess { supportInteractor.registerUser(it, address) }
    .ignoreElement()
}

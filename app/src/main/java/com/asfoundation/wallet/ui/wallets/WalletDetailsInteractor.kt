package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import com.wallet.appcoins.feature.support.data.SupportInteractor
import io.reactivex.Completable
import javax.inject.Inject

class WalletDetailsInteractor @Inject constructor(
    private val setDefaultWalletInteractor: SetDefaultWalletInteractor,
    private val supportInteractor: com.wallet.appcoins.feature.support.data.SupportInteractor,
    private val gamificationRepository: Gamification,
    private val getCurrentPromoCodeUseCase: com.appcoins.wallet.feature.promocode.data.use_cases.GetCurrentPromoCodeUseCase
) {

  fun setActiveWallet(address: String): Completable = setDefaultWalletInteractor.set(address)

  fun setActiveWalletSupport(address: String): Completable = getCurrentPromoCodeUseCase()
    .flatMap { gamificationRepository.getUserLevel(address, it.code) }
    .doOnSuccess { supportInteractor.registerUser(it, address) }
    .doOnError(Throwable::printStackTrace)
    .ignoreElement()
    .onErrorComplete()
}

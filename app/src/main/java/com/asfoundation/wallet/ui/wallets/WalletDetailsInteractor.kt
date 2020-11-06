package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.interact.SetDefaultWalletInteract
import com.asfoundation.wallet.support.SupportRepository
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletDetailsInteractor(private val balanceInteract: BalanceInteract,
                              private val setDefaultWalletInteract: SetDefaultWalletInteract,
                              private val supportRepository: SupportRepository,
                              private val gamificationRepository: Gamification) {

  fun getBalanceModel(address: String): Single<BalanceScreenModel> =
      balanceInteract.getStoredBalanceScreenModel(address)

  fun setActiveWallet(address: String): Completable {
    return setDefaultWalletInteract.set(address)
        .andThen(gamificationRepository.getUserStats(address))
        .flatMap {
          gamificationRepository.getUserStats(address)
              .doOnSuccess { supportRepository.registerUser(it.level, address) }
        }
        .ignoreElement()
  }
}

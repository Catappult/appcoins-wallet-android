package com.asfoundation.wallet.ui.wallets

import com.appcoins.wallet.gamification.Gamification
import com.asfoundation.wallet.interact.SetDefaultWalletInteractor
import com.asfoundation.wallet.support.SupportInteractor
import com.asfoundation.wallet.ui.balance.BalanceInteractor
import com.asfoundation.wallet.ui.balance.BalanceScreenModel
import io.reactivex.Completable
import io.reactivex.Single

class WalletDetailsInteractor(private val balanceInteractor: BalanceInteractor,
                              private val setDefaultWalletInteractor: SetDefaultWalletInteractor,
                              private val supportInteractor: SupportInteractor,
                              private val gamificationRepository: Gamification) {

  fun getBalanceModel(address: String): Single<BalanceScreenModel> =
      balanceInteractor.getStoredBalanceScreenModel(address)

  fun setActiveWallet(address: String): Completable {
    return setDefaultWalletInteractor.set(address)
        .andThen(gamificationRepository.getUserLevel(address)
            .doOnSuccess { supportInteractor.registerUser(it, address) }
            .ignoreElement())
  }
}

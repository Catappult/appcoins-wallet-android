package com.asfoundation.wallet.wallet_blocked

import com.asfoundation.wallet.wallets.FindDefaultWalletInteract
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class WalletBlockedInteract(private val findDefaultWalletInteract: FindDefaultWalletInteract,
                            private val walletStatusRepository: WalletStatusRepository
) {

  fun isWalletBlocked(): Single<Boolean> {
    return findDefaultWalletInteract.find()
        .flatMap { walletStatusRepository.isWalletBlocked(it.address) }
        .onErrorReturn { false }
        .delay(1, TimeUnit.SECONDS)
  }

}
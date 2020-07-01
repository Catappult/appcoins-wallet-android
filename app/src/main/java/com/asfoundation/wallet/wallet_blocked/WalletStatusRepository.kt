package com.asfoundation.wallet.wallet_blocked

import io.reactivex.Single

class WalletStatusRepository(
    private val api: WalletStatusApi
) {

  fun isWalletBlocked(walletAddress: String): Single<Boolean> {
    return api.isWalletBlocked(walletAddress)
        .map { it.blocked }
  }

}
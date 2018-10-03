package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.AppcoinsRewardsRepository
import io.reactivex.Single

class AppcoinsRewards(private val repository: AppcoinsRewardsRepository,
                      private val walletAddressProvider: WalletAddressProvider) {

  fun getBalance(address: String): Single<Long> {
    return repository.getBalance(address)
  }

  fun getBalance(): Single<Long> {
    return walletAddressProvider.getWallet().flatMap { getBalance(it) }
  }
}
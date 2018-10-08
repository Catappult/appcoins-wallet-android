package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class AppcoinsRewards(private val repository: AppcoinsRewardsRepository,
                      private val walletService: WalletService) {

  fun getBalance(address: String): Single<Long> {
    return repository.getBalance(address)
  }

  fun getBalance(): Single<Long> {
    return walletService.getWalletAddress().flatMap { getBalance(it) }
  }

  fun pay(amount: BigDecimal,
          origin: Origin, sku: String,
          type: Type,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String): Completable {
    return walletService.getWalletAddress().flatMapCompletable { walletAddress ->
      walletService.signContent(walletAddress).flatMapCompletable { signature ->
        repository.pay(walletAddress, signature, amount,
            origin, sku,
            type, developerAddress, storeAddress, oemAddress)
      }
    }
  }
}
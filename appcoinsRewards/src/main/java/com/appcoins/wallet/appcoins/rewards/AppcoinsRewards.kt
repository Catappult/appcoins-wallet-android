package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.appcoins.rewards.repository.WalletService
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigInteger

class AppcoinsRewards(private val repository: AppcoinsRewardsRepository,
                      private val walletAddressProvider: WalletAddressProvider,
                      private val walletService: WalletService,
                      private val appCoinsUnityConverter: AppcoinsUnityConverter) {

  fun getBalance(address: String): Single<Long> {
    return repository.getBalance(address)
  }

  fun getBalance(): Single<Long> {
    return walletAddressProvider.getWallet().flatMap { getBalance(it) }
  }

  fun pay(amount: BigInteger,
          origin: Origin, sku: String,
          type: Type,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String): Completable {
    return walletService.getWalletAddress().flatMapCompletable { walletAddress ->
      walletService.signContent(walletAddress).flatMapCompletable { signature ->
        repository.pay(walletAddress, signature, appCoinsUnityConverter.convertToAppCoins(amount),
            origin, sku,
            type, developerAddress, storeAddress, oemAddress)
      }
    }
  }
}
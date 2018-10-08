package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class BdsAppcoinsRewardsRepository(private val remoteRepository: RemoteRepository) :
    AppcoinsRewardsRepository {
  override fun getBalance(address: String): Single<Long> {
    return remoteRepository.getBalance(address).map { it.balance }
  }

  override fun pay(walletAddress: String, signature: String, amount: BigDecimal,
                   origin: Origin,
                   sku: String,
                   type: Type,
                   developerAddress: String,
                   storeAddress: String,
                   oemAddress: String): Completable {
    return remoteRepository.pay(walletAddress, signature, amount, origin, sku,
        type, developerAddress, storeAddress, oemAddress)
  }
}
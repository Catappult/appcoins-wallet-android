package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigInteger

interface RemoteRepository {
  fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse>
  fun pay(walletAddress: String, signature: String, amount: BigInteger,
          origin: Origin,
          sku: String,
          type: Type,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String): Completable
}

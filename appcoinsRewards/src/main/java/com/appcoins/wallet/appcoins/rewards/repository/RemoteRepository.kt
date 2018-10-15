package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.Origin
import com.appcoins.wallet.appcoins.rewards.repository.bds.Type
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Single
import java.math.BigDecimal

interface RemoteRepository {
  fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse>
  fun pay(walletAddress: String, signature: String, amount: BigDecimal,
          origin: Origin,
          sku: String,
          type: Type,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String,
          packageName: String): Single<Transaction>
}

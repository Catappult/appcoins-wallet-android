package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Single
import java.math.BigDecimal

interface AppcoinsRewardsRepository {
  fun getBalance(address: String): Single<Long>
  fun pay(walletAddress: String, signature: String, amount: BigDecimal,
          origin: String?, sku: String,
          type: String,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String,
          packageName: String): Single<Transaction>
}

package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

interface RemoteRepository {
  fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse>
  fun pay(walletAddress: String, signature: String, amount: BigDecimal,
          origin: String?,
          sku: String?,
          type: String,
          developerAddress: String,
          storeAddress: String,
          oemAddress: String,
          packageName: String,
          payload: String?,
          callback: String?,
          orderReference: String?): Single<Transaction>

  fun sendCredits(toWallet: String, walletAddress: String, signature: String, amount: BigDecimal,
                  origin: String,
                  type: String, packageName: String): Completable
}

package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.repository.backend.BackendApi
import com.appcoins.wallet.appcoins.rewards.repository.bds.BdsApi
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Single
import java.math.BigDecimal

class BdsRemoteApi(private val backendApi: BackendApi, private val bdsApi: BdsApi) :
    RemoteRepository {
  override fun getBalance(address: String): Single<BackendApi.RewardBalanceResponse> {
    return backendApi.getBalance(address)
  }

  override fun pay(walletAddress: String, signature: String,
                   amount: BigDecimal,
                   origin: String?,
                   sku: String?,
                   type: String,
                   developerAddress: String,
                   storeAddress: String,
                   oemAddress: String,
                   packageName: String,
                   payload: String?,
                   callback: String?): Single<Transaction> {
    return bdsApi.pay(walletAddress, signature,
        BdsApi.PayBody(amount.toPlainString(), origin, sku, type, payload, callback, developerAddress, storeAddress,
            oemAddress, "APPC", packageName))
  }
}
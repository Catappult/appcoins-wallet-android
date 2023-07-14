package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

interface RemoteRepository {
  fun pay(walletAddress: String, signature: String, amount: BigDecimal, origin: String?,
          sku: String?, type: String, developerAddress: String, entityOemId: String?,
          entityDomain: String?, packageName: String, payload: String?, callback: String?,
          orderReference: String?, referrerUrl: String?, productToken: String?): Single<Transaction>

  fun sendCredits(toWallet: String, walletAddress: String, signature: String, amount: BigDecimal,
                  origin: String,
                  type: String, packageName: String): Single<Transaction>
}

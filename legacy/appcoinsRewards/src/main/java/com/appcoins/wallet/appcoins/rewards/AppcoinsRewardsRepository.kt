package com.appcoins.wallet.appcoins.rewards

import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Single
import java.math.BigDecimal

interface AppcoinsRewardsRepository {
  fun pay(walletAddress: String, signature: String, amount: BigDecimal, origin: String?,
          sku: String?, type: String, developerAddress: String, entityOemId: String?,
          entityDomain: String?, packageName: String, payload: String?, callback: String?,
          orderReference: String?, referrerUrl: String?, productToken: String?): Single<Transaction>

  fun sendCredits(toAddress: String, walletAddress: String, signature: String, amount: BigDecimal,
                  origin: String,
                  type: String, packageName: String): Single<Status>

  enum class Status {
    API_ERROR, UNKNOWN_ERROR, SUCCESS, INVALID_AMOUNT, INVALID_WALLET_ADDRESS, NOT_ENOUGH_FUNDS,
    NO_INTERNET
  }
}

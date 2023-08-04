package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Single
import retrofit2.HttpException
import java.math.BigDecimal

class BdsAppcoinsRewardsRepository(private val remoteRepository: RemoteRepository) :
    AppcoinsRewardsRepository {
  override fun sendCredits(toAddress: String, walletAddress: String, signature: String,
                           amount: BigDecimal,
                           origin: String, type: String,
                           packageName: String): Single<Pair<AppcoinsRewardsRepository.Status, Transaction>> {
    return remoteRepository.sendCredits(toAddress, walletAddress, signature, amount, origin, type,
        packageName)
      .map { Pair(AppcoinsRewardsRepository.Status.SUCCESS, it) }
        .onErrorReturn { Pair(map(it), Transaction.notFound()) }
  }

  override fun pay(walletAddress: String, signature: String, amount: BigDecimal, origin: String?,
                   sku: String?, type: String, developerAddress: String, entityOemId: String?,
                   entityDomain: String?, packageName: String, payload: String?, callback: String?,
                   orderReference: String?, referrerUrl: String?, productToken: String?)
      : Single<Transaction> {
    return remoteRepository.pay(walletAddress, signature, amount, origin, sku,
        type, developerAddress, entityOemId, entityDomain, packageName, payload, callback,
        orderReference, referrerUrl, productToken)
  }

  private fun map(throwable: Throwable): AppcoinsRewardsRepository.Status {
    return when (throwable) {
      is HttpException -> AppcoinsRewardsRepository.Status.API_ERROR
      else -> AppcoinsRewardsRepository.Status.UNKNOWN_ERROR
    }
  }
}
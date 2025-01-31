package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.core.network.microservices.model.Transaction
import io.reactivex.Single
import retrofit2.HttpException
import java.math.BigDecimal

class BdsAppcoinsRewardsRepository(
  private val remoteRepository: RemoteRepository
) : AppcoinsRewardsRepository {

  override fun sendCredits(
    toAddress: String,
    walletAddress: String,
    signature: String,
    amount: BigDecimal,
    currency: String,
    origin: String,
    type: String,
    packageName: String,
    guestWalletId: String?
  ): Single<Pair<AppcoinsRewardsRepository.Status, Transaction>> {
    return remoteRepository.sendCredits(
      toWallet = toAddress,
      walletAddress = walletAddress,
      amount = amount,
      currency = currency,
      origin = origin,
      type = type,
      packageName = packageName,
      guestWalletId = guestWalletId
    )
      .map { Pair(AppcoinsRewardsRepository.Status.SUCCESS, it) }
      .onErrorReturn { Pair(map(it), Transaction.notFound()) }
  }

  override fun pay(
    walletAddress: String,
    signature: String,
    amount: BigDecimal,
    origin: String?,
    sku: String?,
    type: String,
    entityOemId: String?,
    entityDomain: String?,
    packageName: String,
    payload: String?,
    callback: String?,
    orderReference: String?,
    referrerUrl: String?,
    productToken: String?,
    guestWalletId: String?
  ): Single<Transaction> {
    return remoteRepository.pay(
      walletAddress = walletAddress,
      signature = signature,
      amount = amount,
      origin = origin,
      sku = sku,
      type = type,
      entityOemId = entityOemId,
      entityDomain = entityDomain,
      packageName = packageName,
      payload = payload,
      callback = callback,
      orderReference = orderReference,
      referrerUrl = referrerUrl,
      guestWalletId = guestWalletId
    )
  }

  private fun map(throwable: Throwable): AppcoinsRewardsRepository.Status {
    return when (throwable) {
      is HttpException -> AppcoinsRewardsRepository.Status.API_ERROR
      else -> AppcoinsRewardsRepository.Status.UNKNOWN_ERROR
    }
  }
}

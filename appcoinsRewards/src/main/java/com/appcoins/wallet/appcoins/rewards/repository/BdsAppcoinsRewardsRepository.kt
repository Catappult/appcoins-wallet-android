package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Single
import retrofit2.HttpException
import java.math.BigDecimal

class BdsAppcoinsRewardsRepository(private val remoteRepository: RemoteRepository) :
    AppcoinsRewardsRepository {
  override fun sendCredits(toAddress: String, walletAddress: String, signature: String,
                           amount: BigDecimal,
                           origin: String, type: String,
                           packageName: String): Single<AppcoinsRewardsRepository.Status> {
    return remoteRepository.sendCredits(toAddress, walletAddress, signature, amount, origin, type,
        packageName).toSingle { AppcoinsRewardsRepository.Status.SUCCESS }.onErrorReturn { map(it) }
  }

  override fun getBalance(address: String): Single<BigDecimal> {
    return remoteRepository.getBalance(address).map { it.balance }
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
                   callback: String?,
                   orderReference: String?): Single<Transaction> {
    return remoteRepository.pay(walletAddress, signature, amount, origin, sku,
        type, developerAddress, storeAddress, oemAddress, packageName, payload, callback,
        orderReference)
  }

  private fun map(throwable: Throwable): AppcoinsRewardsRepository.Status {
    return when (throwable) {
      is HttpException -> AppcoinsRewardsRepository.Status.API_ERROR
      else -> AppcoinsRewardsRepository.Status.UNKNOWN_ERROR
    }
  }
}
package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

class BdsAppcoinsRewardsRepository(private val remoteRepository: RemoteRepository) :
    AppcoinsRewardsRepository {
  override fun getBalance(address: String): Single<Long> {
    return remoteRepository.getBalance(address).map { it.balance }
  }
}
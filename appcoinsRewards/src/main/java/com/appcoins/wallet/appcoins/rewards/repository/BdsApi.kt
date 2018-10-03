package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

class BdsApi(private val api: Api) : RemoteRepository {
  override fun getBalance(address: String): Single<Api.RewardBalanceResponse> {
    return api.getBalance(address)
  }
}
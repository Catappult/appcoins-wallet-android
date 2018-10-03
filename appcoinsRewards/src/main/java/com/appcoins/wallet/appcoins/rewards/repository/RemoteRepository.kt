package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

interface RemoteRepository {
  fun getBalance(address: String): Single<Api.RewardBalanceResponse>
}

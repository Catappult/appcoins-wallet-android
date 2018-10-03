package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

interface AppcoinsRewardsRepository {
  fun getBalance(address: String): Single<Long>
}

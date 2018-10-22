package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

interface WalletService {
  fun getWalletAddress(): Single<String>

  fun signContent(content: String): Single<String>
}

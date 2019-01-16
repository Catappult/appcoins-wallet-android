package com.appcoins.wallet.appcoins.rewards.repository

import io.reactivex.Single

interface AddressService {
  fun getWalletAddress(packageName: String): Single<String>
}
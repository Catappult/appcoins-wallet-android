package com.appcoins.wallet.billing

import io.reactivex.Single

interface WalletService {
  fun getAddress(): Single<String>
  fun getSignature(): Single<String>
}
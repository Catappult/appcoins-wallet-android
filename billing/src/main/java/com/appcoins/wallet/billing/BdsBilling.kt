package com.appcoins.wallet.billing

import io.reactivex.Single

internal class BdsBilling(private val repository: Repository) :
    Billing {
  override fun isSupported(): Single<Boolean> {
    return repository.isSupported()
  }
}
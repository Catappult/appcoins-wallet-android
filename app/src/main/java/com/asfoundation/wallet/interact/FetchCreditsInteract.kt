package com.asfoundation.wallet.interact

import io.reactivex.Single
import java.math.BigDecimal

class FetchCreditsInteract(private val balanceGetter: BalanceGetter) {
  fun getBalance(address: String): Single<BigDecimal> {
    return balanceGetter.getBalance(address)
  }
}
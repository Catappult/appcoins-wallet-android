package com.asfoundation.wallet.interact

import com.asfoundation.wallet.entity.Wallet
import io.reactivex.Single
import java.math.BigDecimal

class FetchCreditsInteract (private val balanceGetter: BalanceGetter) {
  fun getBalance(wallet: Wallet): Single<BigDecimal> {
    return balanceGetter.getBalance(wallet.address)
  }
}
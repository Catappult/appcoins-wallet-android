package com.asfoundation.wallet.interact

import io.reactivex.Single
import java.math.BigDecimal


interface BalanceGetter {
  fun getBalance(address: String): Single<BigDecimal>

  fun getBalance(): Single<BigDecimal>
}
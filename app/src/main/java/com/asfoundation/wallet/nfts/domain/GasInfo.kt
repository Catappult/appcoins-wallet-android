package com.asfoundation.wallet.nfts.domain

import java.math.BigDecimal
import java.math.BigInteger

data class GasInfo(val gasPrice: BigInteger, val gasLimit: BigInteger, val rate: BigDecimal,
                   val symbol: String, val currency: String) {
  fun copyWith(gasPrice: BigInteger = this.gasPrice, gasLimit: BigInteger = this.gasLimit,
               rate: BigDecimal = this.rate, symbol: String = this.symbol,
               currency: String = this.currency) =
      GasInfo(gasPrice, gasLimit, rate, symbol, currency)
}



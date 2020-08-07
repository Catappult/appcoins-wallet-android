package com.asfoundation.wallet.transactions

import java.math.BigDecimal

data class MissingTransactionDetails(val title: String?, val appcoinsAmount: BigDecimal,
                                     val subscriptionPeriod: String?) { //TODO this should receive all other subscription values such as trial period

  constructor(appcoinsAmount: BigDecimal) : this(null, appcoinsAmount, null)

}

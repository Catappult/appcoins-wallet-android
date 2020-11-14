package com.appcoins.wallet.billing.carrierbilling.response

import java.math.BigDecimal

data class TransactionCarrierError(val name: String?,
                                   val type: String?,
                                   val value: BigDecimal?)
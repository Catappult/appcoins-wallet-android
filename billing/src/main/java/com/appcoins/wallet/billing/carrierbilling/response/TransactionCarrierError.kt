package com.appcoins.wallet.billing.carrierbilling.response

import java.math.BigDecimal

data class TransactionCarrierError(val enduser: String, val technical: String, val code: Int,
                                   val value: BigDecimal)
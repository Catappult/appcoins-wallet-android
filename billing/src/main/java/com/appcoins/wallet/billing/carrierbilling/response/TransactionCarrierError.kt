package com.appcoins.wallet.billing.carrierbilling.response

import java.math.BigDecimal

data class TransactionCarrierError(val errorCode: Int, val value: BigDecimal)
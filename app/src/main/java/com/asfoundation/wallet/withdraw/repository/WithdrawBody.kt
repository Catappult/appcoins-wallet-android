package com.asfoundation.wallet.withdraw.repository

import java.math.BigDecimal

data class WithdrawBody(val email: String, val amount: BigDecimal)

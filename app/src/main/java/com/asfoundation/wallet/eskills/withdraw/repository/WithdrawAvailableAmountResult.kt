package com.asfoundation.wallet.eskills.withdraw.repository

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WithdrawAvailableAmountResult(
    @SerializedName("available_amount")
    val amount: BigDecimal
)

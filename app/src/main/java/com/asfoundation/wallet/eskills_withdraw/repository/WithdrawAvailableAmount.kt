package com.asfoundation.wallet.eskills_withdraw.repository

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WithdrawAvailableAmount(
    @SerializedName("available_amount")
    val amount: BigDecimal
)

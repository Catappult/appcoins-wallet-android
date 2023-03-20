package com.appcoins.wallet.core.network.backend.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class WithdrawAvailableAmountResult(
    @SerializedName("available_amount")
    val amount: BigDecimal
)

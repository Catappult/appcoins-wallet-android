package com.appcoins.wallet.billing.adyen

import com.google.gson.annotations.SerializedName

data class FraudResult(@SerializedName("FraudCheckResult") val fraudCheckResult: FraudCheckResult)
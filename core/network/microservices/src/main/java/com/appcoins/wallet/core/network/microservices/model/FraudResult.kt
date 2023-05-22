package com.appcoins.wallet.core.network.microservices.model

import com.google.gson.annotations.SerializedName

data class FraudResult(@SerializedName("FraudCheckResult") val fraudCheckResult: FraudCheckResult)
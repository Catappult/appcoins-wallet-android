package com.appcoins.wallet.billing.adyen

data class FraudResultResponse(val accountScore: String, val results: List<FraudResult>)
package com.appcoins.wallet.core.network.microservices.model

data class FraudResultResponse(val accountScore: String, val results: List<FraudResult>)
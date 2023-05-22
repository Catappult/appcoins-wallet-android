package com.appcoins.wallet.core.network.microservices.model

data class FraudCheckResult(val accountScore: Int, val checkId: Int, val name: String)

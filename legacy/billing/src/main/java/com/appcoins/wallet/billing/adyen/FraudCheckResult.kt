package com.appcoins.wallet.billing.adyen

data class FraudCheckResult(val accountScore: Int, val checkId: Int, val name: String)

package com.asfoundation.wallet.util

data class OneStepUri(
    var scheme: String,
    var host: String,
    var path: String,
    var parameters: Map<String, String>
)

package com.asfoundation.wallet.util

data class OneStepUri(
    var scheme: String? = null,
    var host: String? = null,
    var path: String? = null,
    var parameters: MutableMap<String, String> = mutableMapOf()
)
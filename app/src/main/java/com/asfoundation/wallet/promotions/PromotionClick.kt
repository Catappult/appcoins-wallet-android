package com.asfoundation.wallet.promotions

data class PromotionClick(
    val id: String,
    val extras: Map<String, String>? = null
)
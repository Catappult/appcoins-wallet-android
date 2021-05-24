package com.asfoundation.wallet.promotions.model

data class PromotionClick(
    val id: String,
    val extras: Map<String, String>? = null
)
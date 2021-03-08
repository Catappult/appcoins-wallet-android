package com.asfoundation.wallet.promotions

open class PromotionClick(val id: String)

class VoucherClick(id: String, val packageName: String, val title: String,
                   val featureGraphic: String, val icon: String, val maxBonus: Double,
                   val hasAppcoins: Boolean) : PromotionClick(id)

class AppPromotionClick(id: String, val downloadLink: String) : PromotionClick(id)

class ReferralClick(id: String, val link: String, val action: String) : PromotionClick(id)

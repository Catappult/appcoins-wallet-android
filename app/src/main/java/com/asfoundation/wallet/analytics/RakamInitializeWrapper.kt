package com.asfoundation.wallet.analytics

import com.appcoins.wallet.feature.promocode.data.repository.PromoCode

data class RakamInitializeWrapper(
    val installerPackage: String, val level: Int,
    val hasGms: Boolean, val walletAddress: String,
    val promoCode: com.appcoins.wallet.feature.promocode.data.repository.PromoCode
)

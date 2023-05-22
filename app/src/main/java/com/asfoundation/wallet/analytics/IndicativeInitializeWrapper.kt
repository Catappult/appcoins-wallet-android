package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.identification.DeviceInformation
import com.asfoundation.wallet.promo_code.repository.PromoCode


data class IndicativeInitializeWrapper(
    val installerPackage: String, val level: Int,
    val hasGms: Boolean, val walletAddress: String,
    val promoCode: PromoCode, val deviceInfo: DeviceInformation
)

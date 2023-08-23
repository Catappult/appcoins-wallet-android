package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.identification.DeviceInformation
import com.appcoins.wallet.feature.promocode.data.repository.PromoCode


data class IndicativeInitializeWrapper(
    val installerPackage: String, val level: Int,
    val hasGms: Boolean, val walletAddress: String,
    val promoCode: com.appcoins.wallet.feature.promocode.data.repository.PromoCode, val deviceInfo: DeviceInformation
)

package com.asfoundation.wallet.analytics

import com.appcoins.wallet.feature.promocode.data.repository.PromoCode
import com.asfoundation.wallet.identification.DeviceInformation

data class IndicativeInitializeWrapper(
  val installerPackage: String, val level: Int,
  val hasGms: Boolean, val walletAddress: String,
  val promoCode: PromoCode, val deviceInfo: DeviceInformation,
  val ghOemId: String
)

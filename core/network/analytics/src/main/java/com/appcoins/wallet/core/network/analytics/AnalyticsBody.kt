package com.appcoins.wallet.core.network.analytics

import com.fasterxml.jackson.annotation.JsonProperty

class AnalyticsBody(
  @field:JsonProperty("aptoide_vercode")
  val appcoinsVercode: Int, @field:JsonProperty("aptoide_package")
  val appcoinsPackage: String, val data: Map<*, *>
)
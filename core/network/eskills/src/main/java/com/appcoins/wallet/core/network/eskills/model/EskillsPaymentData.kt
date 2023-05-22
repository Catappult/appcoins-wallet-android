package com.appcoins.wallet.core.network.eskills.model

import java.math.BigDecimal

data class EskillsPaymentData(
  var scheme: String,
  var host: String,
  var path: String,
  var parameters: MutableMap<String, String?>,
  var userId: String?,
  var userName: String?,
  var packageName: String,
  var product: String?,
  var price: BigDecimal?,
  var currency: String?,
  var environment: MatchEnvironment?,
  var metadata: Map<String, String>,
  var numberOfUsers: Int?,
  var timeout: Int?,
  var queueId: QueueIdentifier? = null
) {

  enum class MatchEnvironment {
    LIVE, SANDBOX
  }
}
